package com.jam.client.chat.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.jam.client.chat.vo.ChatRoomListVO;
import com.jam.client.chat.vo.ChatVO;

public interface ChatDAO {

	List<ChatRoomListVO> getChatRooms(@Param("userId") String userId);
	
	Long getChatRoomId(@Param("userId") String userId, @Param("targetUserId") String targetUserId);

	Long nextChatRoomId();
	
	Long createChatRoomId(@Param("roomId") Long roomId, @Param("pairKey") String pairKey);

	void insertChatRoomUser(@Param("roomId") Long roomId, @Param("userId") String userId);

	Map<String, String> getChatPartner(@Param("roomId") Long roomId, @Param("userId") String userId);

	Long saveChat(ChatVO chat);

	List<ChatVO> getMessages(@Param("roomId") Long roomId, @Param("userId") String userId);

	int isMemberOfRoom(@Param("roomId") Long roomId, @Param("userId") String userId);
}
