package com.jam.client.chat.controller;

import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jam.client.chat.service.ChatService;
import com.jam.client.chat.vo.ChatVO;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;

@RestController
@RequestMapping("/api/chat")
@Log4j
@RequiredArgsConstructor
public class ChatRestController {
	
	private final ChatService chatService;
	
	/* 채팅방에 입장하면 채팅 내용 불러옴 */
    @GetMapping("/messages")
    public ResponseEntity<List<ChatVO>> messages(@RequestParam String chatRoomId, HttpServletRequest request) {
        
    	try {
    		// page, size
        	Pageable pageable = PageRequest.of(1, 100);
        	        
        	List<ChatVO> messages = chatService.getMessages(chatRoomId, pageable);
        	
        	String userId = (String) request.getAttribute("userId");
        	for (ChatVO message : messages) {
        		message.setMine(message.getSenderId().equals(userId));
            }
        	
            return ResponseEntity.ok(messages);
    	}catch(Exception e){
    		log.error(e.getMessage());
    	}
		return null;    
    }
    
    @GetMapping(value="/chatRoomId")
    public String getChatRoomId(@RequestParam String targetUserName, HttpServletRequest request) {
    	
    	String targetUserId = chatService.getTargetUserId(targetUserName);
		String userId = (String)request.getAttribute("userId");
				
		// 사용자, 상대방 아이디 없을 때 예외 처리 해야함 
		String chatRoomId = chatService.getChatRoomId(userId, targetUserId); 
		
		log.info(chatRoomId);
		
    	return chatRoomId;
    }
    
    @GetMapping(value="/chatRooms")
    public ResponseEntity<List<ChatVO>> getChatRooms(HttpServletRequest request){
    	
        try {
        	String userId = (String) request.getSession().getAttribute("userId");
    		List<ChatVO> chatRooms = chatService.getChatRooms(userId);
			
			return ResponseEntity.ok().body(chatRooms);
		}catch(Exception e) {
			log.error(e.getMessage());
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
    }
    
    // 사용자 채팅방 목록 추가
 	@PostMapping(value = "/addchatRooms", produces = "application/json")
     public ResponseEntity<String> addUserToChatRoom(@RequestBody ChatVO chat) {
 		try {
 			String senderId = chat.getSenderId();
 			String receiverId = chat.getReceiverId();
 			
 			// 채팅방 id 랜덤으로 생성
 			//이거 변경 
 			String chatRoomId = UUID.randomUUID().toString();
 			
 			chatService.addUserToChatRoom(senderId, receiverId, chatRoomId);
 			
 			return new ResponseEntity<>(HttpStatus.OK);
 		}catch(Exception e) {
 			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
 		}
 	}
    
 	// 발신자와 수신자 함께하는 채팅방이 존재하는지 확인
 	@GetMapping(value="/commonRooms")
 	public ResponseEntity<String> getCommonChatRooms(@RequestParam String userId1, @RequestParam String userId2) {
 		boolean existence = chatService.getCommonChatRooms(userId1, userId2);
 		
 		if(existence) return new ResponseEntity<>("existence", HttpStatus.OK);
 		else return new ResponseEntity<>("nonexistence",HttpStatus.OK);
 	}

    /*
	@GetMapping(value = "/chatRooms", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<ChatVO> getRoomsWithDetails(HttpServletRequest request) {
		String userId = (String) request.getSession().getAttribute("userId");
		
		List<ChatVO> getRoomWithDetails = chatService.getRoomsWithDetails(userId);
		
		log.info("chatController : " + getRoomWithDetails);
		
		return getRoomWithDetails;
	}*/
	
}
