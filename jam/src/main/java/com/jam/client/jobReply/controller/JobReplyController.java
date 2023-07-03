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
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonFormat;
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
	
	@GetMapping(value = "/all/{job_no}", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<JobReplyVO> replyList(@PathVariable("job_no") Integer job_no,@ModelAttribute("data") JobReplyVO jrvo, MemberVO member, HttpServletRequest request, Model model){
		System.out.println("replyList 호출 성공");
		
		HttpSession session = request.getSession();
		MemberVO vo = (MemberVO)session.getAttribute("member");
		
		if(vo != null) {
   		 
			session.setAttribute("member", vo);
	   		jrvo.setUser_id(vo.getUser_id());
	   		jrvo.setUser_name(vo.getUser_name());
		} 
		List<JobReplyVO> reply = null;
		reply = jobReplyService.jobReplyList(job_no);
		
		return reply;
	}
	

	@JsonFormat
	@PostMapping(value="/replyInsert",consumes = "application/json", produces = MediaType.TEXT_PLAIN_VALUE)
	public String replyInsert(@RequestBody JobReplyVO jrvo,@ModelAttribute("data") MemberVO member, HttpServletRequest request, Model model) {
		
		HttpSession session = request.getSession();
		MemberVO vo =(MemberVO)session.getAttribute("member");
		session.setAttribute("member", vo);
		
		jrvo.setUser_id(vo.getUser_id());
		jrvo.setUser_name(vo.getUser_name());
		log.info("jobReplyVO : "+jrvo);
		
		int result = 0;
		
		result = jobReplyService.replyInsert(jrvo);
		
		return(result ==1)? "SUCCESS" : "FAILURE";
	}
	


	@PutMapping(value = "/{jobReply_no}", consumes = "application/json", produces = MediaType.TEXT_PLAIN_VALUE)
	public String replyUpdate(@PathVariable("jobReply_no") Integer jobReply_no, @RequestBody JobReplyVO jrvo) {
	
		jrvo.setJobReply_no(jobReply_no);
		int result = jobReplyService.replyUpdate(jrvo);
		return(result ==1) ? "SUCCESS" : "FAILURE";
	}
	
	@DeleteMapping(value = "/{jobReply_no}", produces = MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity<String> replyDelete(@PathVariable("jobReply_no")Integer jobReply_no){

		int result = jobReplyService.replyDelete(jobReply_no);
		
		return result == 1?
		new ResponseEntity<String>("SUCCESS", HttpStatus.OK) :new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);	
	}
	
	
}
