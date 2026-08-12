package com.jam.chat.webSocket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jam.chat.service.ChatService;

/**
 * WebSocketHandler에 대한 Mockito 단위 테스트. 세션은 목으로 대체하고, 실제 송신 여부는
 * session.sendMessage(...) 호출을 캡처해 검증한다. 내부 세션 매핑(ConcurrentHashMap)은
 * private 필드라 ReflectionTestUtils로 상태를 세팅/검증한다.
 */
@ExtendWith(MockitoExtension.class)
class WebSocketHandlerTest {

	@Mock
	private ChatService chatService;

	private WebSocketHandler handler;
	private ObjectMapper objectMapper = new ObjectMapper();

	@SuppressWarnings("unchecked")
	private Map<Long, Set<WebSocketSession>> chatRoomSession() {
		return (Map<Long, Set<WebSocketSession>>) ReflectionTestUtils.getField(handler, "chatRoomSession");
	}

	@SuppressWarnings("unchecked")
	private Map<WebSocketSession, Long> sessionToChatRoom() {
		return (Map<WebSocketSession, Long>) ReflectionTestUtils.getField(handler, "sessionToChatRoom");
	}

	@SuppressWarnings("unchecked")
	private Map<String, Set<WebSocketSession>> userSessionMap() {
		return (Map<String, Set<WebSocketSession>>) ReflectionTestUtils.getField(handler, "userSessionMap");
	}

	private WebSocketSession mockSession(String id, Map<String, Object> attributes) {
		WebSocketSession session = mock(WebSocketSession.class);
		lenient().when(session.getId()).thenReturn(id);
		lenient().when(session.getAttributes()).thenReturn(new ConcurrentHashMap<>(attributes));
		lenient().when(session.isOpen()).thenReturn(true);
		return session;
	}

	@BeforeEach
	void setUp() {
		handler = new WebSocketHandler(chatService, objectMapper);
	}

