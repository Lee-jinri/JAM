package com.jam.client.chat.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/chat")
public class ChatController {
	
	
	// 채팅 페이지
	@GetMapping("/chatRooms")
	public String chatRoomsPage() {
		
		return "chat/chatRooms";
	}
	@GetMapping("")
	public String chatPage() {
		
		return "fleaMarket/chat";
	}
}
