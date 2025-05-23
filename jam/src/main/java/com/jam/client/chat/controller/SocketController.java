package com.jam.client.chat.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.jam.client.chat.vo.ChatVO;

import lombok.extern.log4j.Log4j;

@Controller
@Log4j
public class SocketController {

	
	@PostMapping("/send")
	public void send(@RequestBody ChatVO chat) {
		log.info("chat : "  + chat);
		log.info("hi");
	}
	// 메시지 전송
	@MessageMapping(value = "/abcd/room123")
	public void message(ChatVO message) {
		log.info("웨 안되.");
		//messagingTemplate.convertAndSend("/sub/chatroom/room123", message);
    
		
	}
	
	
	    public ChatVO sendMessage(ChatVO chatMessage) {
			System.out.println("?");
			log.info("이건 나오나?");
			
			// 메시지를 Redis에 저장
	        //chatService.addMessageToChatRoom(chatMessage.getChatRoomId().toString(), chatMessage);

	        // 클라이언트로부터 메시지를 받아 처리하고, 처리된 메시지를 '/sub/chatroom'을 구독하는 모든 클라이언트에게 전송
			// 동적 경로(/sub/chatroom/{chatRoomId})로  메시지 전송
	        //String destination = "/sub/chatroom/" + chatMessage.getChatRoomId();
	        //messagingTemplate.convertAndSend(destination, chatMessage);
			
			return chatMessage;
					
	    }
}
