package com.jam.client.chat.service;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Pageable;

import com.jam.client.chat.vo.ChatVO;

public interface ChatService {
	
	List<ChatVO> getMessages(String chatRoomId, Pageable pageable);
		
	void addParticipant(String chatRoomId, String participant, String userId);

	Map<String, String> getChatPartner(String chatRoomId, String userId);

	String getUserNameFromRedis(String userId);
	
	List<ChatVO> getChatRooms(String userId);
	
	void saveChat(ChatVO chat);

	String getChatRoomId(String userId, String targetUserId);
	
	boolean ensureRoomOnFirstMessage(String chatRoomId, String userId, String partnerId);

	boolean isMemberOfRoom(String userId, String chatRoomId);

	boolean roomExists(String chatRoomId);
}
