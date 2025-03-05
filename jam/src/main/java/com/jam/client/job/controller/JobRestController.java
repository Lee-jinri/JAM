package com.jam.client.job.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jam.client.community.vo.CommunityVO;
import com.jam.client.job.service.JobService;
import com.jam.client.job.vo.JobVO;
import com.jam.common.vo.PageDTO;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j;

@RestController
@RequestMapping("/api/job/")
@AllArgsConstructor
@Log4j
public class JobRestController {

	@Autowired
	private JobService jobService;
	

	@GetMapping(value = "boards")
	public ResponseEntity<Map<String, Object>> getBoards(JobVO job_vo){
		

		/**/
		try {
			log.info(job_vo);
			
			if (job_vo.getPositions() == null) {
			    job_vo.setPositions(Collections.emptyList());
			}

			Map<String, Object> result = new HashMap<>();

			List<JobVO> jobList = jobService.getBoards(job_vo);
			result.put("jobList", jobList);
			
			int total = jobService.listCnt(job_vo);
			PageDTO pageMaker = new PageDTO(job_vo, total);
	        result.put("pageMaker", pageMaker);

	        return ResponseEntity.ok(result);
		}catch(Exception e) {
			log.error(e.getMessage());
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(Collections.singletonMap("error", "An unexpected error occurred"));
		}
		
	}
	
	
	/*************************************
	 * 구인구직 글 상세정보를 조회하는 메서드입니다.
	 *
	 * @param job_no 조회할 구인구직 글의 번호
	 * @return ResponseEntity<JobVO> - 조회된 구인구직 글의 정보와 HTTP 상태 코드를 포함한 응답 VO
	 * @throws Exception 데이터 조회 중 발생한 예외
	 ************************************/
	@GetMapping(value = "/board/{job_no}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<JobVO> getBoardDetail(@PathVariable("job_no") Long job_no) throws Exception{
		if (job_no == null) { 
			log.error("job_no is required");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}
		
		try {
	        // 조회수 증가
			jobService.incrementReadCnt(job_no);
			
			// 상세 페이지 조회
			JobVO detail = jobService.getBoardDetail(job_no);
	       
	        return new ResponseEntity<>(detail, HttpStatus.OK);
	    } catch (Exception e) {
	        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}
	
	
	/******************************
	 * 구인구직 글을 작성하는 메서드 입니다.
	 * @param JobVO job_vo 작성자 id와 닉네임, 제목과 내용, 카테고리, 급여 지불 방법, 급여
	 * @return HTTP 상태 코드
	 * 			성공 시 HttpStatus.OK를 반환하고 실패 시 HttpStatus.INTERNAL_SERVER_ERROR를 반환합니다.
	 *****************************/
	@RequestMapping(value="/board", method=RequestMethod.POST)
	public ResponseEntity<String> writeBoard(@RequestBody JobVO job_vo) throws Exception{
		
		String errorMsg;
		if (job_vo == null) { 
			log.error("job_vo is null");
			errorMsg = "job_vo is null.";
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMsg);
		}
		
		// 유효성 검사
		String job_title = job_vo.getJob_title();
		String job_content = job_vo.getJob_content();

		if (job_title == null) {
			log.error("job_title is null.");
			errorMsg = "job_title is null.";
		    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMsg);
		}
		if (job_content == null) {
			log.error("job_content is null.");
			errorMsg = "job_content is null.";
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMsg);
		}
		
		log.info(job_vo);
		
		log.info(job_vo.getCity());
		log.info(job_vo.getGu());
		log.info(job_vo.getDong());
		
		try {
			jobService.writeBoard(job_vo);
			
			String job_no = job_vo.getJob_no().toString();
			
			return new ResponseEntity<>(job_no,HttpStatus.OK);
		} catch (Exception e) {
			log.error("구인구직 글 작성 데이터 저장 중 오류 : " + e.getMessage());
			
			String responseBody = e.getMessage();
			return new ResponseEntity<>(responseBody, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	/*******************************************
	 * 구인구직의 수정할 글 정보(제목, 내용, 급여, 급여 지불 방법, 카테고리, 구인구직 완료 여부)를 불러오는 메서드 입니다.
	 * @param job_no 수정을 위해 불러올 글 번호
	 * @return ResponseEntity<JobVO> - 조회된 구인구직 글의 정보와 HTTP 상태 코드를 포함한 응답 VO
	 *******************************************/
	@GetMapping(value = "/board/edit/{job_no}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<JobVO> getBoardById(@PathVariable Long job_no) {
		if (job_no == null) { 
			log.error("job_no is required");
			
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}else {
			try {
				JobVO board = jobService.getBoardById(job_no);
			    board.setJob_no(job_no);
			    
				return ResponseEntity.ok(board);
			}catch (Exception e) {
				log.error("구인구직 수정 글 불러오던 중 오류 : " + e.getMessage());
				
				return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
		
	}
	
	/***********************************
	 * 구인구직 글을 수정하는 메서드 입니다.
	 * @param JobVO job_vo  수정할 글 번호, 제목, 내용, 카테고리, 급여 지불 방법, 급여
	 * @return HTTP 상태 코드
	 * 			성공 시 HttpStatus.OK를 반환하고 실패 시 HttpStatus.INTERNAL_SERVER_ERROR를 반환합니다.
	 ***********************************/
	@RequestMapping(value="/board", method=RequestMethod.PUT)
	public ResponseEntity<String> editBoard(@RequestBody JobVO job_vo) throws Exception{
		String errorMsg;
		if (job_vo == null) { 
			log.error("job_vo is null");
			errorMsg = "job_vo is null.";
			
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMsg);
		}
		
		// 유효성 검사
		String job_title = job_vo.getJob_title();
		String job_content = job_vo.getJob_content();

		if (job_title == null) {
			log.error("job_title is null.");
			errorMsg = "job_title is null.";
		    
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMsg);
		}
		if (job_content == null) {
			log.error("job_content is null.");
			errorMsg = "job_content is null.";
			
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMsg);
		}
		
		try {
			jobService.editBoard(job_vo);
			String job_no = job_vo.getJob_no().toString();
			
			return new ResponseEntity<>(job_no, HttpStatus.OK);
		} catch(Exception e) {
			log.error("구인구직 editBoard 데이터 수정 중 오류 : " + e.getMessage());
			String responseBody = e.getMessage();
			
			return new ResponseEntity<>(responseBody, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
	}
	

	// 본인 확인 해야됨
	/**********************************
	 * 구인구직 글을 삭제하는 메서드 입니다.
	 * @param Long job_no 삭제할 글 번호
	 * @return HTTP 상태 코드
	 * 			성공 시 HttpStatus.OK를 반환하고 실패 시 HttpStatus.INTERNAL_SERVER_ERROR를 반환합니다.
	 **********************************/
	@RequestMapping(value="/board", method=RequestMethod.DELETE)
	public ResponseEntity<String> boardDelete(@RequestParam("job_no") Long job_no) throws Exception{
		
		if (job_no == null) { 
			log.error("job_no is required");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("job_no is required");
		}
		
		try {
			jobService.boardDelete(job_no);
			
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (Exception e) {
			log.error("구인구직 delete 데이터 삭제 중 오류 : " + e.getMessage());
			
			String resopnseBody = "구인구직 delete 데이터 삭제 중 오류 : " + e.getMessage();
			
			return new ResponseEntity<>(resopnseBody, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
