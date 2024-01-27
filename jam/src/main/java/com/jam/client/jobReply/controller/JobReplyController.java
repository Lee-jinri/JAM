package com.jam.client.jobReply.controller;

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
import com.jam.client.jobReply.service.JobReplyService;
import com.jam.client.jobReply.vo.JobReplyVO;
import com.jam.client.member.vo.MemberVO;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping(value="/jobReplies")
@AllArgsConstructor
public class JobReplyController {

	private JobReplyService jobReplyService;
	
	/************************
	 * 구인구직 댓글을 조회하는 메서드입니다.
	 * @param Long job_no 조회할 구인구직 글 번호
	 * @param JobReplyVO jrvo
	 * @return 댓글 리스트
	 *************************/
	@GetMapping(value = "/all/{job_no}", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<JobReplyVO> replyList(@PathVariable("job_no") Long job_no,@RequestParam(value = "user_id", required = false) String user_id,@ModelAttribute("data") JobReplyVO jrvo, MemberVO member, HttpServletRequest request, Model model){
		
		List<JobReplyVO> reply = null;
		reply = jobReplyService.jobReplyList(job_no);
		
		jrvo.setUser_id(user_id);
   		
		return reply;
	}
	

	/*******************************
	 * 구인구직 댓글을 작성하는 메서드입니다.
	 * @param JobReplyVO jrvo 
	 * @return 댓글 작성 실행 결과
	 *******************************/
	@JsonFormat
	@PostMapping(value="/replyInsert",consumes = "application/json", produces = MediaType.TEXT_PLAIN_VALUE)
	public String replyInsert(@RequestBody JobReplyVO jrvo,@ModelAttribute("data") MemberVO member, HttpServletRequest request, Model model) {

		int result = 0;
		
		result = jobReplyService.replyInsert(jrvo);
		
		return(result ==1)? "SUCCESS" : "FAILURE";
	}
	
	/*******************************
	 * 구인구직 댓글을 수정하는 메서드입니다.
	 * @param Long jobReply_no 수정할 댓글 번호
	 * @param JobReplyVO jrvo 수정할 댓글 내용
	 * @return 댓글 수정 결과
	 *******************************/
	@PutMapping(value = "/{jobReply_no}", consumes = "application/json", produces = MediaType.TEXT_PLAIN_VALUE)
	public String replyUpdate(@PathVariable("jobReply_no") Long jobReply_no, @RequestBody JobReplyVO jrvo) {
	
		jrvo.setJobReply_no(jobReply_no);
		int result = jobReplyService.replyUpdate(jrvo);
		return(result ==1) ? "SUCCESS" : "FAILURE";
	}
	
	/********************************
	 * 구인구직 댓글을 삭제하는 메서드입니다.
	 * @param Long jobReply_no 삭제할 댓글 번호
	 * @return 댓글 삭제 결과
	 **********************************/
	@DeleteMapping(value = "/{jobReply_no}", produces = MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity<String> replyDelete(@PathVariable("jobReply_no")Long jobReply_no){

		int result = jobReplyService.replyDelete(jobReply_no);
		
		return result == 1?
		new ResponseEntity<String>("SUCCESS", HttpStatus.OK) :new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);	
	}
	
	
}