	private String toJson(Object payload) {
		try {
			return objectMapper.writeValueAsString(payload);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private String sentPayload(WebSocketSession session) throws Exception {
		ArgumentCaptor<TextMessage> captor = ArgumentCaptor.forClass(TextMessage.class);
		verify(session).sendMessage(captor.capture());
		return captor.getValue().getPayload();
	}

	@Nested
	@DisplayName("afterConnectionEstablished")
	class AfterConnectionEstablished {

		@Test
		@DisplayName("userId가 있으면 userSessionMap에 등록된다")
		void withUserId_registersSession() throws Exception {
			WebSocketSession session = mockSession("s1", Map.of("userId", "user1"));

			handler.afterConnectionEstablished(session);

			assertThat(userSessionMap().get("user1")).contains(session);
		}

		@Test
		@DisplayName("userId가 없으면 등록하지 않고 예외도 던지지 않는다")
		void withoutUserId_doesNotRegister() throws Exception {
			WebSocketSession session = mockSession("s1", Map.of());

			handler.afterConnectionEstablished(session);

			assertThat(userSessionMap()).isEmpty();
		}
	}

	@Nested
	@DisplayName("handleTextMessage - ENTER")
	class HandleTextMessageEnter {

		@Test
		@DisplayName("roomId가 없으면 400 에러를 보낸다")
		void noRoomId_sendsBadRequest() throws Exception {
			WebSocketSession session = mockSession("s1", Map.of("userId", "user1"));
			String payload = "{\"type\":\"ENTER\"}";

			handler.handleTextMessage(session, new TextMessage(payload));

			assertThat(sentPayload(session)).contains("\"code\":400");
		}

		@Test
		@DisplayName("로그인 사용자가 아니면 401 에러를 보내고 세션을 닫는다")
		void noUserId_sendsUnauthorizedAndCloses() throws Exception {
			WebSocketSession session = mockSession("s1", Map.of());
			String payload = toJson(Map.of("type", "ENTER", "roomId", 1));

			handler.handleTextMessage(session, new TextMessage(payload));

			assertThat(sentPayload(session)).contains("\"code\":401");
			verify(session).close(CloseStatus.POLICY_VIOLATION);
		}

		@Test
		@DisplayName("방 멤버가 아니면 403 에러를 보내고 세션을 등록하지 않는다")
		void notMember_sendsForbidden_doesNotRegister() throws Exception {
			WebSocketSession session = mockSession("s1", Map.of("userId", "intruder"));
			given(chatService.isMemberOfRoom("intruder", 1L)).willReturn(false);
			String payload = toJson(Map.of("type", "ENTER", "roomId", 1));

			handler.handleTextMessage(session, new TextMessage(payload));

			assertThat(sentPayload(session)).contains("\"code\":403");
			verify(chatService, never()).getChatPartner(any(), any());
			assertThat(chatRoomSession()).doesNotContainKey(1L);
		}

		@Test
		@DisplayName("멤버인데 상대방 정보를 찾을 수 없으면 404 에러를 보낸다")
		void memberButNoPartnerInfo_sendsNotFound() throws Exception {
			WebSocketSession session = mockSession("s1", Map.of("userId", "user1"));
			given(chatService.isMemberOfRoom("user1", 1L)).willReturn(true);
			given(chatService.getChatPartner(1L, "user1")).willReturn(null);
			String payload = toJson(Map.of("type", "ENTER", "roomId", 1));

			handler.handleTextMessage(session, new TextMessage(payload));

			assertThat(sentPayload(session)).contains("\"code\":404");
		}

		@Test
		@DisplayName("정상 입장 시 세션을 방에 등록하고 상대방 정보를 응답한다")
		void success_registersSessionAndSendsPartnerInfo() throws Exception {
			WebSocketSession session = mockSession("s1", Map.of("userId", "user1"));
			given(chatService.isMemberOfRoom("user1", 1L)).willReturn(true);
			given(chatService.getChatPartner(1L, "user1"))
					.willReturn(Map.of("CHATPARTNERID", "user2", "CHATPARTNERNAME", "상대방"));
			String payload = toJson(Map.of("type", "ENTER", "roomId", 1));

			handler.handleTextMessage(session, new TextMessage(payload));

			assertThat(sentPayload(session)).contains("PARTNER_INFO").contains("상대방");
			assertThat(chatRoomSession().get(1L)).contains(session);
			assertThat(sessionToChatRoom().get(session)).isEqualTo(1L);
			assertThat(session.getAttributes().get("partnerId")).isEqualTo("user2");
		}
	}

	@Nested
	@DisplayName("handleTextMessage - LEAVE")
	class HandleTextMessageLeave {

		@Test
		@DisplayName("방 세션 목록과 세션-방 매핑에서 제거된다")
		void removesFromRoomAndMapping() throws Exception {
			WebSocketSession session = mockSession("s1", Map.of("userId", "user1"));
			chatRoomSession().put(1L, ConcurrentHashMap.newKeySet());
			chatRoomSession().get(1L).add(session);
			sessionToChatRoom().put(session, 1L);
			String payload = toJson(Map.of("type", "LEAVE", "roomId", 1));

			handler.handleTextMessage(session, new TextMessage(payload));

			assertThat(chatRoomSession()).doesNotContainKey(1L);
			assertThat(sessionToChatRoom()).doesNotContainKey(session);
		}
	}

	@Nested
	@DisplayName("handleTextMessage - MESSAGE")
	class HandleTextMessageMessage {

		private WebSocketSession session;

		@BeforeEach
		void enterRoom() {
			session = mockSession("s1", new ConcurrentHashMap<>(Map.of("userId", "user1", "partnerId", "user2")));
			chatRoomSession().put(1L, ConcurrentHashMap.newKeySet());
			chatRoomSession().get(1L).add(session);
		}

		@Test
		@DisplayName("메시지 내용이 없으면 400 에러를 보내고 저장하지 않는다")
		void blankMessage_sendsBadRequest() throws Exception {
			String payload = toJson(Map.of("type", "MESSAGE", "roomId", 1, "message", ""));

			handler.handleTextMessage(session, new TextMessage(payload));

			assertThat(sentPayload(session)).contains("\"code\":400");
			verify(chatService, never()).saveChat(any());
		}

		@Test
		@DisplayName("방 세션 목록에 없는 세션이 보내면 403 에러를 보내고 세션을 닫는다")
		void sessionNotInRoom_sendsForbiddenAndCloses() throws Exception {
			WebSocketSession outsider = mockSession("s2", Map.of("userId", "user3"));
			String payload = toJson(Map.of("type", "MESSAGE", "roomId", 1, "message", "hi"));

			handler.handleTextMessage(outsider, new TextMessage(payload));

			assertThat(sentPayload(outsider)).contains("\"code\":403");
			verify(outsider).close(CloseStatus.POLICY_VIOLATION);
			verify(chatService, never()).saveChat(any());
		}

		@Test
		@DisplayName("DB 멤버십 확인에 실패하면 403 에러를 보낸다")
		void notMemberInDb_sendsForbidden() throws Exception {
			given(chatService.isMemberOfRoom("user1", 1L)).willReturn(false);
			String payload = toJson(Map.of("type", "MESSAGE", "roomId", 1, "message", "hi"));

			handler.handleTextMessage(session, new TextMessage(payload));

			assertThat(sentPayload(session)).contains("\"code\":403");
			verify(chatService, never()).saveChat(any());
		}

		@Test
		@DisplayName("정상 메시지면 저장 후 방의 모든 세션에게 브로드캐스트한다")
		void success_savesAndBroadcasts() throws Exception {
			WebSocketSession other = mockSession("s2", Map.of("userId", "user2"));
			chatRoomSession().get(1L).add(other);

			given(chatService.isMemberOfRoom("user1", 1L)).willReturn(true);
			String payload = toJson(Map.of("type", "MESSAGE", "roomId", 1, "message", "안녕"));

			handler.handleTextMessage(session, new TextMessage(payload));

			verify(chatService).saveChat(any());
			assertThat(sentPayload(session)).contains("MESSAGE").contains("안녕");
			assertThat(sentPayload(other)).contains("MESSAGE").contains("안녕");
		}

		@Test
		@DisplayName("상대방이 다른 세션으로 접속해 있지만 이 방에 있지 않으면 NEW_ROOM_ALERT를 보낸다")
		void partnerConnectedElsewhere_sendsNewRoomAlert() throws Exception {
			WebSocketSession partnerOtherSession = mockSession("s3", Map.of("userId", "user2"));
			userSessionMap().put("user2", ConcurrentHashMap.newKeySet());
			userSessionMap().get("user2").add(partnerOtherSession);

			given(chatService.isMemberOfRoom("user1", 1L)).willReturn(true);
			String payload = toJson(Map.of("type", "MESSAGE", "roomId", 1, "message", "안녕"));

			handler.handleTextMessage(session, new TextMessage(payload));

			assertThat(sentPayload(partnerOtherSession)).contains("NEW_ROOM_ALERT");
		}
	}

	@Nested
	@DisplayName("afterConnectionClosed")
	class AfterConnectionClosed {

		@Test
		@DisplayName("사용자 세션 목록과 채팅방 세션 목록에서 모두 제거된다")
		void removesFromAllMappings() throws Exception {
			WebSocketSession session = mockSession("s1", Map.of("userId", "user1"));
			userSessionMap().put("user1", ConcurrentHashMap.newKeySet());
			userSessionMap().get("user1").add(session);
			chatRoomSession().put(1L, ConcurrentHashMap.newKeySet());
			chatRoomSession().get(1L).add(session);
			sessionToChatRoom().put(session, 1L);

			handler.afterConnectionClosed(session, CloseStatus.NORMAL);

			assertThat(userSessionMap()).doesNotContainKey("user1");
			assertThat(chatRoomSession()).doesNotContainKey(1L);
			assertThat(sessionToChatRoom()).doesNotContainKey(session);
		}
	}
}
