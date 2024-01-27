package com.jam.client.fleaReply.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
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
import com.jam.client.fleaReply.service.FleaReplyService;
import com.jam.client.fleaReply.vo.FleaReplyVO;
import com.jam.client.member.vo.MemberVO;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping(value="/fleareplies")
@AllArgsConstructor
public class FleaReplyController {

	private FleaReplyService fleareplyService;
	
	/***************************
	 * 중고악기 댓글을 조회하는 메서드입니다.
	 * @param Long flea_no 조회할 중고악기 글 번호
	 * @param FleaReplyVO frvo
	 * @return 댓글 리스트
	 ****************************/
	@DateTimeFormat 
	@GetMapping(value = "/all/{flea_no}", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<FleaReplyVO> replyList(@PathVariable("flea_no") Long flea_no,@RequestParam(value = "user_id", required = false) String user_id,  @ModelAttribute("data") FleaReplyVO frvo, MemberVO member, HttpServletRequest request, Model model){
		System.out.println("replyList 호출 성공");
		
		List<FleaReplyVO> reply = null;
		reply = fleareplyService.fleaReplyList(flea_no);
		
		frvo.setUser_id(user_id);
   		
		return reply;
	}
	
	/******************************
	 * 중고악기 댓글을 작성하는 메서드입니다.
	 * @param FleaReplyVO frvo
	 * @return 댓글 작성 실행 결과
	 **********************************/
	@JsonFormat
	@PostMapping(value="/reply",consumes = "application/json", produces = MediaType.TEXT_PLAIN_VALUE)
	public String replyInsert(@RequestBody FleaReplyVO frvo,@ModelAttribute("data") MemberVO member, HttpServletRequest request, Model model) {
		
		int result = 0;
		
		result = fleareplyService.replyInsert(frvo);
		
		return(result ==1)? "SUCCESS" : "FAILURE";
	}
	
	/****************************
	 * 중고악기 댓글을 수정하는 메서드입니다.
	 * @param Long fleaReply_no 수정할 댓글 번호
	 * @param FleaReplyVO frvo 수정할 댓글 내용
	 * @return 댓글 수정 결과
	 ****************************/
	@PutMapping(value = "/{fleaReply_no}", consumes = "application/json", produces = MediaType.TEXT_PLAIN_VALUE)
	public String replyUpdate(@PathVariable("fleaReply_no") Long fleaReply_no, @RequestBody FleaReplyVO frvo) {
	
		frvo.setFleaReply_no(fleaReply_no);
		int result = fleareplyService.replyUpdate(frvo);
		
		return(result ==1) ? "SUCCESS" : "FAILURE";
	}
	
	/*******************************
	 * 중고악기 댓글을 삭제하는 메서드입니다.
	 * @param Long fleaReply_no 삭제할 댓글 번호
	 * @return 댓글 삭제 결과
	 **********************************/
	
	@DeleteMapping(value = "/{fleaReply_no}", produces = MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity<String> replyDelete(@PathVariable("fleaReply_no")Long fleaReply_no){

		int result = fleareplyService.replyDelete(fleaReply_no);
		
		return result == 1?
		new ResponseEntity<String>("SUCCESS", HttpStatus.OK) :new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);	
	}
}
