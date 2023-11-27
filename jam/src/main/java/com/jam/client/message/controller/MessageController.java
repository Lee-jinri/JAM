package com.jam.client.message.controller;

import java.util.List;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jam.client.message.vo.MessageVO;
import com.jam.common.vo.PageDTO;
import com.jam.client.message.service.MessageService;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j;

@Controller
@RequestMapping("/message")
@AllArgsConstructor
public class MessageController {
	
	@Autowired
	private MessageService messageService;
	
	/***********************************************
	 * @param MessageVO m_vo
	 * @return 받은 쪽지 목록 페이지
	 *******************************************/
	@RequestMapping(value="/receiveMessage", method=RequestMethod.GET)
	public String receiveMessage(@RequestParam("user_id") String user_id, Model model, @ModelAttribute MessageVO m_vo) {
		String url = "";
		
		if(user_id != null && user_id != "") {
			
			m_vo.setReceiver_id(user_id);
				
			List<MessageVO> rMessage = messageService.receiveMessage(m_vo);
			model.addAttribute("rMessage",rMessage);
				
			int total = messageService.receiveListCnt(m_vo);
			model.addAttribute("pageMaker",new PageDTO(m_vo, total));
			
			url = "message/rMessageList";
		}else {
			url = "member/login";
		}
		
		return url;
	}
	
	/*********************************
	 * @param MessageVO m_vo
	 * @return 보낸 쪽지 목록 페이지
	 ********************************/
	@RequestMapping(value="/sendMessage", method=RequestMethod.GET)
	public String sendMessage(@RequestParam("user_id") String user_id, Model model, @ModelAttribute MessageVO m_vo) {
		
		String url = "";
		
		if(user_id != null && user_id != "") {
					
			m_vo.setSender_id(user_id);
			
			List<MessageVO> sMessage = messageService.sendMessage(m_vo);
			model.addAttribute("sMessage",sMessage);
			
			int total = messageService.sendListCnt(m_vo);
			model.addAttribute("pageMaker",new PageDTO(m_vo, total));
			
			url = "message/sMessageList";
		}else {
			url = "member/login";
		}
		return url;
	}
	
	/***********************************
	 * @param MessageVO message_vo
	 * @return 받은 쪽지 상세 페이지
	 ***********************************/
	@RequestMapping(value="/rMessage_detail", method=RequestMethod.POST)
	public String receiveMessageDetail(@ModelAttribute MessageVO message_vo, Model model) {
		
		messageService.message_read(message_vo);
		MessageVO detail = messageService.rMessageDetail(message_vo);
		
		model.addAttribute("detail", detail);
		return "/message/receiveMessageDetail";
	}
	
	/******************************
	 * @param MessageVO message_vo
	 * @return 보낸 쪽지 상세페이지
	 ******************************/
	@RequestMapping(value="/sMessage_detail", method=RequestMethod.POST)
	public String sendMessageDetail(@ModelAttribute MessageVO message_vo, Model model) {
		
		MessageVO detail = messageService.sMessageDetail(message_vo);
		
		model.addAttribute("detail", detail);
		return "/message/sendMessageDetail";
	}
	
	/********************************
	 * @return 쪽지 전송 페이지
	 ********************************/
	@RequestMapping(value="/send", method=RequestMethod.POST)
	public String sendForm(@ModelAttribute MessageVO message_vo, Model model) {
		
		model.addAttribute("receiver",message_vo.getReceiver());
		model.addAttribute("receiver_id",message_vo.getReceiver_id());
		return "/message/send";
	}
	
	/********************************
	 * @param MessageVO message_vo 
	 * @return 쪽지 답장 페이지
	 ********************************/
	@RequestMapping(value="/response", method=RequestMethod.POST)
	public String responseForm(@ModelAttribute MessageVO message_vo, Model model) {
		
		String receiver = message_vo.getSender();
		String receiver_id = message_vo.getSender_id();
		
		model.addAttribute("receiver",receiver);
		model.addAttribute("receiver_id",receiver_id);
		return "/message/response";
	}
	
	/*******************************
	 * 쪽지 전송
	 * @param MessageVO message_vo
	 * @return 쪽지 전송 결과 
	 ****************************/
	@ResponseBody
	@RequestMapping(value="/messageWrite", method=RequestMethod.POST)
	public String messageWrite(@ModelAttribute MessageVO message_vo, Model model) {

		int result = messageService.messageWrite(message_vo);
		
		return(result == 1)? "SUCCESS" : "FAILURE";
	}
	
	
}
