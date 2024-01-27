package com.jam.client.message.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.jam.client.message.service.MessageService;
import com.jam.client.message.vo.MessageVO;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j;

@RestController
@RequestMapping("/api/message")
@AllArgsConstructor
@Log4j
public class MessageRestController {
	
	@Autowired
	private MessageService messageService;

	
	/*******************************
	 * 쪽지 전송
	 * @param MessageVO message_vo
	 * @return 쪽지 전송 결과 
	 ****************************/
	@RequestMapping(value="/messageWrite", method=RequestMethod.POST)
	public ResponseEntity<String> messageWrite(@RequestBody MessageVO message) {

		String msg;
	
		if (message == null)  msg = "MessageVO is null.";
	    else if (message.getReceiver() == null) msg = "Receiver is null.";
	    else if (message.getReceiver_id() == null)msg = "Receiver id is null.";
	    else if (message.getSender() == null) msg = "Sender is null.";
	    else if (message.getSender_id() == null) msg = "Sender id is null.";
	    else if (message.getMessage_title() == null) msg = "Message title is null.";
	    else if (message.getMessage_contents() == null) msg = "Message contents is null.";
	    else {
	        try {
	            messageService.messageWrite(message);
	            return ResponseEntity.ok("Message written successfully.");
	        } catch (Exception e) {
	        	log.error("쪽지 보내는 중 오류: " + e.getMessage());
	            return ResponseEntity.internalServerError().body(e.getMessage());
	        }
	    }

	    return new ResponseEntity<>(msg, HttpStatus.BAD_REQUEST);
		
	}
	
	
	/***********************************
	 * @param MessageVO message_vo
	 * @return 받은 쪽지 상세 페이지
	 ***********************************/
	@PostMapping(value = "/receiveMsgDetail", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<MessageVO> receiveMessageDetailPage(@RequestBody MessageVO message) {

		Long message_no = message.getMessage_no();
		String receiver_id = message.getReceiver_id();
		
		log.info("message_no : "+message_no);
;		if(message_no == null) {
			log.error("message_no is required.");
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}else if(receiver_id == null) {
			log.error("receiver_id is required.");
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		
		try {
			MessageVO detail = messageService.receiveMsgDetail(message_no);
			
			// 로그인한 사용자와 받는 회원의 아이디가 같은지 확인
			if(!receiver_id.equals(detail.getReceiver_id())) {
				log.error("This member does not have permission for this message.");
				return new ResponseEntity<>(HttpStatus.FORBIDDEN);
			}
			
			// 쪽지 상태 읽음으로 변경
			if(detail.getRead_chk() == 0) messageService.message_read(message_no);
			
			return new ResponseEntity<>(detail, HttpStatus.OK);
		}catch(Exception e) {
			log.error("받은 쪽지 가져오는 중 오류: " + e.getMessage());
			
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
	}
	
	/******************************
	 * @param MessageVO message_vo
	 * @return 보낸 쪽지 상세페이지
	 ******************************/
	@PostMapping(value = "/sendMessageDetail", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<MessageVO> sendMessageDetailPage(@RequestBody MessageVO message) {
		
		
		Long message_no = message.getMessage_no();
		String sender_id = message.getSender_id();
		
		if(message_no == null) {
			log.error("message_no is required.");
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}else if(sender_id == null) {
			log.error("sender_id is required.");
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		
		try {
			MessageVO detail = messageService.sendMsgDetail(message.getMessage_no());
			
			// 로그인한 사용자가 쪽지를 보낸 회원과 같은지 확인
			if(!sender_id.equals(detail.getSender_id())) {
				log.error("This member does not have permission for this message.");
				return new ResponseEntity<>(HttpStatus.FORBIDDEN);
			}
			return new ResponseEntity<>(detail, HttpStatus.OK);
		}catch(Exception e) {
			
			log.error("보낸 쪽지 가져오는 중 오류: "+ e.getMessage());
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		
		
	}
	
}
