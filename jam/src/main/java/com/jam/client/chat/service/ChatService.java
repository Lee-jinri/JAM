package com.jam.client.chat.service;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.jam.client.chat.vo.ChatVO;

public interface ChatService {
	
	// 사용자 채팅방 목록 추가
	void addUserToChatRoom(String senderId, String receiverId, String chatRoomId);
		
	// 채팅방에 채팅 정보 추가 (메시지 전송)
	void addMessageToChatRoom(String chatRoomId, ChatVO chat);

	// 상대방 닉네임으로 아이디 가져오기
	String getTargetUserId(String userName);
	
	// 사용자 채팅방 목록과 마지막 메시지, 상대방 Id 조회
	List<ChatVO> getChatRooms(String userId);
	
	// 발신자와 수신자 함께하는 채팅방이 존재하는지 확인
	boolean getCommonChatRooms(String userId1, String userId2);
	
	void saveChat(ChatVO chat);
	
	ChatVO convertToChatMessageVO(String payload);

	String getChatRoomId(String userId, String targetUserId);
	
	// 발신자와 수신자 함께하는 채팅방이 존재하는지 확인 후 존재하면 채팅 가져옴
	List<ChatVO> getMessages(String chatRoomId, Pageable pageable);

	// 팝업에서 채팅방 
	List<ChatVO> getMessagesByRoomId(Long chatRoomId);

	List<String> getChatMessages(String chatRoomId, Pageable pageable);

	void addParticipant(String chatRoomId, String participant, String userId);

	String getParticipant(String chatRoomId, String userId);

}
