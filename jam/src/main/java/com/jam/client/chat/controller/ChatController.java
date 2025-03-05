package com.jam.client.chat.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.jam.client.chat.service.ChatService;

@Controller
@RequestMapping("/chat")
public class ChatController {
	
	
	// 채팅 페이지
	@RequestMapping(value="/chatRooms", method=RequestMethod.GET)
	public String chatRoomsPage() {
		
		return "chat/chatRooms";
	}
	
	@RequestMapping(value="/chat", method=RequestMethod.GET)
	public String chatPage() {
		
		return "chat/chat";
	}
}
