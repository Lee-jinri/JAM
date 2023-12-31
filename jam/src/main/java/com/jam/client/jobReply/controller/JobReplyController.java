package com.jam.client.jobReply.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

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
import com.jam.client.comReply.vo.ComReplyVO;
import com.jam.client.jobReply.service.JobReplyService;
import com.jam.client.jobReply.vo.JobReplyVO;
import com.jam.client.member.vo.MemberVO;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j;

@RestController
@RequestMapping(value="/jobReplies")
@AllArgsConstructor
@Log4j
public class JobReplyController {

	private JobReplyService jobReplyService;
	
	/************************
	 * @param Integer job_no
	 * @param JobReplyVO jrvo
	 * @param MemberVO member
	 * @return 댓글 리스트
	 *************************/
	@GetMapping(value = "/all/{job_no}", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<JobReplyVO> replyList(@PathVariable("job_no") Integer job_no,@RequestParam(value = "user_id", required = false) String user_id,@ModelAttribute("data") JobReplyVO jrvo, MemberVO member, HttpServletRequest request, Model model){
		
		List<JobReplyVO> reply = null;
		reply = jobReplyService.jobReplyList(job_no);
		
		jrvo.setUser_id(user_id);
   		
		return reply;
	}
	

	/*******************************
	 * 구인구직 댓글 작성
	 * @param JobReplyVO jrvo
	 * @param MemberVO member
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
	 * 구인구직 댓글 수정
	 * @param Integer jobReply_no
	 * @param JobReplyVO jrvo
	 * @return 댓글 수정 결과
	 */
	@PutMapping(value = "/{jobReply_no}", consumes = "application/json", produces = MediaType.TEXT_PLAIN_VALUE)
	public String replyUpdate(@PathVariable("jobReply_no") Integer jobReply_no, @RequestBody JobReplyVO jrvo) {
	
		jrvo.setJobReply_no(jobReply_no);
		int result = jobReplyService.replyUpdate(jrvo);
		return(result ==1) ? "SUCCESS" : "FAILURE";
	}
	
	/********************************
	 * 구인구직 댓글 삭제
	 * @param Integer jobReply_no
	 * @return 댓글 삭제 결과
	 */
	@DeleteMapping(value = "/{jobReply_no}", produces = MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity<String> replyDelete(@PathVariable("jobReply_no")Integer jobReply_no){

		int result = jobReplyService.replyDelete(jobReply_no);
		
		return result == 1?
		new ResponseEntity<String>("SUCCESS", HttpStatus.OK) :new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);	
	}
	
	
}
