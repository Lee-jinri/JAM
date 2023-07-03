package com.jam.client.fleaReply.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jam.client.fleaReply.service.FleaReplyService;
import com.jam.client.fleaReply.vo.FleaReplyVO;
import com.jam.client.member.vo.MemberVO;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j;

@RestController
@RequestMapping(value="/fleareplies")
@AllArgsConstructor
@Log4j
public class FleaReplyController {

	private FleaReplyService fleareplyService;
	
	@DateTimeFormat 
	@GetMapping(value = "/all/{flea_no}", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<FleaReplyVO> replyList(@PathVariable("flea_no") Integer flea_no, @ModelAttribute("data") FleaReplyVO frvo, MemberVO member, HttpServletRequest request, Model model){
		System.out.println("replyList 호출 성공");
		
		HttpSession session = request.getSession();
		MemberVO vo = (MemberVO)session.getAttribute("member");
		
		if(vo != null) {
   		 
			session.setAttribute("member", vo);
			frvo.setUser_id(vo.getUser_id());
			frvo.setUser_name(vo.getUser_name());
		} 
		List<FleaReplyVO> reply = null;
		reply = fleareplyService.fleaReplyList(flea_no);
		
		return reply;
	}
	
	@JsonFormat
	@PostMapping(value="/replyInsert",consumes = "application/json", produces = MediaType.TEXT_PLAIN_VALUE)
	public String replyInsert(@RequestBody FleaReplyVO frvo,@ModelAttribute("data") MemberVO member, HttpServletRequest request, Model model) {
		
		System.out.println("replyInsert 호출 성공");
		HttpSession session = request.getSession();
		MemberVO vo =(MemberVO)session.getAttribute("member");
		session.setAttribute("member", vo);
		
		frvo.setUser_id(vo.getUser_id());
		frvo.setUser_name(vo.getUser_name());
		log.info("fleaReplyVO : "+frvo);
		
		int result = 0;
		
		result = fleareplyService.replyInsert(frvo);
		
		return(result ==1)? "SUCCESS" : "FAILURE";
	}
	
	@PutMapping(value = "/{fleaReply_no}", consumes = "application/json", produces = MediaType.TEXT_PLAIN_VALUE)
	public String replyUpdate(@PathVariable("fleaReply_no") Integer fleaReply_no, @RequestBody FleaReplyVO frvo) {
	
		frvo.setFleaReply_no(fleaReply_no);
		int result = fleareplyService.replyUpdate(frvo);
		return(result ==1) ? "SUCCESS" : "FAILURE";
	}
	
	@DeleteMapping(value = "/{fleaReply_no}", produces = MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity<String> replyDelete(@PathVariable("fleaReply_no")Integer fleaReply_no){

		int result = fleareplyService.replyDelete(fleaReply_no);
		
		return result == 1?
		new ResponseEntity<String>("SUCCESS", HttpStatus.OK) :new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);	
	}
}
