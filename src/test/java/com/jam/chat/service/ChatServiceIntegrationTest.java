package com.jam.chat.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.jam.chat.dto.ChatDto;
import com.jam.chat.dto.ChatRoomListDto;
import com.jam.member.entity.Member;
import com.jam.member.repository.MemberRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * ChatService/ChatRoomFacade에 대한 @SpringBootTest 통합테스트. ChatMapper.xml의 실제 SQL과,
 * 실제 Redis 캐시, 실제 Redisson 분산 락 동시성까지 검증한다.
 * 동시성 테스트(concurrent room creation)는 여러 스레드가 테스트 메서드의 트랜잭션 밖에서
 * 각자 커밋하므로, @Transactional 롤백에 기대지 않고 테스트 종료 후 직접 정리한다.
 */
@SpringBootTest
class ChatServiceIntegrationTest {

	@Autowired
	private ChatService chatService;

	@Autowired
	private ChatRoomFacade chatRoomFacade;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	@PersistenceContext
	private EntityManager entityManager;

	private int seq = 0;

	private String uniqueId(String prefix) {
		return prefix + (++seq) + "_" + System.nanoTime() % 100000;
	}

	private Member seedMemberCommitted(String prefix) {
		String userId = uniqueId(prefix);
		Member member = new Member();
		member.setUserId(userId);
		member.setUserPw("pw1234");
		member.setUserName("n" + Integer.toHexString(userId.hashCode()));
		Member saved = memberRepository.save(member);
		// @Transactional 테스트에서는 이후 MyBatis(다른 세션)에서도 이 회원이 보이도록 flush가 필요하지만,
		// 동시성 테스트처럼 트랜잭션 밖에서 호출되면 flush할 트랜잭션 자체가 없어 예외가 난다.
		if (org.springframework.transaction.support.TransactionSynchronizationManager.isActualTransactionActive()) {
			entityManager.flush();
		}
		return saved;
	}

	@Nested
	@DisplayName("getChatRooms / caching")
	@Transactional
	class GetChatRoomsAndCaching {

		@Test
		@DisplayName("채팅방 목록을 조회하면 상대방 닉네임과 마지막 메시지가 포함된다")
		void getChatRooms_includesPartnerAndLastMessage() {
			Member me = seedMemberCommitted("chatListMe");
			Member partner = seedMemberCommitted("chatListPartner");
			Long roomId = chatService.createChatRoomWithTransaction(me.getUserId(), partner.getUserId(), me.getUserId() + ":" + partner.getUserId());
			ChatDto chat = new ChatDto();
			chat.setRoomId(roomId);
			chat.setSenderId(me.getUserId());
			chat.setReceiverId(partner.getUserId());
			chat.setMessage("안녕하세요");
			chatService.saveChat(chat);

			List<ChatRoomListDto> result = chatService.getChatRooms(me.getUserId());

			assertThat(result).extracting(ChatRoomListDto::getRoomId).contains(roomId);
			ChatRoomListDto found = result.stream().filter(r -> r.getRoomId().equals(roomId)).findFirst().orElseThrow();
			assertThat(found.getPartner()).isEqualTo(partner.getUserName());
			assertThat(found.getMessage()).isEqualTo("안녕하세요");
		}

		@Test
		@DisplayName("두 번째 조회부터는 캐시된 값을 반환한다")
		void getChatRooms_secondCallUsesCache() {
			Member me = seedMemberCommitted("chatCacheMe");
			Member partner = seedMemberCommitted("chatCachePartner");
			chatService.createChatRoomWithTransaction(me.getUserId(), partner.getUserId(), me.getUserId() + ":" + partner.getUserId());

			List<ChatRoomListDto> first = chatService.getChatRooms(me.getUserId());

			@SuppressWarnings("unchecked")
			List<ChatRoomListDto> cached = (List<ChatRoomListDto>) redisTemplate.opsForValue().get("chat:list:" + me.getUserId());
			assertThat(cached).isNotNull();
			assertThat(chatService.getChatRooms(me.getUserId())).isEqualTo(first);
		}

