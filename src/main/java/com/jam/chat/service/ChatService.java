package com.jam.chat.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jam.chat.dto.ChatDto;
import com.jam.chat.dto.ChatRoomListDto;
import com.jam.chat.mapper.ChatMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatService {

	private final ChatMapper chatMapper;
    //private final RedisTemplate<String, Object> redisTemplate;
    
	public List<ChatRoomListDto> getChatRooms(String userId) {
		List<ChatRoomListDto> chatList = new ArrayList<>();
		String key = "chat:list:" + userId;
		
		/*
	    @SuppressWarnings("unchecked")
		List<ChatRoomListDto> cachedList = (List<ChatRoomListDto>) redisTemplate.opsForValue().get(key);

	    if (cachedList != null) {
	        return cachedList;
	    }
	    */
		chatList = chatMapper.getChatRooms(userId);

        //redisTemplate.opsForValue().set(key, chatList, 10, TimeUnit.MINUTES);
		
		return chatList;
	}

    // 채팅방 Id 조회
 	@Transactional
 	public Long createChatRoomWithTransaction(String userId, String targetUserId, String pairKey) {
 		// 락 획득 후 로직 수행 이미 방이 있는지 다시 확인
        Long roomId = chatMapper.getChatRoomId(userId, targetUserId);
        if (roomId == null) {
            roomId = chatMapper.nextChatRoomId();
            chatMapper.createChatRoomId(roomId, pairKey);
            chatMapper.insertChatRoomUser(roomId, userId);
            chatMapper.insertChatRoomUser(roomId, targetUserId);
            log.info("새로운 채팅방 생성 완료: {}", roomId);
        }
		return roomId;
	}
	
	public Map<String, String> getChatPartner(Long roomId, String userId) {
		
		Map<String, String> result = new HashMap<>();
		result = chatMapper.getChatPartner(roomId, userId);
		
		return result;
	}
	
	public void saveChat(ChatDto chat) {
		chat.setSentAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
		chatMapper.saveChat(chat);
		
		//redisTemplate.delete("chat:list:" + chat.getSenderId());
		//redisTemplate.delete("chat:list:" + chat.getReceiverId());
	}
	
	public List<ChatDto> getMessages(Long roomId, String userId) {
		// FIXME: 페이징 추가
		List<ChatDto> messages = chatMapper.getMessages(roomId, userId);

    	for (ChatDto message : messages) {
    		message.setMine(message.getSenderId().equals(userId));
        }
    	
		return messages;
	}

	// 채팅방 멤버십 확인
	public boolean isMemberOfRoom(String userId, Long roomId) {
		boolean isMember = chatMapper.isMemberOfRoom(roomId, userId) > 0;
		return isMember;
	}
}
