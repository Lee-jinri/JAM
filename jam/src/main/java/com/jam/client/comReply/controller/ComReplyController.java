package com.jam.client.comReply.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
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
	 * 커뮤니티 댓글을 조회하는 메서드입니다.
	 * @param Long com_no 조회할 커뮤니티 글의 번호
	 * @param ComReplyVO crvo	
	 * @return 커뮤니티 댓글 리스트
	 ****************************/
	@GetMapping(value = "/reply/{com_no}", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<ComReplyVO> replyList(@PathVariable("com_no") Long com_no, @RequestParam(value = "user_id", required = false) String user_id){
		
		List<ComReplyVO> reply = comreplyService.comReplyList(com_no);
		
		return reply;
	}
	
	/*************************
	 * 커뮤니티 댓글을 작성하는 메서드입니다.
	 * @param ComReplyVO crvo
	 * @return 댓글 작성 실행 결과
	 **************************/
	@JsonFormat
	@PostMapping(value="/reply",consumes = "application/json", produces = MediaType.TEXT_PLAIN_VALUE)
	public String replyInsert(@RequestBody ComReplyVO crvo, HttpServletRequest request) {
		
	
		int result = 0;
		
		try {
			crvo.setUser_id((String)request.getAttribute("userId"));
			result = comreplyService.replyInsert(crvo);
		}catch(Exception e) {
			log.error(e.getMessage());
		}
		
		return(result ==1)? "SUCCESS" : "FAILURE";
	}
	

	/*******************************
	 * 커뮤니티 댓글을 수정하는 메서드입니다.
	 * @param Long comReply_no 수정할 댓글 번호
	 * @param ComReplyVO crvo 수정할 댓글 내용
	 * @return 댓글 수정 결과 
	 *******************************/
	@PutMapping(value = "/reply", consumes = "application/json", produces = MediaType.TEXT_PLAIN_VALUE)
	public String replyUpdate(@RequestBody ComReplyVO crvo, HttpServletRequest request) {
		
		int result = 0;
		
		try {
			crvo.setUser_id((String)request.getAttribute("userId"));
			
			result = comreplyService.replyUpdate(crvo);
		}catch(Exception e) {
			log.error(e.getMessage());
		}
		
		return(result ==1) ? "SUCCESS" : "FAILURE";
	}
	
	/*******************************
	 * 커뮤니티 댓글을 삭제하는 메서드입니다.
	 * @param Long comReply_no 삭제할 댓글 번호
	 * @return 댓글 삭제 결과
	 ********************************/
	@DeleteMapping(value = "/reply/{comReply_no}", produces = MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity<String> replyDelete(@PathVariable(name="comReply_no") Long comReply_no, HttpServletRequest request){
		
		int result = 0;
		try {
			String user_id = (String)request.getAttribute("userId");
			
			result = comreplyService.replyDelete(comReply_no, user_id);		
		}catch(Exception e) {
			log.error(e.getMessage());
		}
		return result == 1?
				new ResponseEntity<String>("SUCCESS", HttpStatus.OK) :new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);	
	}
}