package com.jam.client.comReply.controller;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jam.client.comReply.service.ComReplyService;
import com.jam.client.comReply.vo.ComReplyVO;
import com.jam.client.member.vo.MemberVO;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j;

@RestController
@RequestMapping(value="/comreplies")
@AllArgsConstructor
@Log4j
public class ComReplyController {
	
	private ComReplyService comreplyService;
	
	/***************************
	 * @param Integer com_no
	 * @param ComReplyVO crvo	
	 * @param MemberVO member
	 * @return 커뮤니티 댓글 리스트
	 ****************************/
	@GetMapping(value = "/all/{com_no}", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<ComReplyVO> replyList(@PathVariable("com_no") Integer com_no, @RequestParam(value = "user_id", required = false) String user_id, @ModelAttribute("data") ComReplyVO crvo, MemberVO member, HttpServletRequest request, Model model){
		log.info("community reply list");
		
		List<ComReplyVO> reply = null;
		reply = comreplyService.comReplyList(com_no);
		
		crvo.setUser_id(user_id);
   		
		return reply;
	}
	
	/*************************
	 * 커뮤니티 댓글 작성
	 * @param ComReplyVO crvo
	 * @param MemberVO member
	 * @return 댓글 작성 실행 결과
	 **************************/
	@JsonFormat
	@PostMapping(value="/replyInsert",consumes = "application/json", produces = MediaType.TEXT_PLAIN_VALUE)
	public String replyInsert(@RequestBody ComReplyVO crvo,@ModelAttribute("data") MemberVO member, HttpServletRequest request, Model model) {
		
		log.info("community replyInsert");
		
		int result = 0;
		
		result = comreplyService.replyInsert(crvo);
		
		return(result ==1)? "SUCCESS" : "FAILURE";
	}
	

	/*******************************
	 * 커뮤니티 댓글 수정
	 * @param Integer comReply_no
	 * @param ComReplyVO crvo
	 * @return 댓글 수정 결과 
	 *******************************/
	@PutMapping(value = "/{comReply_no}", consumes = "application/json", produces = MediaType.TEXT_PLAIN_VALUE)
	public String replyUpdate(@PathVariable("comReply_no") Integer comReply_no, @RequestBody ComReplyVO crvo) {
	
		crvo.setComReply_no(comReply_no);
		int result = comreplyService.replyUpdate(crvo);
		
		return(result ==1) ? "SUCCESS" : "FAILURE";
	}
	
	/*******************************
	 * 커뮤니티 댓글 삭제
	 * @param Integer comReply_no
	 * @return 댓글 삭제 결과
	 ********************************/
	@DeleteMapping(value = "/{comReply_no}", produces = MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity<String> replyDelete(@PathVariable("comReply_no")Integer comReply_no){

		int result = comreplyService.replyDelete(comReply_no);
		
		return result == 1?
		new ResponseEntity<String>("SUCCESS", HttpStatus.OK) :new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);	
	}
}