package com.jam.chat.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import com.jam.global.exception.BadRequestException;
import com.jam.member.service.MemberService;

/**
 * ChatRoomFacade에 대한 Mockito 단위 테스트. RedissonClient/RLock을 목으로 대체해 락 흐름을 검증한다.
 * 실제 동시성(여러 스레드가 동시에 락을 다투는 상황)은 통합테스트에서 별도로 검증한다.
 */
@ExtendWith(MockitoExtension.class)
class ChatRoomFacadeTest {

	@Mock
	private ChatService chatService;
	@Mock
	private MemberService memberService;
	@Mock
	private RedissonClient redissonClient;
	@Mock
	private RLock lock;

	@InjectMocks
	private ChatRoomFacade chatRoomFacade;

	@AfterEach
	void clearInterruptFlag() {
		Thread.interrupted(); // 인터럽트 테스트로 세팅된 플래그가 다른 테스트에 영향 주지 않도록 정리
	}

	@Nested
	@DisplayName("getOrCreateChatRoomId")
	class GetOrCreateChatRoomId {

		@Test
		@DisplayName("상대방이 탈퇴한 사용자면 BadRequestException을 던지고 락을 시도하지 않는다")
		void targetInactive_throwsBadRequest() {
			given(memberService.isActiveUser("target1")).willReturn(false);

			assertThatThrownBy(() -> chatRoomFacade.getOrCreateChatRoomId("user1", "target1"))
					.isInstanceOf(BadRequestException.class);

			verify(redissonClient, never()).getLock(anyString());
		}

		@Test
		@DisplayName("이미 방이 있으면 락을 걸지 않고 바로 반환한다")
		void existingRoom_skipsLock() {
			given(memberService.isActiveUser("target1")).willReturn(true);
			given(chatService.getChatRoomId("user1", "target1")).willReturn(10L);

			Long roomId = chatRoomFacade.getOrCreateChatRoomId("user1", "target1");

			assertThat(roomId).isEqualTo(10L);
			verify(redissonClient, never()).getLock(anyString());
			verify(chatService, never()).createChatRoomWithTransaction(any(), any(), any());
		}

		@Test
		@DisplayName("방이 없으면 락을 획득해 방을 생성하고, 끝나면 락을 해제한다")
		void noExistingRoom_acquiresLockAndCreates() throws InterruptedException {
			given(memberService.isActiveUser("target1")).willReturn(true);
			given(chatService.getChatRoomId("user1", "target1")).willReturn(null);
			given(redissonClient.getLock(anyString())).willReturn(lock);
			given(lock.tryLock(5, TimeUnit.SECONDS)).willReturn(true);
			given(lock.isHeldByCurrentThread()).willReturn(true);
			given(chatService.createChatRoomWithTransaction(eq("user1"), eq("target1"), anyString())).willReturn(30L);

			Long roomId = chatRoomFacade.getOrCreateChatRoomId("user1", "target1");

			assertThat(roomId).isEqualTo(30L);
			verify(lock).unlock();
		}

		@Test
		@DisplayName("사용자 순서와 무관하게 정렬된 동일한 pairKey/lockKey를 사용한다")
		void pairKey_isOrderIndependent() throws InterruptedException {
			given(memberService.isActiveUser(anyString())).willReturn(true);
			given(chatService.getChatRoomId(any(), any())).willReturn(null);
			given(redissonClient.getLock(anyString())).willReturn(lock);
			given(lock.tryLock(5, TimeUnit.SECONDS)).willReturn(true);
			given(lock.isHeldByCurrentThread()).willReturn(true);

			chatRoomFacade.getOrCreateChatRoomId("bbb", "aaa");

			ArgumentCaptor<String> lockKeyCaptor = ArgumentCaptor.forClass(String.class);
			verify(redissonClient).getLock(lockKeyCaptor.capture());
			assertThat(lockKeyCaptor.getValue()).isEqualTo("lock:chatroom:aaa:bbb");

			ArgumentCaptor<String> pairKeyCaptor = ArgumentCaptor.forClass(String.class);
			verify(chatService).createChatRoomWithTransaction(eq("bbb"), eq("aaa"), pairKeyCaptor.capture());
			assertThat(pairKeyCaptor.getValue()).isEqualTo("aaa:bbb");
		}

		@Test
		@DisplayName("락 획득에 실패하면(타임아웃) 예외를 던지고 방을 생성하지 않는다")
		void lockTimeout_throwsAndDoesNotCreate() throws InterruptedException {
			given(memberService.isActiveUser("target1")).willReturn(true);
			given(chatService.getChatRoomId("user1", "target1")).willReturn(null);
			given(redissonClient.getLock(anyString())).willReturn(lock);
			given(lock.tryLock(5, TimeUnit.SECONDS)).willReturn(false);
			given(lock.isHeldByCurrentThread()).willReturn(false);

			assertThatThrownBy(() -> chatRoomFacade.getOrCreateChatRoomId("user1", "target1"))
					.isInstanceOf(RuntimeException.class)
					.hasMessage("잠시 후 다시 시도해주세요.");

			verify(chatService, never()).createChatRoomWithTransaction(any(), any(), any());
			verify(lock, never()).unlock();
		}

		@Test
		@DisplayName("락 대기 중 인터럽트되면 인터럽트 상태를 복구하고 예외를 던진다")
		void interruptedWhileWaitingForLock_restoresInterruptAndThrows() throws InterruptedException {
			given(memberService.isActiveUser("target1")).willReturn(true);
			given(chatService.getChatRoomId("user1", "target1")).willReturn(null);
			given(redissonClient.getLock(anyString())).willReturn(lock);
			given(lock.tryLock(5, TimeUnit.SECONDS)).willThrow(new InterruptedException());
			given(lock.isHeldByCurrentThread()).willReturn(false);

			assertThatThrownBy(() -> chatRoomFacade.getOrCreateChatRoomId("user1", "target1"))
					.isInstanceOf(RuntimeException.class)
					.hasMessage("시스템 오류입니다. 잠시 후 다시 시도해 주세요.");

			assertThat(Thread.currentThread().isInterrupted()).isTrue();
		}
	}
}
