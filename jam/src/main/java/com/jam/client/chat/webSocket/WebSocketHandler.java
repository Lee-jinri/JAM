package com.jam.client.chat.webSocket;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jam.client.chat.service.ChatService;
import com.jam.client.chat.vo.ChatVO;
import com.jam.global.util.JsonUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;

@Log4j
@RequiredArgsConstructor
public class WebSocketHandler extends TextWebSocketHandler  {

    private final ChatService chatService;
    
    // 현재 연결된 모든 WebSocket 세션을 관리
    private final Set<WebSocketSession> sessions = java.util.concurrent.ConcurrentHashMap.newKeySet();

    /* chatRoomId: {session1, session2}
     * 채팅에 입장하면 세션 추가됨. 채팅 나가면 세션 삭제
     * 특정 채팅방에 참여한 세션들을 관리. 키는 채팅방 ID, 값은 해당 채팅방에 연결된 세션들 /
     * 특정 채팅방에 속한 클라이언트들에게만 메시지를 보냄.*/
    private final Map<String,Set<WebSocketSession>> chatRoomSession = new ConcurrentHashMap<>();

    /* session: chatRoomId
     * 채팅에 입장하면 채팅방 추가됨, 채팅방 
     * 특정 세션이 참여한 채팅방 관리, 채팅방 나갈 때 채팅방 id 찾기 위함  */
    private final Map<WebSocketSession, String> sessionToChatRoom = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("소켓 연결됨" + session.getId());
        sessions.add(session);
    }

    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
    	try {
        	String payload = message.getPayload();
        	
            // 페이로드(JSON 형식의 메시지 데이터) -> ChatVO로 변환
            ChatVO chatVO = convertToChatMessageVO(payload);

            String chatRoomId = chatVO.getChatRoomId();
            
    	    Map<String, Object> attributes = session.getAttributes();
    	    String userId = (String) attributes.get("userId");
            
    	    if (userId == null) {
    	    	sendError(session, 401, "로그인이 필요한 서비스 입니다. 로그인 하시겠습니까?");
    	    	session.close(CloseStatus.POLICY_VIOLATION);
    	        return;
    	    }
    	    
            // 채팅방 세션 없으면 만듦
            if (!chatRoomSession.containsKey(chatRoomId)) {
                chatRoomSession.put(chatRoomId, ConcurrentHashMap.newKeySet());
            }
            
            // 특정 채팅방에 연결된 세션 집합
            Set<WebSocketSession> sessionSet = chatRoomSession.get(chatRoomId);

            // ENTER(채팅방 입장)
            if (chatVO.getType().equals(ChatVO.Type.ENTER)) {
            	
            	sessionSet.add(session);
            	sessionToChatRoom.put(session, chatRoomId);
            	
        	    Map<String, String> info = chatService.getChatPartner(chatVO.getChatRoomId(), userId);
        	    
        	    if (info == null || info.get("chatPartnerId") == null) {
        	    	sessionSet.remove(session);
        	    	if (sessionSet.isEmpty()) chatRoomSession.remove(chatRoomId);
        	    	sessionToChatRoom.remove(session);
        	    	sendMessage(session, "ERROR", Map.of("code", 404, "message", "채팅방을 찾을 수 없습니다."));
        	    	return;
        		}
        	    
        	    session.getAttributes().put("partnerId", info.get("chatPartnerId"));
        	    session.getAttributes().put("partnerName", info.get("chatPartnerName"));
        	    
        	    sendMessage(session, "PARTNER_INFO", Map.of("partnerName", info.get("chatPartnerName"))); 
        	    
        	    log.info("사용자 " + session.getId() + "가 채팅방 " + chatRoomId + "에 입장했습니다.");
            } else if (chatVO.getType().equals(ChatVO.Type.LEAVE)) {
            	
                // 1. chatRoomSession에서 제거
                if (chatRoomSession.containsKey(chatRoomId)) {
                    sessionSet.remove(session);

                    // 채팅방에 세션이 없으면 맵에서 키 제거
                    if (sessionSet.isEmpty()) {
                        chatRoomSession.remove(chatRoomId);
                    }
                }

                sessionToChatRoom.remove(session);

                log.info("사용자 " + session.getId() + "가 채팅방 " + chatRoomId + "에서 퇴장했습니다.");
            
            } else if (chatVO.getType().equals(ChatVO.Type.MESSAGE)) {
            	try {

            		// payload 검증
            		if (chatRoomId == null || chatVO.getMessage() == null || chatVO.getMessage().isBlank()) {
            			sendMessage(session, "ERROR", Map.of("code", 400, "message", "잘못된 요청 입니다. 다시 시도해 주세요."));
            			return;
            		}
            		// 방 세션 검증
            		if (sessionSet == null || !sessionSet.contains(session)) {
            			sendError(session, 403, "잘못된 접근 입니다.");
            			session.close(CloseStatus.POLICY_VIOLATION);
            			return;
            		}
            		
            		// redis에 채팅방 있는지
            		if (!chatService.roomExists(chatRoomId)) {
            			sendMessage(session, "ERROR", Map.of("code", 404, "message", "채팅방을 찾을 수 없습니다."));
            			return;
            		}
            		
            		// Redis에서 멤버십 확인
            		if (!chatService.isMemberOfRoom(userId, chatRoomId)) {
            			sendMessage(session, "ERROR", Map.of("code", 403, "message", "잘못된 접근 입니다."));
            			return;
            		}
            		
            		String partnerId = (String) attributes.get("partnerId");
            		
            		if (partnerId == null) {
            			sendMessage(session, "ERROR", Map.of("code", 404, "message", "채팅방을 찾을 수 없습니다."));
            			return;
            		}
            		
            		chatVO.setReceiverId(partnerId);
            	    chatVO.setSenderId(userId);

            	    // 첫 메시지인지 확인
            	    boolean isFirst = chatService.ensureRoomOnFirstMessage(chatRoomId, userId, partnerId);

            		if (isFirst) { 
            	    	String userName = chatService.getUserNameFromRedis(userId);
            			String partnerName = (String) attributes.get("partnerName");
            			
            			if (chatVO.getChatDate() == null) chatVO.setChatDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            			
            			if (userName != null && partnerName != null) {
                			broadcastRoomCreated(chatRoomId, userId, userName, partnerId, partnerName, chatVO.getMessage(), chatVO.getChatDate());
                		}
            		}
            		
            	    // 메시지 저장 후 해당 방에만 브로드캐스트
            	    chatService.saveChat(chatVO);
            	    sendMessageToChatRoom(chatVO, "MESSAGE", sessionSet);
                    
        	    } catch (Exception e) {
        	        throw new RuntimeException("Failed to save chat message", e);
        	    }
            }
        } catch (Exception e) {
            log.error("Exception: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
            
            sendError(session, 500,  "서버에 문제가 발생했습니다. 잠시 후 다시 시도해 주세요.");
        }
    }

    // 클라이언트가 연결 끊음
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        
    	log.info("연결 끊김" + session.getId());
    	
        sessions.remove(session);
        
        // 세션이 속한 채팅방 ID 찾기
        //String chatRoomId = sessionToChatRoom.get(session);
        String chatRoomId = sessionToChatRoom.remove(session);
        
        if (chatRoomId != null) {
            // 해당 채팅방의 세션 집합에서 제거
            Set<WebSocketSession> chatRoomSessions = chatRoomSession.get(chatRoomId);
            if (chatRoomSessions != null) {
                chatRoomSessions.remove(session);
            }
        }

        chatRoomSession.values().forEach(this::removeClosedSession);
    }

    // 닫힌 세션을 chatRoomSession에서 제거
    private void removeClosedSession(Set<WebSocketSession> chatRoomSession) {
        chatRoomSession.removeIf(sess -> !sessions.contains(sess));
    }
    
    private void sendMessage(WebSocketSession session, String type, Object data) {
        if (session == null || !session.isOpen()) {
            log.warn("Skipping message. Session is null or closed.");
            return;
        }
        
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("type", type);
            payload.put("data", data);

            String json = JsonUtils.toJson(payload);
            session.sendMessage(new TextMessage(json));
        } catch (IOException e) {
            log.error("Error sending WebSocket message: " + e.getMessage(), e);
        }
    }


    // 특정 채팅방의 모든 클라이언트에게 메시지를 전송
    private void sendMessageToChatRoom(ChatVO chatVO, String type, Set<WebSocketSession> sessionSet) {
    	sessionSet.stream().forEach(sess -> sendMessage(sess, type, chatVO));
    }
    
    // 현재 연결된 세션들 중 userId가 일치하는 모든 세션에 전송 (PC+모바일에서 동시 사용해도 새 채팅방 생성되도록)
    private void sendMessageToUser(String userId, String type, Object data) {
    	for (WebSocketSession s : sessions) {
    		if (!s.isOpen()) continue;
    		Object uid = s.getAttributes().get("userId");
    		if (userId != null && userId.equals(uid)) {
    			sendMessage(s, type, data);
    		}
    	}
    }
    
    private void broadcastRoomCreated(
    		String chatRoomId, 
    		String senderId, String senderName, 
    		String partnerId, String partnerName, 
    		String firstMessage, String chatDate) {

    	// 송신자에게는 partner = 상대 이름
    	Map<String, Object> toSender = Map.of(
    		"chatRoomId", chatRoomId,
    		"partnerId", partnerId,
    		"partner", partnerName,
    		"message", firstMessage,
    		"chatDate", chatDate
    	);
    	sendMessageToUser(senderId, "ROOM_CREATED", toSender);

    	// 수신자에게는 partner = 보내는 사람 이름
    	Map<String, Object> toPartner = Map.of(
    		"chatRoomId", chatRoomId,
    		"partnerId", senderId,
    		"partner", senderName,
    		"message", firstMessage,
    		"chatDate", chatDate
    	);
    	sendMessageToUser(partnerId, "ROOM_CREATED", toPartner);
    }

    private void sendError(WebSocketSession session, int code, String msg) {
    	sendMessage(session, "ERROR", Map.of("code", code, "message", msg));
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