		@Test
		@DisplayName("메시지를 저장하면 송신자/수신자의 채팅방 목록 캐시가 무효화된다")
		void saveChat_invalidatesCacheForBothUsers() {
			Member me = seedMemberCommitted("chatInvMe");
			Member partner = seedMemberCommitted("chatInvPartner");
			Long roomId = chatService.createChatRoomWithTransaction(me.getUserId(), partner.getUserId(), me.getUserId() + ":" + partner.getUserId());
			chatService.getChatRooms(me.getUserId());
			chatService.getChatRooms(partner.getUserId());
			assertThat(redisTemplate.opsForValue().get("chat:list:" + me.getUserId())).isNotNull();
			assertThat(redisTemplate.opsForValue().get("chat:list:" + partner.getUserId())).isNotNull();

			ChatDto chat = new ChatDto();
			chat.setRoomId(roomId);
			chat.setSenderId(me.getUserId());
			chat.setReceiverId(partner.getUserId());
			chat.setMessage("새 메시지");
			chatService.saveChat(chat);

			assertThat(redisTemplate.opsForValue().get("chat:list:" + me.getUserId())).isNull();
			assertThat(redisTemplate.opsForValue().get("chat:list:" + partner.getUserId())).isNull();
		}
	}

	@Nested
	@DisplayName("createChatRoomWithTransaction / getMessages / isMemberOfRoom")
	@Transactional
	class RoomAndMessages {

		@Test
		@DisplayName("같은 두 사용자로 다시 호출해도 새 방을 만들지 않고 기존 방을 반환한다")
		void createChatRoom_idempotentForSamePair() {
			Member a = seedMemberCommitted("chatDupA");
			Member b = seedMemberCommitted("chatDupB");

			Long first = chatService.createChatRoomWithTransaction(a.getUserId(), b.getUserId(), a.getUserId() + ":" + b.getUserId());
			Long second = chatService.createChatRoomWithTransaction(a.getUserId(), b.getUserId(), a.getUserId() + ":" + b.getUserId());

			assertThat(second).isEqualTo(first);
		}

		@Test
		@DisplayName("본인이 보낸 메시지는 mine=true로 표시된다")
		void getMessages_marksMineCorrectly() {
			Member me = seedMemberCommitted("chatMsgMe");
			Member partner = seedMemberCommitted("chatMsgPartner");
			Long roomId = chatService.createChatRoomWithTransaction(me.getUserId(), partner.getUserId(), me.getUserId() + ":" + partner.getUserId());

			ChatDto myMsg = new ChatDto();
			myMsg.setRoomId(roomId);
			myMsg.setSenderId(me.getUserId());
			myMsg.setReceiverId(partner.getUserId());
			myMsg.setMessage("내가 보낸 메시지");
			chatService.saveChat(myMsg);

			ChatDto partnerMsg = new ChatDto();
			partnerMsg.setRoomId(roomId);
			partnerMsg.setSenderId(partner.getUserId());
			partnerMsg.setReceiverId(me.getUserId());
			partnerMsg.setMessage("상대방이 보낸 메시지");
			chatService.saveChat(partnerMsg);

			List<ChatDto> messages = chatService.getMessages(roomId, me.getUserId());

			assertThat(messages).hasSize(2);
			assertThat(messages).filteredOn(m -> m.getSenderId().equals(me.getUserId()))
					.allMatch(ChatDto::isMine);
			assertThat(messages).filteredOn(m -> m.getSenderId().equals(partner.getUserId()))
					.noneMatch(ChatDto::isMine);
		}

		@Test
		@DisplayName("방 멤버가 아니면 메시지를 조회할 수 없다 (빈 목록)")
		void getMessages_notAMember_returnsEmpty() {
			Member a = seedMemberCommitted("chatAuthA");
			Member b = seedMemberCommitted("chatAuthB");
			Member stranger = seedMemberCommitted("chatAuthStranger");
			Long roomId = chatService.createChatRoomWithTransaction(a.getUserId(), b.getUserId(), a.getUserId() + ":" + b.getUserId());
			ChatDto msg = new ChatDto();
			msg.setRoomId(roomId);
			msg.setSenderId(a.getUserId());
			msg.setReceiverId(b.getUserId());
			msg.setMessage("비밀 메시지");
			chatService.saveChat(msg);

			List<ChatDto> messages = chatService.getMessages(roomId, stranger.getUserId());

			assertThat(messages).isEmpty();
		}

