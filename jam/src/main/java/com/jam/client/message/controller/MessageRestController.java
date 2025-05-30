package com.jam.client.message.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.jam.client.member.service.MemberService;
import com.jam.client.message.service.MessageService;
import com.jam.client.message.vo.MessageVO;
import com.jam.global.jwt.JwtTokenProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;

@RestController
@RequestMapping("/api/message")
@RequiredArgsConstructor 
@Log4j
public class MessageRestController {
	
	private final MessageService messageService;
	private final MemberService memberService;
	
	/*******************************
	 * 쪽지 전송
	 * @param MessageVO message_vo
	 * @return 쪽지 전송 결과 
	 ****************************/
	@RequestMapping(value="/send", method=RequestMethod.POST)
	public ResponseEntity<String> messageWrite(@RequestBody MessageVO message, HttpServletRequest request) {

		String validationError  = validateMessage(message);
		
		if (validationError != null) {
	        return ResponseEntity.badRequest().body(validationError);
	    }
		
	    try {
	    	String user_id = (String)request.getAttribute("userId");
	    		
	    	if(user_id != null) {
	    		message.setSender_id(user_id);	
	    	}else return ResponseEntity.status(401).body("Authentication failed");
	    		
	    	String receiver_id = memberService.getUserId(message.getReceiver());	

	    	if(receiver_id == "" || receiver_id == null) return ResponseEntity.badRequest().body("Receiver ID not found."); 
	    	
	    	message.setReceiver_id(receiver_id);
	    	
            messageService.send(message);
	            
            return ResponseEntity.ok("Message written successfully.");
        } catch (Exception e) {
        	log.error("쪽지 보내는 중 오류: " + e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    

	}
	
	/*******************************
	 * 메시지 유효성 검증
	 * @param MessageVO message
	 * @return 에러 메시지 or null
	 ******************************/
	private String validateMessage(MessageVO message) {
	    if (message == null) {
	        return "MessageVO is null.";
	    }
	    if (message.getReceiver() == null) {
	        return "Receiver is null.";
	    }
	    if (message.getMessage_title() == null) {
	        return "Message title is null.";
	    }
	    if (message.getMessage_contents() == null) {
	        return "Message contents is null.";
	    }
	    return null; 
	}

	
	
}
