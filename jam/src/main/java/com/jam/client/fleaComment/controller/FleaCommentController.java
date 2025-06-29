package com.jam.client.fleaComment.controller;

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
import com.jam.client.fleaComment.service.FleaCommentService;
import com.jam.client.fleaComment.vo.FleaCommentVO;
import com.jam.client.member.vo.MemberVO;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping(value="/fleaMarket/comment")
@AllArgsConstructor
public class FleaCommentController {

	private FleaCommentService commentService;
	
	/***************************
	 * 중고악기 댓글을 조회하는 메서드입니다.
	 * @param Long post_id 글 번호
	 * @param FleaCommentVO comment
	 * @return 댓글 리스트
	 ****************************/
	@DateTimeFormat 
	@GetMapping(value = "/all/{post_id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<FleaCommentVO> commentList(@PathVariable("post_id") Long post_id,@RequestParam(value = "user_id", required = false) String user_id,  @ModelAttribute("data") FleaCommentVO comment, MemberVO member, HttpServletRequest request, Model model){
		
		List<FleaCommentVO> reply = null;
		reply = commentService.commentList(post_id);
		
		comment.setUser_id(user_id);
   		
		return reply;
	}
	
	/******************************
	 * 중고악기 댓글을 작성하는 메서드입니다.
	 * @param FleaCommentVO 
	 * @return 댓글 작성 실행 결과
	 **********************************/
	@JsonFormat
	@PostMapping(value="/comment",consumes = "application/json", produces = MediaType.TEXT_PLAIN_VALUE)
	public String commentInsert(@RequestBody FleaCommentVO comment,@ModelAttribute("data") MemberVO member, HttpServletRequest request, Model model) {
		
		int result = 0;
		
		result = commentService.commentInsert(comment);
		
		return(result ==1)? "SUCCESS" : "FAILURE";
	}
	
	/****************************
	 * 중고악기 댓글을 수정하는 메서드입니다.
	 * @param Long commnet_id 수정할 댓글 번호
	 * @param FleaCommentVO comment 수정할 댓글 내용
	 * @return 댓글 수정 결과
	 ****************************/
	@PutMapping(value = "/{comment_id}", consumes = "application/json", produces = MediaType.TEXT_PLAIN_VALUE)
	public String replyUpdate(@PathVariable("comment_id") Long comment_id, @RequestBody FleaCommentVO comment) {
	
		comment.setComment_id(comment_id);
		int result = commentService.commentUpdate(comment);
		
		return(result ==1) ? "SUCCESS" : "FAILURE";
	}
	
	/*******************************
	 * 중고악기 댓글을 삭제하는 메서드입니다.
	 * @param Long commnet_id 삭제할 댓글 번호
	 * @return 댓글 삭제 결과
	 **********************************/
	
	@DeleteMapping(value = "/{comment_id}", produces = MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity<String> commnetDelete(@PathVariable("comment_id")Long comment_id){

		int result = commentService.commentDelete(comment_id);
		
		return result == 1?
		new ResponseEntity<String>("SUCCESS", HttpStatus.OK) :new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);	
	}
}
