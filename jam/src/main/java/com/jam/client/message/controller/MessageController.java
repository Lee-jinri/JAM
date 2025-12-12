package com.jam.client.message.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.jam.client.message.vo.MessageVO;
import com.jam.common.vo.PageDTO;
import com.jam.client.message.service.MessageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/message")
@RequiredArgsConstructor
@Slf4j
public class MessageController {
	
	private final MessageService messageService;
	
	/***********************************************
	 * @return 받은 쪽지 목록 페이지
	 ***********************************************/
	@RequestMapping(value = "/receiveMessage", method = RequestMethod.GET)
	public String receiveMessage(@ModelAttribute MessageVO message, Model model, HttpServletRequest request) {
		
		
	    try {
	    	String receiverId = (String)request.getAttribute("userId");
	    	
	        if (receiverId == null) {
	            return "member/login";
	        }

	        message.setReceiver_id(receiverId);

	        List<MessageVO> receiveMsg = messageService.receiveMessage(message);
	        model.addAttribute("receiveMsg", receiveMsg);

	        int total = messageService.receiveListCnt(message);
	        model.addAttribute("pageMaker", new PageDTO(message, total));

	        return "message/receiveMessageList";
	    } catch (Exception e) {
	        log.error(e.getMessage());
	        return "member/login";
	    }
	}
	
	/***********************************************
	 * @return 보낸 쪽지 목록 페이지
	 ***********************************************/
	@RequestMapping(value = "/sendMessage", method = RequestMethod.GET)
	public String sendMessage(@ModelAttribute MessageVO message, Model model, HttpServletRequest request) {
	    
	    try {
	    	String sender_id = (String)request.getAttribute("userId");
	    	

	        if (sender_id == null) {
	            return "member/login";
	        }

	        message.setSender_id(sender_id);

	        List<MessageVO> sendMsg = messageService.sendMessage(message);
	        model.addAttribute("sendMsg", sendMsg);

	        int total = messageService.sendListCnt(message);
	        model.addAttribute("pageMaker", new PageDTO(message, total));

	        return "message/sendMessageList";
	    } catch (Exception e) {
	        log.error(e.getMessage());
	        return "member/login";
	    }
	}
	
	
	/***********************************
	 * @param MessageVO message_vo
	 * @return 받은 쪽지 상세 페이지
	 ***********************************/
	@RequestMapping(value="/receiveMsgDetail/{message_no}", method=RequestMethod.GET)
	public String receiveMessageDetailPage(@PathVariable("message_no") Long message_no, Model model, HttpServletRequest request) {
		
		String receiver_id = (String)request.getAttribute("userId");
		
		if(message_no == null) {
			log.error("receiveMessageDetailPage message_no is required.");
		}else if(receiver_id == null) {
			log.error("receiver_id is required.");
		}
		
		try {
			
			MessageVO detail = messageService.receiveMsgDetail(message_no, receiver_id);
			
			// 쪽지 상태 읽음으로 변경
			if(detail.getRead_chk() == 0) messageService.message_read(message_no);
			
			model.addAttribute("receiveDetail", detail);
			
		}catch(Exception e) {
			log.error("받은 쪽지 가져오는 중 오류: " + e.getMessage());
		}
		
		return "/message/receiveMessageDetail";
	}
	
	/******************************
	 * @param MessageVO message_vo
	 * @return 보낸 쪽지 상세페이지
	 ******************************/
	@RequestMapping(value="/sendMsgDetail/{message_no}", method=RequestMethod.GET)
	public String sendMessageDetailPage(@PathVariable("message_no") Long message_no, Model model, HttpServletRequest request) {
		
		String sender_id = (String) request.getAttribute("userId");
		
		if(message_no == null) {
			log.error("receiveMessageDetailPage message_no is required.");
		}else if(sender_id == null) {
			
			log.error("sender_id is required.");
			return "member/login";
		}
		
		try {
			MessageVO detail = messageService.sendMsgDetail(message_no, sender_id);
			
			model.addAttribute("sendDetail", detail);
		}catch(Exception e) {
			
			log.error("보낸 쪽지 가져오는 중 오류: "+ e.getMessage());
		}
		
		return "/message/sendMessageDetail";
	}
	
	/********************************
	 * @return 쪽지 전송 페이지
	 ********************************/
	@RequestMapping(value = "/send/{user_name}", method = RequestMethod.GET)
	public String sendMessage(@PathVariable("user_name") String user_name, Model model) {
	    
		model.addAttribute("receiver", user_name);
		
		return "/message/send"; 
	}
	
	/********************************
	 * @param MessageVO message_vo 
	 * @return 쪽지 답장 페이지
	 ********************************/
	@RequestMapping(value="/response/{user_name}", method=RequestMethod.GET)
	public String responseForm(@PathVariable("user_name") String user_name, Model model) {
	    
		model.addAttribute("receiver", user_name);
		
		return "/message/response";
	}
	
	
	
	
}
