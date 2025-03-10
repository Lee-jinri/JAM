package com.jam.client.jobReply.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.velocity.runtime.log.Log;
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
import com.jam.common.vo.ReplyResponse;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;

@RestController
@RequestMapping(value="/jobReplies")
@RequiredArgsConstructor
@Log4j
public class JobReplyController {

	private final JobReplyService jobReplyService;
	
	/************************
	 * 구인 댓글을 조회하는 메서드입니다.
	 * @param Long job_no 조회할 구인 글 번호
	 * @return 댓글 리스트
	 *************************/
	@GetMapping(value = "/all/{job_no}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<JobReplyVO>> replyList(@PathVariable("job_no") Long job_no) {
	    log.info("??"); // 요청이 정상적으로 들어오는지 확인
	    List<JobReplyVO> reply = jobReplyService.jobReplyList(job_no);

	    return ResponseEntity.ok(reply);
	}


	/*******************************
	 * 구인구직 댓글을 작성하는 메서드입니다.
	 * @param JobReplyVO jrvo 
	 * @return 댓글 작성 실행 결과
	 *******************************/
	@JsonFormat
	@PostMapping(value="/replyInsert", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, Object>> replyInsert(@RequestBody JobReplyVO jrvo, HttpServletRequest request) {
		
		Map<String, Object> response = new HashMap<>();
		
		String user_id = (String)request.getAttribute("userId");
		String user_name = (String)request.getAttribute("userName");
		
		if(user_id == null || user_name == null) {
			response.put("status", "FAIL");
			response.put("message", "로그인이 필요한 서비스 입니다.");
			return ResponseEntity.status(401).body(response);
		}
		
		if (jrvo.getJobReply_content() == null || jrvo.getJobReply_content().trim().isEmpty()) {
		    response.put("status", "FAIL");
		    response.put("message", "댓글 내용을 입력하세요.");
		    return ResponseEntity.badRequest().body(response);
		}
		
		jrvo.setUser_id(user_id);
		jrvo.setUser_name(user_name);
		
		int result = jobReplyService.replyInsert(jrvo);
		
		if(result == 1) {
			response.put("status", "SUCCESS");
			response.put("message", "댓글이 등록 되었습니다.");
			return ResponseEntity.ok(response);
		}else {
			response.put("status", "FAIL");
			response.put("message", "댓글 등록에 실패했습니다. 잠시 후 다시 시도하세요.");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}
	
	/*******************************
	 * 구인구직 댓글을 수정하는 메서드입니다.
	 * @param Long jobReply_no 수정할 댓글 번호
	 * @param JobReplyVO jrvo 수정할 댓글 내용
	 * @return 댓글 수정 결과
	 *******************************/
	@PutMapping(value = "/{jobReply_no}", consumes = "application/json", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, Object>> replyUpdate(@PathVariable("jobReply_no") Long jobReply_no, @RequestBody Map<String, String> payload, HttpServletRequest request) {
	
		Map<String, Object> response = new HashMap<>();
		
		String user_id = (String)request.getAttribute("userId");
		
		if(user_id == null) {
			response.put("status", "FAIL");
			response.put("message", "로그인이 필요한 서비스 입니다.");
			return ResponseEntity.status(401).body(response);
		}
		
		String jobReply_content = payload.get("jobReply_content");
		
		if (jobReply_content == null || jobReply_content.trim().isEmpty()) {
		    response.put("status", "FAIL");
		    response.put("message", "댓글 내용을 입력하세요.");
		    return ResponseEntity.badRequest().body(response);
		}
		
		JobReplyVO vo = new JobReplyVO();
		
		vo.setJobReply_no(jobReply_no);
		vo.setUser_id(user_id);
		vo.setJobReply_content(jobReply_content);
		
		int result = jobReplyService.replyUpdate(vo);
		
		if(result == 1) {
			response.put("status", "SUCCESS");
			response.put("message", "댓글이 수정 되었습니다.");
			return ResponseEntity.ok(response);
		}else {
			response.put("status", "FAIL");
			response.put("message", "댓글 수정이 실패했습니다. 잠시 후 다시 시도하세요.");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}
	
	/********************************
	 * 구인구직 댓글을 삭제하는 메서드입니다.
	 * @param Long jobReply_no 삭제할 댓글 번호
	 * @return 댓글 삭제 결과
	 **********************************/
	@DeleteMapping(value = "/{jobReply_no}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, Object>> replyDelete(@PathVariable("jobReply_no")Long jobReply_no, HttpServletRequest request){

		Map<String, Object> response = new HashMap<>();
		
		String user_id = (String)request.getAttribute("userId");
		
		if(user_id == null) {
			response.put("status", "FAIL");
			response.put("message", "로그인이 필요한 서비스 입니다.");
			return ResponseEntity.status(401).body(response);
		}
		
		int result = jobReplyService.replyDelete(jobReply_no, user_id);
		
		if(result == 1) {
			response.put("status", "SUCCESS");
			response.put("message", "댓글이 삭제 되었습니다.");
			return ResponseEntity.ok(response);
		}else {
			response.put("status", "FAIL");
			response.put("message", "댓글 삭제가 실패했습니다. 잠시 후 다시 시도하세요.");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}
	
	
}
