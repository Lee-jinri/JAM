package com.jam.client.message.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.jam.client.member.vo.MemberVO;
import com.jam.client.message.vo.MessageVO;
import com.jam.common.vo.PageDTO;
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
	
	/***********************************************
	 * @param MessageVO m_vo
	 * @return 받은 쪽지 목록 페이지
	 *******************************************/
	@RequestMapping(value="/receiveMessage", method=RequestMethod.GET)
	public String receiveMessage(HttpServletRequest request, Model model, @ModelAttribute MessageVO m_vo) {
		
		String url = "";
		
		HttpSession session = request.getSession(false);
		if(session != null) {
			MemberVO member = (MemberVO) session.getAttribute("member");
			if (member != null) {
				url = "message/rMessageList";
				
				m_vo.setReceiver_id(member.getUser_id());
				
				List<MessageVO> rMessage = messageService.receiveMessage(m_vo);
				model.addAttribute("rMessage",rMessage);
				
				int total = messageService.receiveListCnt(m_vo);
				model.addAttribute("pageMaker",new PageDTO(m_vo, total));
			}else {
				url = "member/login";
			}
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
	public String sendMessage(HttpServletRequest request, Model model, @ModelAttribute MessageVO m_vo) {
		
		String url = "";
		
		HttpSession session = request.getSession(false);
		if(session != null) {
			MemberVO member = (MemberVO) session.getAttribute("member");
			if (member != null) {
				m_vo.setSender_id(member.getUser_id());
				
				List<MessageVO> sMessage = messageService.sendMessage(m_vo);
				model.addAttribute("sMessage",sMessage);
				
				url = "message/sMessageList";
				
				int total = messageService.sendListCnt(m_vo);
				model.addAttribute("pageMaker",new PageDTO(m_vo, total));
				
			}else {
				url = "member/login";
			}
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
	public String receiveMessageDetail(HttpServletRequest request, @ModelAttribute MessageVO message_vo, Model model) {
		
		HttpSession session = request.getSession(false);
		MemberVO member = (MemberVO) session.getAttribute("member");
		String user = member.getUser_id();
		
		messageService.message_read(message_vo);
		MessageVO detail = messageService.rMessageDetail(message_vo);
		
		model.addAttribute("user",user);
		model.addAttribute("detail", detail);
		return "/message/receiveMessageDetail";
	}
	
	/******************************
	 * @param MessageVO message_vo
	 * @return 보낸 쪽지 상세페이지
	 ******************************/
	@RequestMapping(value="/sMessage_detail", method=RequestMethod.POST)
	public String sendMessageDetail(HttpServletRequest request, @ModelAttribute MessageVO message_vo, Model model) {
		
		HttpSession session = request.getSession(false);
		MemberVO member = (MemberVO) session.getAttribute("member");
		String user = member.getUser_id();
		
		MessageVO detail = messageService.sMessageDetail(message_vo);
		
		model.addAttribute("user",user);
		model.addAttribute("detail", detail);
		return "/message/sendMessageDetail";
	}
	
	/********************************
	 * @return 쪽지 전송 페이지
	 ********************************/
	@RequestMapping(value="/send", method=RequestMethod.POST)
	public String sendForm(@ModelAttribute MessageVO message_vo, Model model) {
		
		log.info("받는 사람 " + message_vo.getReceiver());
		log.info(message_vo.getReceiver_id());
		
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
	public String messageWrite(HttpServletRequest request, @ModelAttribute MessageVO message_vo, Model model) {
		
		HttpSession session = request.getSession(false);
		MemberVO member = (MemberVO) session.getAttribute("member");
	
		message_vo.setSender(member.getUser_name());
		message_vo.setSender_id(member.getUser_id());
		
		log.info("받는 사람" +message_vo.getReceiver());
		log.info("받는 사람2" + message_vo.getReceiver_id());
		
		int result = messageService.messageWrite(message_vo);
		
		return(result == 1)? "SUCCESS" : "FAILURE";
	}
	
	
}
