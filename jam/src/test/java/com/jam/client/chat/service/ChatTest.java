package com.jam.client.chat.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.jam.client.chat.dao.ChatDAO;
import com.jam.client.chat.vo.ChatRoomListVO;
import com.jam.client.chat.vo.ChatVO;
import com.jam.config.RootConfig;

import lombok.extern.slf4j.Slf4j;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = RootConfig.class)
@Slf4j
public class ChatTest {
	
	@Autowired
	private ChatDAO chatDao;
	
	
	@Test
	public void testGetChatRooms() {
		String userId = "abcd1234";
		List<ChatRoomListVO> chatList = new ArrayList<>();
		chatList = chatDao.getChatRooms(userId);
		
		log.info("chatList = {}", chatList);
	}/*
	
	@Test
	public void testGetOrCreateChatRoomId() {
		String userId = "abcd1234";
		String targetUserId = "asdf1234";
		Long roomId = chatDao.getChatRoomId(userId, targetUserId);
		
		log.info("roomId = {}", roomId);
		
		if (roomId == null) {
 			// 채팅방 생성
			roomId = chatDao.nextChatRoomId();
			log.info("createdRoomId = {}", roomId);
				
 			chatDao.createChatRoomId(roomId);
 			chatDao.insertChatRoomUser(roomId, userId);
 			chatDao.insertChatRoomUser(roomId, targetUserId);
 		}
	}
	
	@Test
	public void testGetChatPartner() {
		String userId = "abcd1234";
		Long roomId = 1L;
		
		Map<String, String> result = new HashMap<>();
		result = chatDao.getChatPartner(roomId, userId);
		
		log.info("chatPartner = {}", result);
	}
	
	@Test
	public void testSaveChat() {
		ChatVO chat = new ChatVO();
		chat.setMessage("메시지1");
		chat.setRoomId(1L);
		chat.setSenderId("abcd1234");
		
		chatDao.saveChat(chat);
	}
	
	@Test
	public void testGetMessages() {
		String userId = "abcd1234";
		Long roomId = 1L;
		List<ChatVO> messages = chatDao.getMessages(roomId, userId);
		
		log.info("messages = {}", messages);
	}*/
}
