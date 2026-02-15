package com.jam.chat.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.jam.chat.dto.ChatDto;
import com.jam.chat.dto.ChatRoomListDto;

public interface ChatMapper {

	List<ChatRoomListDto> getChatRooms(@Param("userId") String userId);
	
	Long getChatRoomId(@Param("userId") String userId, @Param("targetUserId") String targetUserId);

	Long nextChatRoomId();
	
	Long createChatRoomId(@Param("roomId") Long roomId, @Param("pairKey") String pairKey);

	void insertChatRoomUser(@Param("roomId") Long roomId, @Param("userId") String userId);

	Map<String, String> getChatPartner(@Param("roomId") Long roomId, @Param("userId") String userId);

	Long saveChat(ChatDto chat);

	List<ChatDto> getMessages(@Param("roomId") Long roomId, @Param("userId") String userId);

	int isMemberOfRoom(@Param("roomId") Long roomId, @Param("userId") String userId);
}
