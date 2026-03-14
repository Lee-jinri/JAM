package com.jam.chat.service;

import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import com.jam.global.exception.BadRequestException;
import com.jam.member.service.MemberService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatRoomFacade {

	private final ChatService chatService;
	private final MemberService memberService;
	private final RedissonClient redissonClient;
    
	// 채팅방 Id 조회
	public Long getOrCreateChatRoomId(String userId, String targetUserId) {
		
		String firstUser = userId.compareTo(targetUserId) < 0 ? userId : targetUserId;
		String secondUser = userId.compareTo(targetUserId) < 0 ? targetUserId : userId;
		String pairKey = firstUser + ":" + secondUser;
		String lockKey = "lock:chatroom:" + pairKey;
		
		RLock lock = redissonClient.getLock(lockKey);
		 
		try {
			// 락 획득 시도
			boolean isLocked = lock.tryLock(5, TimeUnit.SECONDS);

			if (!isLocked) {
				log.warn("락 획득 실패: {} & {}", userId, targetUserId);
				throw new RuntimeException("잠시 후 다시 시도해주세요.");
			}
			
			boolean isTargetActive = memberService.isActiveUser(targetUserId); 
	        if (!isTargetActive) {
	            throw new BadRequestException("상대방이 탈퇴하여 채팅을 시작할 수 없습니다.");
	        }
	        
			return chatService.createChatRoomWithTransaction(userId, targetUserId, pairKey);
		     
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException("시스템 오류입니다. 잠시 후 다시 시도해 주세요.");
		} finally {
			// 락 해제
			if (lock.isHeldByCurrentThread()) {
				lock.unlock();
			}
		}
	}
}
