package com.jam.client.chat.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jam.client.chat.dao.ChatDAO;
import com.jam.client.chat.vo.ChatRoomListVO;
import com.jam.client.chat.vo.ChatVO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {
    
	private final ChatDAO chatDao;
    private final RedisTemplate<String, Object> redisTemplate;
    
	@Override
	public List<ChatRoomListVO> getChatRooms(String userId) {
		List<ChatRoomListVO> chatList = new ArrayList<>();
		String key = "chat:list:" + userId;
		
	    @SuppressWarnings("unchecked")
		List<ChatRoomListVO> cachedList = (List<ChatRoomListVO>) redisTemplate.opsForValue().get(key);

	    if (cachedList != null) {
	        return cachedList;
	    }
	    
		chatList = chatDao.getChatRooms(userId);

        redisTemplate.opsForValue().set(key, chatList, 10, TimeUnit.MINUTES);
		
		return chatList;
	}

    // 채팅방 Id 조회
 	@Override
 	@Transactional
 	public Long createChatRoomWithTransaction(String userId, String targetUserId, String pairKey) {
 		// 락 획득 후 로직 수행 이미 방이 있는지 다시 확인
        Long roomId = chatDao.getChatRoomId(userId, targetUserId);
        if (roomId == null) {
            roomId = chatDao.nextChatRoomId();
            chatDao.createChatRoomId(roomId, pairKey);
            chatDao.insertChatRoomUser(roomId, userId);
            chatDao.insertChatRoomUser(roomId, targetUserId);
            log.info("새로운 채팅방 생성 완료: {}", roomId);
        }
		return roomId;
	}
	
	@Override
	public Map<String, String> getChatPartner(Long roomId, String userId) {
		
		Map<String, String> result = new HashMap<>();
		result = chatDao.getChatPartner(roomId, userId);
		
		return result;
	}
	
	@Override
	public void saveChat(ChatVO chat) {
		chat.setSentAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
		chatDao.saveChat(chat);
		
		redisTemplate.delete("chat:list:" + chat.getSenderId());
		redisTemplate.delete("chat:list:" + chat.getReceiverId());
	}
	
	@Override
	public List<ChatVO> getMessages(Long roomId, String userId) {
		// FIXME: 페이징 추가
		List<ChatVO> messages = chatDao.getMessages(roomId, userId);

    	for (ChatVO message : messages) {
    		message.setMine(message.getSenderId().equals(userId));
        }
    	
		return messages;
	}

	// 채팅방 멤버십 확인
	@Override
	public boolean isMemberOfRoom(String userId, Long roomId) {
		boolean isMember = chatDao.isMemberOfRoom(roomId, userId) > 0;
		return isMember;
	}
}
