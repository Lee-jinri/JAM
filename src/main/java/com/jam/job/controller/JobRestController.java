package com.jam.job.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jam.common.dto.PageDto;
import com.jam.global.exception.BadRequestException;
import com.jam.global.exception.ForbiddenException;
import com.jam.global.exception.UnauthorizedException;
import com.jam.global.jwt.JwtService;
import com.jam.global.util.HtmlSanitizer;
import com.jam.global.util.ValidationUtils;
import com.jam.global.util.ValueUtils;
import com.jam.job.dto.ApplicationDto;
import com.jam.job.dto.JobDto;
import com.jam.job.service.JobService;
import com.jam.member.dto.MemberDto;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@Slf4j
public class JobRestController {

	private final JobService jobService;
	private final JwtService jwtService;

	/**
	 * jobs 게시판 조회 API
	 * 요청 파라미터(JobDto)에 따라 채용공고 목록을 조회하고 페이징 정보를 함께 반환합니다.
	 *
	 * @param jobs	요청 파라미터를 담은 DTO 객체
	 * @param user	현재 로그인한 사용자 (nullable, 각 게시글의 즐겨찾기(별표) 활성화 여부를 판단하는 데 사용)
	 * @return		jobList(채용공고 목록), pageMaker(페이징 정보)
	 */
	@GetMapping(value = "/board")
	public ResponseEntity<Map<String, Object>> getBoard(JobDto jobs, HttpServletRequest request, @AuthenticationPrincipal MemberDto user){

		if (user != null) {
			jobs.setUser_id(user.getUser_id());
		}
		
		if (jobs.getPositions() == null) jobs.setPositions(Collections.emptyList());
		
		String kw = jobs.getKeyword();
		jobs.setKeyword(ValueUtils.sanitizeForLike(kw));
		
		Map<String, Object> result = new HashMap<>();

		List<JobDto> jobList = jobService.getBoard(jobs);
		result.put("jobList", jobList);
		
		int total = jobService.listCnt(jobs);
		PageDto pageMaker = new PageDto(jobs, total);
        result.put("pageMaker", pageMaker);

        return ResponseEntity.ok(result);
	}
	
	
	/*************************************
	 * 구인 글 상세정보를 조회하는 메서드입니다.
	 *
	 * @param post_id 	조회할 구인 글의 번호
	 * @param user		현재 로그인한 사용자 (nullable, 각 게시글의 즐겨찾기(별표) 활성화 여부를 판단하는 데 사용)
	 * @return ResponseEntity<JobDto> - 조회된 구인 글의 정보와 HTTP 상태 코드를 포함한 응답 VO
	 ************************************/
	@GetMapping(value = "/post/{post_id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, Object>> getPost(
			@PathVariable("post_id") Long post_id, 
			@AuthenticationPrincipal MemberDto user) {
		post_id = ValidationUtils.requireValidId(post_id);
		String currentUserId = (user != null) ? user.getUser_id() : null;
		
		Map<String, Object> result = new HashMap<>();
		
		JobDto post = jobService.getPost(post_id, currentUserId);
		
		post.setPosition(getTranslatedPosition(post.getPosition()));
		result.put("post", post);
		result.put("isAuthor", Objects.equals(currentUserId, post.getUser_id()));
		
		return ResponseEntity.ok(result);
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
	 * 
	 * @param request  사용자 인증 정보(userId) 추출용 HttpServletRequest
	 * @param JobDto jobs 제목과 내용, 카테고리, 급여 지불 방법, 급여
	 * @return HTTP 상태 코드
	 * 			성공 시 HttpStatus.OK를 반환하고 실패 시 HttpStatus.INTERNAL_SERVER_ERROR를 반환합니다.
	 *****************************/
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/post")
	public ResponseEntity<String> writePost(@RequestBody JobDto jobs, @AuthenticationPrincipal MemberDto user) throws Exception{

	    // 기본값 및 사용자 정보 세팅
	    preprocessJobDto(jobs, user);
	    
		if (jobs == null) {
	        log.error("JOBS writePost JobDto is null");
	        throw new BadRequestException("시스템 오류입니다. 잠시 후 다시 시도하세요.");
	    }

	    // 유효성 검사
	    String validationError = validateJobDto(jobs);
	    if (validationError != null) {
	        throw new BadRequestException(validationError);
	    }
		
		jobService.writePost(jobs);
		
		String post_id = jobs.getPost_id().toString();
		
		return new ResponseEntity<>(post_id,HttpStatus.OK);
	}
	
	private String validateJobDto(JobDto jobs) {
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

	private void preprocessJobDto(JobDto jobs, MemberDto user) {
		if(user == null || user.getUser_id().isEmpty()) throw new UnauthorizedException("로그인이 필요한 서비스입니다.");
		
		jobs.setUser_id(user.getUser_id());
		
	    // gu, dong 기본값 처리
		jobs.setGu(ValueUtils.guNullToAll(jobs.getGu()));
		jobs.setDong(ValueUtils.emptyToNull(jobs.getDong()));
	    
		jobs.setPay(jobs.getPay());

	    jobs.setTitle(HtmlSanitizer.sanitizeTitle(jobs.getTitle()));
	    jobs.setContent(HtmlSanitizer.sanitizeHtml(jobs.getContent()));
	}
	
	/***********************************
	 * 구인 글을 수정하는 메서드 입니다.
	 * @param JobDto job_vo  수정할 글 번호, 제목, 내용, 카테고리, 급여 지불 방법, 급여
	 * @return HTTP 상태 코드
	 * 			성공 시 HttpStatus.OK를 반환하고 실패 시 HttpStatus.INTERNAL_SERVER_ERROR를 반환합니다.
	 ***********************************/
	@PreAuthorize("isAuthenticated()")
	@PutMapping("/post/{postId}")
	public ResponseEntity<String> editPost(@PathVariable Long postId, @RequestBody JobDto jobs, @AuthenticationPrincipal MemberDto user) throws Exception{
		
		if (jobs == null) {
	        log.error("JOBS editPost: 요청 본문이 비어 있습니다. (Jobs 데이터 누락)"); 
		    throw new BadRequestException("시스템 오류입니다. 잠시 후 다시 시도하세요.");
		}

	    // 기본값 및 사용자 정보 세팅
	    preprocessJobDto(jobs, user);

		postId = ValidationUtils.requireValidId(postId);
		jobs.setPost_id(postId);
		
	    // 유효성 검사
	    String validationError = validateJobDto(jobs);
	    if (validationError != null) {
	        log.error(validationError);
	        throw new BadRequestException(validationError);
	    }
    
		jobService.editPost(jobs);
		String post_id = jobs.getPost_id().toString();
		
		return new ResponseEntity<>(post_id, HttpStatus.OK);
	}
	

	/**********************************
	 * 구인 글을 삭제하는 메서드 입니다.
	 * @param Long post_id 삭제할 글 번호
	 * @return HTTP 상태 코드
	 * 			성공 시 HttpStatus.OK를 반환하고 실패 시 HttpStatus.INTERNAL_SERVER_ERROR를 반환합니다.
	 **********************************/
	@PreAuthorize("isAuthenticated()")
	@DeleteMapping("/post/{postId}")
	public ResponseEntity<Void> deletePost(@PathVariable Long postId, @AuthenticationPrincipal MemberDto user) {
		
		String userId = ValidationUtils.requireLogin(user.getUser_id());
		postId = ValidationUtils.requireValidId(postId);
		
		jobService.deletePost(postId, userId);
		
		return ResponseEntity.ok().build();
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
	@PreAuthorize("isAuthenticated()")
	@PatchMapping("/post/{postId}")
	public ResponseEntity<Void> closeJob(@PathVariable Long postId, @AuthenticationPrincipal MemberDto user) {

		String userId = ValidationUtils.requireLogin(user.getUser_id());
		postId = ValidationUtils.requireValidId(postId);
		
		jobService.closePost(postId, userId);

		return ResponseEntity.ok().build();
	}

	/**
	 * 지원서 생성 API
	 * 요청 본문(ApplicationDto)에 담긴 정보로 지원서를 생성합니다.
	 * post_id로 company_user_id를 조회해 설정하고 인증 컨텍스트(request.userId)에서 사용자 ID를 추출하여 저장합니다.
	 *
	 * @param app		지원서 데이터(ApplicationDto, @Valid)
	 * @param request	HttpServletRequest, userId 추출용
	 * @return			HTTP 200 OK (본문 없음) — 생성 성공
	 */
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/applications")
	public ResponseEntity<String> createApplication(
			@Valid @RequestBody ApplicationDto app,
			@AuthenticationPrincipal MemberDto user){
		String userId = ValidationUtils.requireLogin(user.getUser_id());
		app.setUser_id(userId);
		jobService.createApplication(app);

		return ResponseEntity.ok().build();
	}
	
	/**
	 * 작성한 jobs 글 조회 API
	 * 요청 파라미터(JobDto)에 따라 내가 작성한 채용공고 목록을 조회하고 페이징 정보를 함께 반환합니다.
	 * 
	 * @param jobs	요청 파라미터를 담은 VO 객체
	 * @param request	HttpServletRequest, userId 추출용
	 * @return jobList(채용공고 목록), pageMaker(페이징 정보)
	 */
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/my/posts")
	public ResponseEntity<Map<String, Object>> getPostings(
			JobDto jobs, 
			HttpServletRequest request,
			HttpServletResponse response,
			@CookieValue(name = "Authorization", required = true) String token,
			@AuthenticationPrincipal MemberDto user){		
		
		String userId = ValidationUtils.requireLogin(user.getUser_id());
		jobs.setUser_id(userId); 
		
		if (jobs.getPositions() == null) jobs.setPositions(Collections.emptyList());

		Map<String, Object> result = new HashMap<>();
		List<JobDto> postings = new ArrayList<>();
		
		// FIXME: 이거 되는지 확인 필요
		List<String> roles = jwtService.extractUserRole(request, response, token);
		
		if (roles.contains("ROLE_COMPANY")) {
			postings = jobService.getMyJobPosts(jobs);
		}else if (roles.contains("ROLE_USER")) {
			postings = jobService.getMyRecruitPosts(jobs);
		}
		
		result.put("postings", postings);
		
		int total = jobService.getMyPostCnt(jobs);
		PageDto pageMaker = new PageDto(jobs, total);
        result.put("pageMaker", pageMaker);

        return ResponseEntity.ok(result);
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
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/applications/{applicationId}")
	public ResponseEntity<Map<String, Object>> getApplication(@PathVariable Long applicationId, @AuthenticationPrincipal MemberDto user){

		String userId = ValidationUtils.requireLogin(user.getUser_id());
		applicationId = ValidationUtils.requireValidId(applicationId);
		
		Map<String, Object> result = jobService.getApplication(applicationId, userId);
		return ResponseEntity.ok(result);
	}
	
	/**
	 * 사용자의 지원한 공고 목록 조회 API 
	 * 요청 파라미터(ApplicationDto)에 따라 내가 지원한 채용공고 목록을 조회하고 페이징 정보를 함께 반환합니다.
	 * 
	 * @param app	요청 파라미터를 담은 VO 객체
	 * @param request	HttpServletRequest, userId 추출용
	 * @return apps(지원한 공고 목록), pageMaker(페이징 정보)
	 */
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/my/applications")
	public ResponseEntity<Map<String, Object>> getMyApplications(ApplicationDto app, @AuthenticationPrincipal MemberDto user){

		String userId = ValidationUtils.requireLogin(user.getUser_id());
		app.setUser_id(userId);
		app.setKeyword(ValueUtils.sanitizeForLike(app.getKeyword()));
		
		Map<String, Object> result = new HashMap<>(); 
		
		List<Map<String, Object>> raw  = jobService.getMyApplications(app);
		
		List<Map<String,Object>> apps = raw.stream().map(row -> {
			Map<String,Object> m = new HashMap<>();
			row.forEach((k,v) -> m.put(k.toLowerCase(), v)); 
			return m;
		}).toList();
		
		result.put("apps", apps);
				
		int total = jobService.getMyApplicationsCnt(app);
		PageDto pageMaker = new PageDto(app, total);
        result.put("pageMaker", pageMaker);

        return ResponseEntity.ok(result);
	}

	/**
	 * 지원 취소 API
	 * 
	 * @param applicationId	지원 취소할 지원서 ID
	 * @param request	HttpServletRequest, userId 추출용	
	 * @return	Http 상태코드
	 */
	@PreAuthorize("isAuthenticated()")
	@DeleteMapping("/applications/{applicationId}/withdraw")
	public ResponseEntity<Void>  withdrawApplication(@PathVariable Long applicationId, @AuthenticationPrincipal MemberDto user){

		String userId = ValidationUtils.requireLogin(user.getUser_id());
		applicationId = ValidationUtils.requireValidId(applicationId);
		
		jobService.withdrawApplication(applicationId, userId);
		
		return ResponseEntity.ok().build();
	}

	/**
	 * 특정 공고의 지원자 조회 API
	 * 요청 파라미터(JobDto)에 따라 특정 공고의 지원자를 조회하고 페이징 정보를 함께 반환합니다.
	 * 
	 * @param jobs	요청 파라미터를 담은 VO 객체
	 * @param request	HttpServletRequest, userId 추출용
	 * @return apps(지원자 목록), pageMaker(페이징 정보)
	 */
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/candidates")
	public ResponseEntity<Map<String, Object>> getcandidates(ApplicationDto application, @AuthenticationPrincipal MemberDto user){		

		String userId = ValidationUtils.requireLogin(user.getUser_id());
		Long postId = ValidationUtils.requireValidId(application.getPost_id());
		
		String writer = jobService.findCompanyIdByPostId(postId);
		if (writer == null || !writer.equals(userId)) throw new ForbiddenException("해당 정보를 조회할 권한이 없습니다.");
		
		Map<String, Object> result = new HashMap<>();
		
		List<ApplicationDto> apps = jobService.getApplicationsByPostId(postId);
		result.put("apps", apps);
		
		int total = jobService.applicationsListCnt(application);
		PageDto pageMaker = new PageDto(application, total);
		result.put("pageMaker", pageMaker);
		
        return ResponseEntity.ok(result);
	}
	
	/**
	 * 사용자가 스크랩한 글 목록 조회 API
	 * 
	 * @param job	요청 파라미터를 담은 VO 객체	
	 * @param request	HttpServletRequest, userId 추출용	
	 * @return favorites(스크랩한 글 목록), pageMaker(페이징 정보)
	 */
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/my/favorites")
	public ResponseEntity<Map<String, Object>> getMyFavorites(JobDto job, @AuthenticationPrincipal MemberDto user){

		String userId = ValidationUtils.requireLogin(user.getUser_id());
		job.setUser_id(userId);
		job.setKeyword(ValueUtils.sanitizeForLike(job.getKeyword()));
		
		Map<String, Object> result = new HashMap<>(); 
		
		List<Map<String, Object>> raw  = jobService.getMyFavorites(job);
		
		List<Map<String,Object>> favorites = raw.stream().map(row -> {
			Map<String,Object> m = new HashMap<>();
			row.forEach((k,v) -> m.put(k.toLowerCase(), v)); 
			return m;
		}).toList();
		
		result.put("favorites", favorites);
		
		int total = jobService.getMyFavoritesCnt(job);
		PageDto pageMaker = new PageDto(job, total);
        result.put("pageMaker", pageMaker);

        return ResponseEntity.ok(result);
	}
}
