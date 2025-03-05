package com.jam.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jam.client.chat.service.ChatService;
import com.jam.client.chat.vo.ChatVO;
import com.jam.client.member.service.MemberService;
import com.jam.common.controller.CommonController;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;

//@Component
@Log4j
@RequiredArgsConstructor
public class WebSocketHandler extends TextWebSocketHandler  {

    //private final RedisTemplate<String, String> stringRedisTemplate;
    private final ChatService chatService;
    
    private final MemberService memberService;
    
    // 현재 연결된 모든 WebSocket 세션을 관리
    private final Set<WebSocketSession> sessions = new HashSet<>();

    /* chatRoomId: {session1, session2}
     * 채팅에 입장하면 세션 추가됨. 채팅 나가면 세션 삭제
     * 특정 채팅방에 참여한 세션들을 관리. 키는 채팅방 ID, 값은 해당 채팅방에 연결된 세션들 /
     * 특정 채팅방에 속한 클라이언트들에게만 메시지를 보냄.*/
    private final Map<String,Set<WebSocketSession>> chatRoomSession = new HashMap<>();

    /* session: chatRoomId
     * 채팅에 입장하면 채팅방 추가됨, 채팅방 
     * 특정 세션이 참여한 채팅방 관리, 채팅방 나갈 때 채팅방 id 찾기 위함 -> ?? 이거 없어도 채팅방 id 찾을 수 있는데?? */
    private final Map<WebSocketSession, String> sessionToChatRoom = new ConcurrentHashMap<>();

    // 소켓 연결
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        
        log.info("{} 연결됨" + session.getId());
            
        sessions.add(session);
        
    }
    
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        
    	try {
        	String payload = message.getPayload();
        	
            // 페이로드(JSON 형식의 메시지 데이터) -> ChatVO로 변환
            ChatVO chatVO = convertToChatMessageVO(payload);

            String chatRoomId = chatVO.getChatRoomId();
            
            // 메모리 상에 채팅방에 대한 세션 없으면 만들어줌
            if(!chatRoomSession.containsKey(chatRoomId)){
            	chatRoomSession.put(chatRoomId,new HashSet<>());
            }
            
            // 특정 채팅방에 연결된 세션 집합 가져옴
            Set<WebSocketSession> sessionSet = chatRoomSession.get(chatRoomId);

            // ENTER(채팅방 입장)
            if (chatVO.getType().equals(ChatVO.Type.ENTER)) {
            	
            	sessionSet.add(session);
            	sessionToChatRoom.put(session, chatRoomId);
            	
            	String participant = chatVO.getParticipant();
            	
            	Map<String, Object> attributes = session.getAttributes();
                String userId = (String) attributes.get("userId");
                
            	chatService.addParticipant(chatRoomId, participant, userId);
            
            } else if (chatVO.getType().equals(ChatVO.Type.LEAVE)) {
            	
                // 1. chatRoomSession에서 제거
                if (chatRoomSession.containsKey(chatRoomId)) {
                    sessionSet.remove(session);

                    // 채팅방에 세션이 없으면 맵에서 키 제거
                    if (sessionSet.isEmpty()) {
                        chatRoomSession.remove(chatRoomId);
                    }
                }

                // 2. sessionToChatRoom에서 제거
                sessionToChatRoom.remove(session);

                // 3. sessions에서 제거
                sessions.remove(session);

                log.info("사용자 " + session.getId() + "가 채팅방 " + chatRoomId + "에서 퇴장했습니다.");
            
            } else if (chatVO.getType().equals(ChatVO.Type.MESSAGE)) {
            	try {
            		Map<String, Object> attributes = session.getAttributes();
                    String userId = (String) attributes.get("userId");
                    
                    if (userId == null) {
            			log.error("Send Chatting senderId is null.");
            		}
                    
            		chatVO.setSenderId(userId);
            		
            		String receiverId = chatService.getParticipant(chatVO.getChatRoomId(), userId);
            		
            		chatVO.setReceiverId(receiverId);
            		chatVO.setMine(true);
            		
            		
            		log.info("chatVO : " + chatVO);
            		// redis에 채팅 저장
            		chatService.saveChat(chatVO);
            		
            		Map<String, String> messageData = new HashMap<>();
                    messageData.put("receiver", receiverId);
                    messageData.put("message", chatVO.getMessage());
                    
                    
                    log.info(messageData);
                    
                    sendMessageToChatRoom(chatVO, sessionSet);
        	    } catch (Exception e) {
        	        throw new RuntimeException("Failed to save chat message", e);
        	    }
            }
            
            
            
        } catch (Exception e) {
            log.error("Exception: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
            
            // 사용자에게 오류 메시지 전송
            sendErrorMessage(session, "An error occurred while processing your message.");
        }
    	

    }

    // 클라이언트가 연결 끊음
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        
    	log.info("{} 연결 끊김" + session.getId());
        
        // 세션이 속한 채팅방 ID 찾기
        String chatRoomId = sessionToChatRoom.get(session);
        if (chatRoomId != null) {
            // 해당 채팅방의 세션 집합에서 제거
            Set<WebSocketSession> chatRoomSessions = chatRoomSession.get(chatRoomId);
            if (chatRoomSessions != null) {
                chatRoomSessions.remove(session);
            }
        }

        chatRoomSession.values().forEach(this::removeClosedSession);
        sessionToChatRoom.remove(session);
        sessions.remove(session);
    }

    // 닫힌 세션을 chatRoomSession에서 제거
    private void removeClosedSession(Set<WebSocketSession> chatRoomSession) {
        chatRoomSession.removeIf(sess -> !sessions.contains(sess));
    }

    // 특정 채팅방의 모든 클라이언트에게 메시지를 전송
    private void sendMessageToChatRoom(ChatVO chatVO, Set<WebSocketSession> sessionSet) {
    	sessionSet.stream().forEach(sess -> sendMessage(sess, chatVO));
    }

    // 특정 세션에 메시지 전송
    public void sendMessage(WebSocketSession session, Object message) {
        try {
            if (session.isOpen()) {
            	String jsonMessage = JsonUtils.toJson(message);
            	session.sendMessage(new TextMessage(jsonMessage));
            } else {
                log.warn("WebSocket session is not open. Skipping message for session: {}" + session.getId());
            }
        } catch (IOException e) {
        	log.error("Error sending WebSocket message: {}" + e.getMessage()+ e);
        }
    }
    
    
    // 사용자에게 오류 메시지 전송
    private void sendErrorMessage(WebSocketSession session, String errorMessage) {
        try {
            // 오류 메시지를 JSON 형식으로 변환하여 전송
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("type", "ERROR");
            errorResponse.put("message", errorMessage);
            String jsonMessage = JsonUtils.toJson(errorResponse);
            session.sendMessage(new TextMessage(jsonMessage));
            
           // session.sendMessage(new TextMessage(mapper.writeValueAsString(errorResponse)));
        } catch (IOException e) {
            log.error("Failed to send error message: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public ChatVO convertToChatMessageVO(String payload) {
		ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(payload, ChatVO.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert payload to VO", e);
        }
	}


}
