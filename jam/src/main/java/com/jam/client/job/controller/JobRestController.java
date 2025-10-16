package com.jam.client.job.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jam.client.job.service.JobService;
import com.jam.client.job.vo.ApplicationVO;
import com.jam.client.job.vo.JobVO;
import com.jam.common.vo.PageDTO;
import com.jam.global.exception.UnauthorizedException;
import com.jam.global.util.ValueUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;

@RestController
@RequestMapping("/api/jobs/")
@RequiredArgsConstructor
@Log4j
public class JobRestController {

	private final JobService jobService;

	/**
	 * jobs 게시판 조회 API
	 * 요청 파라미터(JobVO)에 따라 채용공고 목록을 조회하고 페이징 정보를 함께 반환합니다.
	 *
	 * @param jobs	요청 파라미터를 담은 VO 객체
	 * @param request	HttpServletRequest, userId 추출용
	 * @return			jobList(채용공고 목록), pageMaker(페이징 정보)
	 */
	@GetMapping(value = "board")
	public ResponseEntity<Map<String, Object>> getBoard(JobVO jobs, HttpServletRequest request){
		try {
			if (jobs.getPositions() == null) jobs.setPositions(Collections.emptyList());
			
			String user_id = (String)request.getAttribute("userId");
			jobs.setUser_id(user_id); 
			
			String kw = jobs.getKeyword();
			jobs.setKeyword(ValueUtils.sanitizeForLike(kw));
			
			Map<String, Object> result = new HashMap<>();

			List<JobVO> jobList = jobService.getBoard(jobs);
			result.put("jobList", jobList);
			
			int total = jobService.listCnt(jobs);
			PageDTO pageMaker = new PageDTO(jobs, total);
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
	 * @param post_id 조회할 구인 글의 번호
	 * @return ResponseEntity<JobVO> - 조회된 구인 글의 정보와 HTTP 상태 코드를 포함한 응답 VO
	 * @throws Exception 데이터 조회 중 발생한 예외
	 ************************************/
	@GetMapping(value = "/post/{post_id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, Object>> getPost(@PathVariable("post_id") Long post_id, HttpServletRequest request) throws Exception{
		if (post_id == null) { 
			log.error("post_id is required");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}
		
		// 조회수 증가
		jobService.incrementReadCnt(post_id);
					
		try {
			Map<String, Object> result = new HashMap<>();
			
			JobVO post = jobService.getPost(post_id);
			post.setPosition(getTranslatedPosition(post.getPosition()));
			result.put("post", post);
			
			String userId = (String)request.getAttribute("userId");
			Boolean isAuthor = false;
			
			if(userId != null && userId.equals(post.getUser_id())) isAuthor = true;
			result.put("isAuthor", isAuthor);
			
			return ResponseEntity.ok(result);
	    } catch (Exception e) {
	    	log.error("Error fetching job detail for post_id: "+ post_id + e);
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
	 * @param JobVO jobs 작성자 id와 닉네임, 제목과 내용, 카테고리, 급여 지불 방법, 급여
	 * @return HTTP 상태 코드
	 * 			성공 시 HttpStatus.OK를 반환하고 실패 시 HttpStatus.INTERNAL_SERVER_ERROR를 반환합니다.
	 *****************************/
	@PostMapping("/post")
	public ResponseEntity<String> writePost(@RequestBody JobVO jobs, HttpServletRequest request) throws Exception{
		
		if (jobs == null) {
	        log.error("jobVO is null");
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("jobVO is null.");
	    }

	    // 유효성 검사
	    String validationError = validateJobVO(jobs);
	    if (validationError != null) {
	        log.error(validationError);
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validationError);
	    }

	    // 기본값 및 사용자 정보 세팅
	    preprocessJobVO(jobs, request);
		
		try {
			jobService.writePost(jobs);
			
			String post_id = jobs.getPost_id().toString();
			
			return new ResponseEntity<>(post_id,HttpStatus.OK);
		} catch (Exception e) {
			log.error("구인 글 작성 데이터 저장 중 오류 : " + e);
			
			String responseBody = e.getMessage();
			return new ResponseEntity<>(responseBody, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	private String validateJobVO(JobVO jobs) {
	    if (jobs.getTitle() == null || jobs.getTitle().isEmpty()) {
	        return "제목은 필수 입력 항목입니다.";
	    }
	    if (jobs.getContent() == null || jobs.getContent().isEmpty()) {
	        return "내용은 필수 입력 항목입니다.";
	    }
	    
	    if (jobs.getPosition() == null || jobs.getPosition().isEmpty()) {
	        return "포지션을 선택해주세요.";
	    }
	    
	    if(jobs.getCategory() == 0 && jobs.getPay() == null) {
	    	return "급여를 입력하세요.";
	    }
	    
	    if(jobs.getCity() == null || jobs.getCity().isEmpty()) {
	    	return "지역을 선택하세요.";
	    }
	    
	    return null;
	}

	private void preprocessJobVO(JobVO jobs, HttpServletRequest request) {
		jobs.setUser_id((String) request.getAttribute("userId"));
		jobs.setUser_name((String) request.getAttribute("userName"));

	    // gu, dong 기본값 처리
		jobs.setGu(ValueUtils.guNullToAll(jobs.getGu()));
		jobs.setDong(ValueUtils.emptyToNull(jobs.getDong()));
	    
		jobs.setPay(jobs.getPay());
	}
	
	/***********************************
	 * 구인 글을 수정하는 메서드 입니다.
	 * @param JobVO job_vo  수정할 글 번호, 제목, 내용, 카테고리, 급여 지불 방법, 급여
	 * @return HTTP 상태 코드
	 * 			성공 시 HttpStatus.OK를 반환하고 실패 시 HttpStatus.INTERNAL_SERVER_ERROR를 반환합니다.
	 ***********************************/
	@PutMapping("/post/{postId}")
	public ResponseEntity<String> editPost(@PathVariable Long postId, @RequestBody JobVO jobs, HttpServletRequest request) throws Exception{
		if (postId == null || postId <= 0) {
			throw new IllegalArgumentException("유효하지 않은 게시글 ID입니다.");
		}

		if (jobs == null) {
	        log.error("요청 본문이 비어 있습니다. (Jobs 데이터 누락)"); 
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("서버가 요청 데이터를 처리할 수 없습니다. 요청 형식을 확인해주세요.");
	    }

		jobs.setPost_id(postId);
	    // 유효성 검사
	    String validationError = validateJobVO(jobs);
	    if (validationError != null) {
	        log.error(validationError);
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validationError);
	    }
	    
	    // 기본값 및 사용자 정보 세팅
	    preprocessJobVO(jobs, request);
	    try {
			jobService.editPost(jobs);
			String post_id = jobs.getPost_id().toString();
			
			return new ResponseEntity<>(post_id, HttpStatus.OK);
		} catch(Exception e) {
			log.error("Jobs editBoard 데이터 수정 중 오류 : " + e.getMessage());
			String responseBody = "시스템 오류가 발생했습니다. 잠시 후 다시 시도하세요.";
			
			return new ResponseEntity<>(responseBody, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	

	/**********************************
	 * 구인 글을 삭제하는 메서드 입니다.
	 * @param Long post_id 삭제할 글 번호
	 * @return HTTP 상태 코드
	 * 			성공 시 HttpStatus.OK를 반환하고 실패 시 HttpStatus.INTERNAL_SERVER_ERROR를 반환합니다.
	 **********************************/
	@DeleteMapping("/post/{postId}")
	public ResponseEntity<Void> deletePost(@PathVariable Long postId, HttpServletRequest request) {

		String user_id = (String)request.getAttribute("userId");
		if(user_id == null) {
			log.error("Unauthorized request: 사용자 아이디가 없습니다.");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		
		if (postId == null) { 
			log.error("Missing required parameter: postId in deletePost()");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}
	
		jobService.deletePost(postId, user_id);
		
		return new ResponseEntity<>(HttpStatus.OK);
	}
	
	/**
	 * 공고 마감 API
	 *
	 * 로그인한 사용자가 자신이 작성한 공고를 마감 처리합니다.
	 * 마감 시 해당 공고에는 더 이상 지원할 수 없습니다.
	 * 
	 * @param postId	마감할 공고 ID
	 * @param request	HttpServletRequest (userId 추출용)
	 * @return	처리 성공 시 204 No Content 
	 */
	@PatchMapping("/post/{postId}")
	public ResponseEntity<Void> closeJob(@PathVariable Long postId, HttpServletRequest request) {
		String user_id = (String)request.getAttribute("userId");
		if(user_id == null) {
			log.error("Unauthorized request: 사용자 아이디가 없습니다.");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		
		if (postId == null) { 
			log.error("Missing required parameter: postId in closeJob()");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}
	
		jobService.closePost(postId, user_id);
		
		return ResponseEntity.noContent().build();
	}

	/**
	 * 지원서 생성 API
	 * 요청 본문(ApplicationVO)에 담긴 정보로 지원서를 생성합니다.
	 * post_id로 company_id를 조회해 설정하고 인증 컨텍스트(request.userId)에서 사용자 ID를 추출하여 저장합니다.
	 *
	 * @param app		지원서 데이터(ApplicationVO, @Valid)
	 * @param request	HttpServletRequest, userId 추출용
	 * @return			HTTP 200 OK (본문 없음) — 생성 성공
	 */
	@PostMapping("/applications")
	public ResponseEntity<String> createApplication(
			@Valid @RequestBody ApplicationVO app,
			HttpServletRequest request){
		
		app.setUser_id((String)request.getAttribute("userId"));
		
		jobService.createApplication(app);
		
		return ResponseEntity.ok(null);
	}
	
	/**
	 * 작성한 jobs 글 조회 API
	 * 요청 파라미터(JobVO)에 따라 내가 작성한 채용공고 목록을 조회하고 페이징 정보를 함께 반환합니다.
	 * 
	 * @param jobs	요청 파라미터를 담은 VO 객체
	 * @param request	HttpServletRequest, userId 추출용
	 * @return jobList(채용공고 목록), pageMaker(페이징 정보)
	 */
	@GetMapping("/my/posts")
	public ResponseEntity<Map<String, Object>> getPostings(JobVO jobs, HttpServletRequest request){		
		try {
			String userId = (String)request.getAttribute("userId");
			if(userId == null ||userId.isEmpty()) throw new UnauthorizedException("인증되지 않은 사용자 입니다.");
			jobs.setUser_id(userId); 
			
			if (jobs.getPositions() == null) jobs.setPositions(Collections.emptyList());

			Map<String, Object> result = new HashMap<>();
			List<JobVO> postings = new ArrayList<>();
			
			@SuppressWarnings("unchecked")
			List<String> roles = (List<String>) request.getAttribute("roles");
			
			if (roles.contains("ROLE_USER")) {
				postings = jobService.getMyRecruitPosts(jobs);
			}else if (roles.contains("ROLE_COMPANY")) {
				postings = jobService.getMyJobPosts(jobs);
			}
			
			result.put("postings", postings);
			
			int total = jobService.getMyPostCnt(jobs);
			PageDTO pageMaker = new PageDTO(jobs, total);
	        result.put("pageMaker", pageMaker);

	        return ResponseEntity.ok(result);
		}catch(UnauthorizedException e) {
			throw e;
		}catch(Exception e) {
			log.error(e.getMessage());
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(Collections.singletonMap("error", "An unexpected error occurred"));
		}
	}
	
	/**
	 * 지원서 상세 조회 API
	 *
	 * 로그인한 사용자가 특정 지원서(applicationId)를 조회합니다.
	 * 해당 지원서에 대한 접근 권한을 확인하며, 요청 사용자가 지원자이거나 해당 공고의 작성자인 경우에만 조회할 수 있습니다.
	 * category 값에 따라 반환되는 데이터 구조가 달라집니다.
	 *
	 * [회사 공고 지원서] (category = 0)
	 * - category: COMPANY
	 * - files: 이력서 파일 목록
	 * - app: 지원서 상세 정보 (title, content, created_at)
	 *
	 * [멤버 모집 지원서] (category = 1)
	 * - category: USER
	 * - app: 지원서 상세 정보 (title, content, created_at)
	 *
	 * @param applicationId 조회할 지원서 ID
	 * @param request       HttpServletRequest (userId 추출용)
	 * @return 지원서 상세 정보 (category, app, files 등 포함)
	 */
	@GetMapping("/applications/{applicationId}")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<Map<String, Object>> getApplication(@PathVariable Long applicationId, HttpServletRequest request){
		String userId = (String) request.getAttribute("userId");
		if(userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
		if(applicationId == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		
		Map<String, Object> result = jobService.getApplication(applicationId, userId);
		return ResponseEntity.ok(result);
	}
	
	/**
	 * 사용자의 지원한 공고 목록 조회 API 
	 * 요청 파라미터(ApplicationVO)에 따라 내가 지원한 채용공고 목록을 조회하고 페이징 정보를 함께 반환합니다.
	 * 
	 * @param app	요청 파라미터를 담은 VO 객체
	 * @param request	HttpServletRequest, userId 추출용
	 * @return apps(지원한 공고 목록), pageMaker(페이징 정보)
	 */
	@GetMapping("/my/applications")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<Map<String, Object>> getMyApplications(ApplicationVO app, HttpServletRequest request){
		String userId = (String) request.getAttribute("userId");
		if(userId == null) throw new UnauthorizedException("인증되지 않은 사용자입니다."); 

		app.setUser_id(userId);
		app.setKeyword(ValueUtils.sanitizeForLike(app.getKeyword()));
		
		try {
			Map<String, Object> result = new HashMap<>(); 
			
			List<Map<String, Object>> raw  = jobService.getMyApplications(app);
			
			List<Map<String,Object>> apps = raw.stream().map(row -> {
				Map<String,Object> m = new HashMap<>();
				row.forEach((k,v) -> m.put(k.toLowerCase(), v)); 
				return m;
			}).toList();
			
			result.put("apps", apps);
			
			log.info(apps);			
			int total = jobService.getMyApplicationsCnt(app);
			PageDTO pageMaker = new PageDTO(app, total);
	        result.put("pageMaker", pageMaker);

	        return ResponseEntity.ok(result);
		}catch(Exception e) {
			log.error("getMyApplications:" + e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}
	
}
