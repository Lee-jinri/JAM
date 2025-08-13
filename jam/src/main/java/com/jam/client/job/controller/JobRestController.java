package com.jam.client.job.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

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

import com.jam.client.job.service.JobService;
import com.jam.client.job.vo.JobVO;
import com.jam.common.vo.PageDTO;
import com.jam.global.util.ValueUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;

@RestController
@RequestMapping("/api/jobs/")
@RequiredArgsConstructor
@Log4j
public class JobRestController {

	private final JobService jobService;
	
	@GetMapping(value = "boards")
	public ResponseEntity<Map<String, Object>> getBoards(JobVO job_vo, HttpServletRequest request){
		try {
			if (job_vo.getPositions() == null) {
			    job_vo.setPositions(Collections.emptyList());
			}
			
			String user_id = (String)request.getAttribute("userId");
			if(user_id != null) job_vo.setUser_id(user_id);
			
			Map<String, Object> result = new HashMap<>();

			List<JobVO> jobList = jobService.getBoards(job_vo);
			JobVO job = jobList.get(0);
			
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
	 * 구인 글 상세정보를 조회하는 메서드입니다.
	 *
	 * @param job_no 조회할 구인 글의 번호
	 * @return ResponseEntity<JobVO> - 조회된 구인 글의 정보와 HTTP 상태 코드를 포함한 응답 VO
	 * @throws Exception 데이터 조회 중 발생한 예외
	 ************************************/
	@GetMapping(value = "/board/{job_no}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<JobVO> getBoardDetail(@PathVariable("job_no") Long job_no, HttpServletRequest request) throws Exception{
		if (job_no == null) { 
			log.error("job_no is required");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}
		
		// 조회수 증가
		jobService.incrementReadCnt(job_no);
					
		try {
			JobVO detail = jobService.getBoardDetail(job_no);
			
			detail.setPosition(getTranslatedPosition(detail.getPosition()));
			detail.setAuthor(false);
			
			String userId = (String) request.getAttribute("userId");
			if (userId != null && userId.equals(detail.getUser_id())) {
			   detail.setAuthor(true);
			}
			
			return ResponseEntity.ok(detail);
	    } catch (Exception e) {
	    	log.error("Error fetching job detail for job_no: {}"+ job_no + e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
	    }
	}
	
	public String getTranslatedPosition(String position) {
	    Map<String, String> positionMap = Map.of(
	        "vocal", "보컬",
	        "piano", "피아노",
	        "guitar", "기타",
	        "bass", "베이스",
	        "drum", "드럼",
	        "midi", "작곡·미디",
	        "lyrics", "작사",
	        "chorus", "코러스",
	        "brass", "관악기",
	        "string", "현악기"
	    );
	    return positionMap.getOrDefault(position, position); // 변환값 없으면 원래 값 반환
	}

	
	/******************************
	 * 구인 글을 작성하는 메서드 입니다.
	 * @param JobVO job_vo 작성자 id와 닉네임, 제목과 내용, 카테고리, 급여 지불 방법, 급여
	 * @return HTTP 상태 코드
	 * 			성공 시 HttpStatus.OK를 반환하고 실패 시 HttpStatus.INTERNAL_SERVER_ERROR를 반환합니다.
	 *****************************/
	@RequestMapping(value="/board", method=RequestMethod.POST)
	public ResponseEntity<String> writeBoard(@RequestBody JobVO jobVO, HttpServletRequest request) throws Exception{
		
		if (jobVO == null) {
	        log.error("jobVO is null");
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("jobVO is null.");
	    }

	    // 유효성 검사
	    String validationError = validateJobVO(jobVO);
	    if (validationError != null) {
	        log.error(validationError);
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validationError);
	    }

	    // 기본값 및 사용자 정보 세팅
	    preprocessJobVO(jobVO, request);
		
		try {
			jobService.writeBoard(jobVO);
			
			String job_no = jobVO.getJob_no().toString();
			
			return new ResponseEntity<>(job_no,HttpStatus.OK);
		} catch (Exception e) {
			log.error("구인 글 작성 데이터 저장 중 오류 : " + e);
			
			String responseBody = e.getMessage();
			return new ResponseEntity<>(responseBody, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	private String validateJobVO(JobVO jobVO) {
	    if (jobVO.getJob_title() == null || jobVO.getJob_title().isEmpty()) {
	        return "제목은 필수 입력 항목입니다.";
	    }
	    if (jobVO.getJob_content() == null || jobVO.getJob_content().isEmpty()) {
	        return "내용은 필수 입력 항목입니다.";
	    }
	    
	    if (jobVO.getPosition() == null || jobVO.getPosition().isEmpty()) {
	        return "포지션을 선택해주세요.";
	    }
	    
	    return null;
	}

	private void preprocessJobVO(JobVO jobVO, HttpServletRequest request) {
	    jobVO.setUser_id((String) request.getAttribute("userId"));
	    jobVO.setUser_name((String) request.getAttribute("userName"));

	    // gu, dong 기본값 처리
	    jobVO.setGu(ValueUtils.guNullToAll(jobVO.getGu()));
	    jobVO.setDong(ValueUtils.emptyToNull(jobVO.getDong()));
	    
	   // jobVO.setPay(ValueUtils.)
	}
	
	/***********************************
	 * 구인 글을 수정하는 메서드 입니다.
	 * @param JobVO job_vo  수정할 글 번호, 제목, 내용, 카테고리, 급여 지불 방법, 급여
	 * @return HTTP 상태 코드
	 * 			성공 시 HttpStatus.OK를 반환하고 실패 시 HttpStatus.INTERNAL_SERVER_ERROR를 반환합니다.
	 ***********************************/
	@RequestMapping(value="/board", method=RequestMethod.PUT)
	public ResponseEntity<String> editBoard(@RequestBody JobVO jobVO, HttpServletRequest request) throws Exception{
		if (jobVO == null) {
	        log.error("jobVO is null");
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("jobVO is null.");
	    }

	    // 유효성 검사
	    String validationError = validateJobVO(jobVO);
	    if (validationError != null) {
	        log.error(validationError);
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validationError);
	    }

	    // 기본값 및 사용자 정보 세팅
	    preprocessJobVO(jobVO, request);
		
		try {
			jobService.editBoard(jobVO);
			String job_no = jobVO.getJob_no().toString();
			
			return new ResponseEntity<>(job_no, HttpStatus.OK);
		} catch(Exception e) {
			log.error("구인 editBoard 데이터 수정 중 오류 : " + e.getMessage());
			String responseBody = "시스템 오류 입니다. 잠시 후 다시 시도하세요.";
			
			return new ResponseEntity<>(responseBody, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
	}
	

	/**********************************
	 * 구인 글을 삭제하는 메서드 입니다.
	 * @param Long job_no 삭제할 글 번호
	 * @return HTTP 상태 코드
	 * 			성공 시 HttpStatus.OK를 반환하고 실패 시 HttpStatus.INTERNAL_SERVER_ERROR를 반환합니다.
	 **********************************/
	@RequestMapping(value="/board", method=RequestMethod.DELETE)
	public ResponseEntity<String> boardDelete(@RequestParam("job_no") Long job_no, HttpServletRequest request) throws Exception{
		
		if (job_no == null) { 
			log.error("job_no is required");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("job_no is required");
		}
		
		// 오류 메시지 수정
		String user_id = (String)request.getAttribute("userId");
		if(user_id == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 안됨.");
		
		// 이거 추가해야됨
		try {
			jobService.boardDelete(job_no, user_id);
			
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (Exception e) {
			log.error("구인 delete 데이터 삭제 중 오류 : " + e.getMessage());
			
			String resopnseBody = "구인 delete 데이터 삭제 중 오류 : " + e.getMessage();
			
			return new ResponseEntity<>(resopnseBody, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