		@Test
		@DisplayName("isMemberOfRoom - 실제 멤버 여부를 정확히 판단한다")
		void isMemberOfRoom_accurate() {
			Member a = seedMemberCommitted("chatMemA");
			Member b = seedMemberCommitted("chatMemB");
			Member stranger = seedMemberCommitted("chatMemStranger");
			Long roomId = chatService.createChatRoomWithTransaction(a.getUserId(), b.getUserId(), a.getUserId() + ":" + b.getUserId());

			assertThat(chatService.isMemberOfRoom(a.getUserId(), roomId)).isTrue();
			assertThat(chatService.isMemberOfRoom(stranger.getUserId(), roomId)).isFalse();
		}
	}

	@Nested
	@DisplayName("ChatRoomFacade 동시성")
	class Concurrency {

		@Autowired
		private org.springframework.transaction.PlatformTransactionManager transactionManager;

		private Member userA;
		private Member userB;
		private Long createdRoomId;

		// 워커 스레드들이 테스트 메서드의 트랜잭션 밖에서 각자 커밋하므로, 정리도 별도 트랜잭션에서 수행한다.
		@AfterEach
		void cleanUp() {
			if (userA == null || userB == null) return;
			new org.springframework.transaction.support.TransactionTemplate(transactionManager).execute(status -> {
				if (createdRoomId != null) {
					entityManager.createNativeQuery("DELETE FROM chat_message WHERE room_id = :roomId")
							.setParameter("roomId", createdRoomId)
							.executeUpdate();
					entityManager.createNativeQuery("DELETE FROM chat_room_user WHERE room_id = :roomId")
							.setParameter("roomId", createdRoomId)
							.executeUpdate();
					entityManager.createNativeQuery("DELETE FROM chat_room WHERE room_id = :roomId")
							.setParameter("roomId", createdRoomId)
							.executeUpdate();
				}
				entityManager.createNativeQuery("DELETE FROM member WHERE user_id IN (:a, :b)")
						.setParameter("a", userA.getUserId())
						.setParameter("b", userB.getUserId())
						.executeUpdate();
				return null;
			});
		}

		@Test
		@DisplayName("여러 스레드가 동시에 같은 두 사용자의 채팅방을 요청해도 방은 하나만 생성된다")
		void concurrentRequests_createOnlyOneRoom() throws InterruptedException {
			userA = seedMemberCommitted("chatRaceA");
			userB = seedMemberCommitted("chatRaceB");

			int threadCount = 10;
			ExecutorService executor = Executors.newFixedThreadPool(threadCount);
			CountDownLatch readyLatch = new CountDownLatch(threadCount);
			CountDownLatch startLatch = new CountDownLatch(1);

			List<java.util.concurrent.Future<Long>> futures = IntStream.range(0, threadCount)
					.mapToObj(i -> executor.submit(() -> {
						readyLatch.countDown();
						startLatch.await();
						return chatRoomFacade.getOrCreateChatRoomId(userA.getUserId(), userB.getUserId());
					}))
					.collect(Collectors.toList());

			readyLatch.await(5, TimeUnit.SECONDS);
			startLatch.countDown();
			executor.shutdown();
			executor.awaitTermination(15, TimeUnit.SECONDS);

			Set<Long> distinctRoomIds = futures.stream().map(f -> {
				try {
					return f.get();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}).collect(Collectors.toSet());

			assertThat(distinctRoomIds).hasSize(1);
			createdRoomId = distinctRoomIds.iterator().next();

			Long count = ((Number) entityManager.createNativeQuery(
					"SELECT COUNT(*) FROM chat_room_user WHERE user_id = :a AND room_id IN "
							+ "(SELECT room_id FROM chat_room_user WHERE user_id = :b)")
					.setParameter("a", userA.getUserId())
					.setParameter("b", userB.getUserId())
					.getSingleResult()).longValue();
			assertThat(count).isEqualTo(1);
		}
	}
}
