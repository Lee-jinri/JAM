package com.jam.client.message.controller;

import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jam.client.message.vo.MessageVO;
import com.jam.common.vo.PageDTO;
import com.jam.security.JwtTokenProvider;
import com.jam.client.fleaMarket.vo.FleaMarketVO;
import com.jam.client.member.vo.MemberVO;
import com.jam.client.message.service.MessageService;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j;

@Controller
@RequestMapping("/message")
@AllArgsConstructor
@Log4j
public class MessageController {
	
	@Autowired
	private MessageService messageService;
	
	@Autowired
	private JwtTokenProvider jwtTokenProvider;
	
	/***********************************************
	 * @return 받은 쪽지 목록 페이지
	 ***********************************************/
	@RequestMapping(value = "/receiveMessage", method = RequestMethod.GET)
	public String receiveMessage(@ModelAttribute MessageVO message, Model model, HttpServletRequest request) {
	    
		String accessToken = jwtTokenProvider.getAccessTokenFromCookies(request.getCookies());

	    if (accessToken.isEmpty()) {
	        return "member/login";
	    }

	    try {
	        String receiverId = jwtTokenProvider.getUserIdFormToken(accessToken);

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
	    
		String accessToken = jwtTokenProvider.getAccessTokenFromCookies(request.getCookies());

	    if (accessToken.isEmpty()) {
	        return "member/login";
	    }

	    try {
	        String sender_id = jwtTokenProvider.getUserIdFormToken(accessToken);

	        log.info(sender_id);
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
		
		String accessToken = jwtTokenProvider.getAccessTokenFromCookies(request.getCookies());
		String receiver_id = jwtTokenProvider.getUserIdFormToken(accessToken);
		
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
		
		String accessToken = jwtTokenProvider.getAccessTokenFromCookies(request.getCookies());
		String sender_id = jwtTokenProvider.getUserIdFormToken(accessToken);
		
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
