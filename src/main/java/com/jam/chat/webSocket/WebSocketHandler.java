package com.jam.chat.webSocket;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jam.chat.dto.ChatDto;
import com.jam.chat.service.ChatService;
import com.jam.global.util.JsonUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class WebSocketHandler extends TextWebSocketHandler  {
    private final ChatService chatService;
    private final ObjectMapper objectMapper;
    // FIXME: 캡슐화
    /* roomId: {session1, session2}
     * 채팅에 입장하면 세션 추가됨. 채팅 나가면 세션 삭제
     * 특정 채팅방에 참여한 세션들을 관리. 키는 채팅방 ID, 값은 해당 채팅방에 연결된 세션들
     * 특정 채팅방에 속한 클라이언트들에게만 메시지를 보냄.*/
    private final Map<Long,Set<WebSocketSession>> chatRoomSession = new ConcurrentHashMap<>();

    /* session: roomId
     * 채팅에 입장하면 (session, roomId) 매핑 추가됨, 채팅방 나가면 제거됨
     * 특정 세션이 참여한 채팅방 관리, 채팅방 나갈 때 채팅방 id 찾기 위함  */
    private final Map<WebSocketSession, Long> sessionToChatRoom = new ConcurrentHashMap<>();
    
    /* userId: {session1, session2}
     * 접속한 모든 사용자의 ID와 세션을 매핑
     * 멀티탭 허용을 위해 Set 사용
     * */
    private final Map<String, Set<WebSocketSession>> userSessionMap = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String userId = (String) session.getAttributes().get("userId");
        if (userId != null) {
        	userSessionMap
        		.computeIfAbsent(userId, key -> ConcurrentHashMap.newKeySet())
        		.add(session);

            log.info("사용자 {} 연결됨 (세션: {})", userId, session.getId());
        }
    }

    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
    	try {
        	String payload = message.getPayload();
        	
            // 페이로드(JSON 형식의 메시지 데이터) -> ChatDto로 변환
            ChatDto chatDto = convertToChatMessageVO(payload);

            Long roomId = chatDto.getRoomId();
            if (roomId == null) {
                sendError(session, 400, "잘못된 요청입니다.");
                return;
            }
            
    	    Map<String, Object> attributes = session.getAttributes();
    	    String userId = (String) attributes.get("userId");
            
    	    if (userId == null) {
    	    	sendError(session, 401, "로그인이 필요한 서비스 입니다. 로그인 하시겠습니까?");
    	    	session.close(CloseStatus.POLICY_VIOLATION);
    	        return;
    	    }
    	    

            switch (chatDto.getType()) {
	        	case ENTER -> handleEnter(session, roomId, userId);
	        	case LEAVE -> handleLeave(session, roomId);
	        	case MESSAGE -> handleMessage(session, attributes, chatDto, roomId, userId);
	        }
        } catch (Exception e) {
            log.error("Exception: {} : {}", e.getClass().getName(), e.getMessage());
            e.printStackTrace();
            
            sendError(session, 500,  "서버에 문제가 발생했습니다. 잠시 후 다시 시도해 주세요.");
        }
    }
    
	private void handleEnter(WebSocketSession session, Long roomId, String userId) {
    	    	
	    Map<String, String> info = chatService.getChatPartner(roomId, userId);
	    
	    if (info == null || info.get("CHATPARTNERID") == null) {
	    	log.info("info : " + info);
	    	sendMessage(session, "ERROR", Map.of("code", 404, "message", "채팅방을 찾을 수 없습니다."));
	    	return;
		}

        // 채팅방 세션 없으면 만듦
        Set<WebSocketSession> sessionSet = chatRoomSession.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet());

	    sessionSet.add(session);
    	sessionToChatRoom.put(session, roomId);
    	
	    session.getAttributes().put("partnerId", info.get("CHATPARTNERID"));
	    session.getAttributes().put("partnerName", info.get("CHATPARTNERNAME"));
	    
	    sendMessage(session, "PARTNER_INFO", Map.of("partnerName", info.get("CHATPARTNERNAME"))); 
	    
	    
	    log.info("사용자 {}가 채팅방 {}에 입장했습니다.", session.getId(), roomId);
	}

    private void handleLeave(WebSocketSession session, Long roomId) {

    	// 1. chatRoomSession에서 제거
    	Set<WebSocketSession> sessionSet = chatRoomSession.get(roomId);
        if (sessionSet != null) {
            sessionSet.remove(session);

            // 채팅방에 세션이 없으면 맵에서 키 제거
            if (sessionSet.isEmpty()) {
                chatRoomSession.remove(roomId);
            }
        }
        sessionToChatRoom.remove(session);

        log.info("사용자 {}가 채팅방 {}에서 퇴장했습니다.", session.getId(), roomId);
	}
    
    private void handleMessage(
    		WebSocketSession session, 
    		Map<String, Object> attributes,
    		ChatDto chatDto, 
    		Long roomId, 
    		String userId) {
    	try {

    		// payload 검증
    		if (roomId == null || chatDto.getMessage() == null || chatDto.getMessage().isBlank()) {
    			sendMessage(session, "ERROR", Map.of("code", 400, "message", "잘못된 요청 입니다. 다시 시도해 주세요."));
    			return;
    		}
    		// 방 세션 검증
            Set<WebSocketSession> sessionSet = chatRoomSession.get(roomId);
    		if (sessionSet == null || !sessionSet.contains(session)) {
    			sendError(session, 403, "잘못된 접근 입니다.");
    			session.close(CloseStatus.POLICY_VIOLATION);
    			return;
    		}
    		
    		// 멤버십 확인
    		if (!chatService.isMemberOfRoom(userId, roomId)) {
    			sendMessage(session, "ERROR", Map.of("code", 403, "message", "잘못된 접근 입니다."));
    			return;
    		}
    		
    		String partnerId = (String) attributes.get("partnerId");
    		
    		if (partnerId == null) {
    			sendMessage(session, "ERROR", Map.of("code", 404, "message", "채팅방을 찾을 수 없습니다."));
    			return;
    		}
    		
    		chatDto.setReceiverId(partnerId);
    		chatDto.setSenderId(userId);

    	    // 메시지 저장 후 해당 방에만 브로드캐스트
    	    chatService.saveChat(chatDto);
    	    sendMessageToChatRoom(chatDto, "MESSAGE", sessionSet);

    	    Set<WebSocketSession> receiverSessions = userSessionMap.get(partnerId);
    	    if (receiverSessions != null) {
	        	String myName = (String) session.getAttributes().get("userName");
	        	chatDto.setPartner(myName);
	        	
    	    	// 만약 상대방 세션이 현재 이 채팅방의 sessionSet에 포함되어 있지 않다면 알림 전송
	        	for (WebSocketSession receiverSession : receiverSessions) {
	                try {
	                    if (receiverSession.isOpen() && !sessionSet.contains(receiverSession)) {
	                        sendMessage(receiverSession, "NEW_ROOM_ALERT", chatDto);
	                    }
	                } catch (Exception e) {
	                    // 로그만 남기고 다음 세션 계속 처리
	                    log.warn("알림 전송 실패: {}", e.getMessage());
	                }
	        	}
    	    }

	    } catch (Exception e) {
	        throw new RuntimeException("Failed to save chat message", e);
	    }
	}

	// 클라이언트가 연결 끊음
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {

    	log.info("연결 끊김" + session.getId());

    	String userId = (String) session.getAttributes().get("userId");
        if (userId != null) {
            Set<WebSocketSession> sessions = userSessionMap.get(userId);
            if (sessions != null) {
                sessions.remove(session); // 현재 연결된 세션만 Set에서 제거

                if (sessions.isEmpty()) {
                    userSessionMap.remove(userId); // Set이 비었으면 맵에서 키 제거
                }
            }
        }

        // 세션이 속한 채팅방 ID 찾기
        Long roomId = sessionToChatRoom.remove(session);
        
        if (roomId != null) {
            // 해당 채팅방의 세션 집합에서 제거
            Set<WebSocketSession> chatRoomSessions = chatRoomSession.get(roomId);
            if (chatRoomSessions != null) {
                chatRoomSessions.remove(session);
                if (chatRoomSessions.isEmpty()) {
                    chatRoomSession.remove(roomId);
                }
            }
        }
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
    private void sendMessageToChatRoom(ChatDto chatDto, String type, Set<WebSocketSession> sessionSet) {
    	sessionSet.stream().forEach(sess -> sendMessage(sess, type, chatDto));
    }

    private void sendError(WebSocketSession session, int code, String msg) {
    	sendMessage(session, "ERROR", Map.of("code", code, "message", msg));
    }

    public ChatDto convertToChatMessageVO(String payload) {
    	log.info("payload = {}", payload);
        try {
            return objectMapper.readValue(payload, ChatDto.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert payload to VO", e);
        }
	}
}