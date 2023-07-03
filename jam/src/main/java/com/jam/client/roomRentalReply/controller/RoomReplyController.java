package com.jam.client.roomRentalReply.controller;

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
import com.jam.client.member.vo.MemberVO;
import com.jam.client.roomRentalReply.service.RoomReplyService;
import com.jam.client.roomRentalReply.vo.RoomReplyVO;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j;

@RestController
@RequestMapping(value="roomreplies")
@AllArgsConstructor
@Log4j
public class RoomReplyController {

	private RoomReplyService roomreplyService;
	
	@DateTimeFormat 
	@GetMapping(value = "/all/{roomRental_no}", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<RoomReplyVO> replyList(@PathVariable("roomRental_no") Integer roomRental_no, @ModelAttribute("data") RoomReplyVO rrvo, MemberVO member, HttpServletRequest request, Model model){
		System.out.println("replyList 호출 성공");
		
		HttpSession session = request.getSession();
		MemberVO vo = (MemberVO)session.getAttribute("member");
		
		if(vo != null) {
   		 
			session.setAttribute("member", vo);
			rrvo.setUser_id(vo.getUser_id());
			rrvo.setUser_name(vo.getUser_name());
		} 
		List<RoomReplyVO> reply = null;
		reply = roomreplyService.roomReplyList(roomRental_no);
		
		return reply;
	}
	
	@JsonFormat
	@PostMapping(value="/replyInsert",consumes = "application/json", produces = MediaType.TEXT_PLAIN_VALUE)
	public String replyInsert(@RequestBody RoomReplyVO rrvo,@ModelAttribute("data") MemberVO member, HttpServletRequest request, Model model) {
		
		System.out.println("replyInsert 호출 성공");
		HttpSession session = request.getSession();
		MemberVO vo =(MemberVO)session.getAttribute("member");
		session.setAttribute("member", vo);
		
		rrvo.setUser_id(vo.getUser_id());
		rrvo.setUser_name(vo.getUser_name());
		log.info("roomReplyVO : "+rrvo);
		
		int result = 0;
		
		result = roomreplyService.replyInsert(rrvo);
		
		return(result ==1)? "SUCCESS" : "FAILURE";
	}
	
	@PutMapping(value = "/{roomReply_no}", consumes = "application/json", produces = MediaType.TEXT_PLAIN_VALUE)
	public String replyUpdate(@PathVariable("roomReply_no") Integer roomReply_no, @RequestBody RoomReplyVO rrvo) {
		
		rrvo.setRoomReply_no(roomReply_no);
		int result = roomreplyService.replyUpdate(rrvo);
		return(result ==1) ? "SUCCESS" : "FAILURE";
	}
	
	@DeleteMapping(value = "/{roomReply_no}", produces = MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity<String> replyDelete(@PathVariable("roomReply_no")Integer roomReply_no){

		int result = roomreplyService.replyDelete(roomReply_no);
		
		return result == 1?
		new ResponseEntity<String>("SUCCESS", HttpStatus.OK) :new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);	
	}
}
