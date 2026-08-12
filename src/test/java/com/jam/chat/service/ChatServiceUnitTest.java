package com.jam.chat.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.jam.chat.dto.ChatDto;
import com.jam.chat.dto.ChatRoomListDto;
import com.jam.chat.mapper.ChatMapper;

/**
 * ChatService에 대한 Mockito 단위 테스트.
 * 단순 mapper 위임 메서드는 위임 확인용으로만 작성했고, XML에 있는 실제 SQL은 이 유닛테스트로 검증되지 않는다.
 */
@ExtendWith(MockitoExtension.class)
class ChatServiceUnitTest {

	@Mock
	private ChatMapper chatMapper;
	@Mock
	private RedisTemplate<String, Object> redisTemplate;
	@Mock
	private ValueOperations<String, Object> valueOperations;

	@InjectMocks
	private ChatService chatService;

	@Nested
	@DisplayName("getChatRooms")
	class GetChatRooms {

		@Test
		@DisplayName("캐시에 있으면 mapper를 호출하지 않고 캐시된 값을 반환한다")
		void cacheHit_returnsCachedWithoutQueryingDb() {
			List<ChatRoomListDto> cached = List.of(new ChatRoomListDto());
			given(redisTemplate.opsForValue()).willReturn(valueOperations);
			given(valueOperations.get("chat:list:user1")).willReturn(cached);

			List<ChatRoomListDto> result = chatService.getChatRooms("user1");

			assertThat(result).isEqualTo(cached);
			verify(chatMapper, never()).getChatRooms(any());
		}

		@Test
		@DisplayName("캐시에 없으면 DB에서 조회하고 10분 TTL로 캐싱한다")
		void cacheMiss_queriesDbAndCaches() {
			List<ChatRoomListDto> fromDb = List.of(new ChatRoomListDto());
			given(redisTemplate.opsForValue()).willReturn(valueOperations);
			given(valueOperations.get("chat:list:user1")).willReturn(null);
			given(chatMapper.getChatRooms("user1")).willReturn(fromDb);

			List<ChatRoomListDto> result = chatService.getChatRooms("user1");

			assertThat(result).isEqualTo(fromDb);
			verify(valueOperations).set("chat:list:user1", fromDb, 10, TimeUnit.MINUTES);
		}
	}

	@Nested
	@DisplayName("createChatRoomWithTransaction")
	class CreateChatRoomWithTransaction {

		@Test
		@DisplayName("이미 방이 있으면 새로 만들지 않고 기존 roomId를 반환한다")
		void existingRoom_returnsWithoutCreating() {
			given(chatMapper.getChatRoomId("user1", "user2")).willReturn(10L);

			Long roomId = chatService.createChatRoomWithTransaction("user1", "user2", "user1:user2");

			assertThat(roomId).isEqualTo(10L);
			verify(chatMapper, never()).nextChatRoomId();
			verify(chatMapper, never()).createChatRoomId(anyLong(), any());
		}

		@Test
		@DisplayName("방이 없으면 새로 생성하고 두 사용자를 모두 등록한다")
		void noExistingRoom_createsNewRoom() {
			given(chatMapper.getChatRoomId("user1", "user2")).willReturn(null);
			given(chatMapper.nextChatRoomId()).willReturn(20L);

			Long roomId = chatService.createChatRoomWithTransaction("user1", "user2", "user1:user2");

			assertThat(roomId).isEqualTo(20L);
			verify(chatMapper).createChatRoomId(20L, "user1:user2");
			verify(chatMapper).insertChatRoomUser(20L, "user1");
			verify(chatMapper).insertChatRoomUser(20L, "user2");
		}

		@Test
		@DisplayName("동시에 다른 스레드가 방을 먼저 만들었으면(DuplicateKeyException) 그 방 id를 다시 조회해서 반환한다")
		void duplicateKeyException_fallsBackToLookup() {
			given(chatMapper.getChatRoomId("user1", "user2"))
					.willReturn(null)
					.willReturn(20L);
			given(chatMapper.nextChatRoomId()).willReturn(20L);
			doThrow(new DuplicateKeyException("already exists")).when(chatMapper).createChatRoomId(20L, "user1:user2");

			Long roomId = chatService.createChatRoomWithTransaction("user1", "user2", "user1:user2");

			assertThat(roomId).isEqualTo(20L);
			verify(chatMapper, never()).insertChatRoomUser(any(), any());
		}
	}

	@Nested
	@DisplayName("getChatPartner")
	class GetChatPartner {

		@Test
		@DisplayName("mapper 결과 그대로 반환한다")
		void delegatesToMapper() {
			Map<String, String> partner = Map.of("CHATPARTNERID", "user2");
			given(chatMapper.getChatPartner(10L, "user1")).willReturn(partner);

			assertThat(chatService.getChatPartner(10L, "user1")).isEqualTo(partner);
		}
	}

	@Nested
	@DisplayName("saveChat")
	class SaveChat {

		@Test
		@DisplayName("전송 시각을 세팅해 저장하고, 송신자/수신자의 채팅목록 캐시를 모두 삭제한다")
		void setsSentAtAndInvalidatesBothCaches() {
			ChatDto chat = new ChatDto();
			chat.setSenderId("user1");
			chat.setReceiverId("user2");

			chatService.saveChat(chat);

			assertThat(chat.getSentAt()).isNotNull();
			verify(chatMapper).saveChat(chat);
			verify(redisTemplate).delete("chat:list:user1");
			verify(redisTemplate).delete("chat:list:user2");
		}
	}

	@Nested
	@DisplayName("getMessages")
	class GetMessages {

		@Test
		@DisplayName("본인이 보낸 메시지는 mine=true, 아니면 mine=false로 설정한다")
		void setsMineFlagCorrectly() {
			ChatDto myMessage = new ChatDto();
			myMessage.setSenderId("user1");
			ChatDto otherMessage = new ChatDto();
			otherMessage.setSenderId("user2");
			given(chatMapper.getMessages(10L, "user1")).willReturn(List.of(myMessage, otherMessage));

			List<ChatDto> result = chatService.getMessages(10L, "user1");

			assertThat(result.get(0).isMine()).isTrue();
			assertThat(result.get(1).isMine()).isFalse();
		}
	}

	@Nested
	@DisplayName("단순 mapper 위임 메서드")
	class MapperDelegation {

		@Test
		@DisplayName("isMemberOfRoom - 0건 초과면 true")
		void isMemberOfRoom_true() {
			given(chatMapper.isMemberOfRoom(10L, "user1")).willReturn(1);

			assertThat(chatService.isMemberOfRoom("user1", 10L)).isTrue();
		}

		@Test
		@DisplayName("isMemberOfRoom - 0건이면 false")
		void isMemberOfRoom_false() {
			given(chatMapper.isMemberOfRoom(10L, "user1")).willReturn(0);

			assertThat(chatService.isMemberOfRoom("user1", 10L)).isFalse();
		}

		@Test
		@DisplayName("getChatRoomId - mapper 결과 그대로 반환")
		void getChatRoomId_delegates() {
			given(chatMapper.getChatRoomId("user1", "user2")).willReturn(10L);

			assertThat(chatService.getChatRoomId("user1", "user2")).isEqualTo(10L);
		}
	}
}
