package com.jam.client.chat.service;

import java.util.List;
import java.util.Map;

import com.jam.client.chat.vo.ChatRoomListVO;
import com.jam.client.chat.vo.ChatVO;

public interface ChatService {

	List<ChatRoomListVO> getChatRooms(String userId);

	Long createChatRoomWithTransaction(String userId, String targetUserId, String pairKey);
	
	List<ChatVO> getMessages(Long roomId, String userId);
		
	Map<String, String> getChatPartner(Long chatRoomId, String userId);
	
	void saveChat(ChatVO chat);
	
	boolean isMemberOfRoom(String userId, Long chatRoomId);

}
