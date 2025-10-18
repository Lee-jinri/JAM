package com.jam.client.job.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.jam.client.job.service.JobService;
import com.jam.client.job.vo.JobVO;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;

@Controller
@RequestMapping("/jobs")
@RequiredArgsConstructor
@Log4j
public class JobController {

	private final JobService jobService;
	
	/******************************
	 * @param JobVO job_vo
	 * @return 구인구직 글 리스트 페이지
	 ******************************/
	@RequestMapping(value="/board", method=RequestMethod.GET)
	public String boards() {
		
		return "jobs/board";
	}
	
	
	/**************************************************
	 * 구인구직 글의 상세 페이지를 반환하는 메서드 입니다.
	 * @param id 글 번호
	 * @param model
	 * @return 상세 페이지
	 **************************************************/
	@RequestMapping(value = "/post/{postId}", method = RequestMethod.GET)
	public String jobDetail(@PathVariable("postId") Long postId, Model model) {
		model.addAttribute("postId", postId);
		
	    return "jobs/post";
	}
	
	
	/***************************************
	 * @return 구인구직 글 작성 페이지
	 ***************************************/
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(value="/post/write", method=RequestMethod.GET)
	public String writeView() throws Exception{
		
		return "jobs/write";
	}
	
	/********************************
	 * 구인구직 글의 수정 페이지를 반환하는 메서드 입니다.
	 * @param job_no 수정할 구인구직 글 번호
	 * @return 구인구직 글 수정 페이지
	 *********************************/
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(value="/post/edit/{post_id}", method=RequestMethod.GET)
	public String updateView(@PathVariable Long post_id, Model model){
	
		model.addAttribute("post_id", post_id);
		JobVO post = jobService.getPostById(post_id);
		model.addAttribute("post", post);
		
		return "jobs/update";
	}
	
	/*********************************
	 * @return 특정 회원의 구인구직 글 페이지 
	 *********************************/
	// FIXME: 기업회원 확인 + 구인 글 목록
	@GetMapping(value="/jobPosts") 
	public String viewPosts(@ModelAttribute JobVO job_vo, Model model, HttpServletRequest request) throws Exception {
		
		if(job_vo.getUser_name() == null || jobService.isValidUserName(job_vo.getUser_name()))
			job_vo.setUser_id((String) request.getAttribute("userId"));
		else 
			job_vo.setUser_id(jobService.getUserId(job_vo.getUser_name()));
		
		List<JobVO> jobPosts = jobService.getPosts(job_vo);
		
		model.addAttribute("jobPosts",jobPosts);
		
		int total = jobService.getUserPostCnt(job_vo);
		
		PageDTO pageMaker = new PageDTO(job_vo, total);
		
		model.addAttribute("pageMaker",pageMaker);
		
		return "jobs/jobPosts";
	}
	
	@GetMapping(value="/toBusiness")
	@PreAuthorize("isAuthenticated() and hasRole('USER')")
	public String convertToBusinessPage() {
		return "forward:/WEB-INF/views/jobs/toBusiness.jsp";
	}
	
	/**************************************
	 * @return 지원서 작성 폼
	 *************************************/
	@GetMapping(value="/applyForm/{postId}")
	@PreAuthorize("isAuthenticated() and hasRole('USER')")
	public String applyFormPage(@PathVariable("postId") String postId, @RequestParam String category, Model model) {
		
	    model.addAttribute("postId", postId);
	    model.addAttribute("category", category);
	    
		return "forward:/WEB-INF/views/jobs/applyForm.jsp";
	}

	/**************************************
	 * @return 작성한 기업 공고/ 멤버 모집 글
	 *************************************/
	@GetMapping(value="/postsManage/{mode}")
	@PreAuthorize("isAuthenticated()")
	public String postsManagePage(@PathVariable String mode, Model model) {
		model.addAttribute("mode", mode);
		return "jobs/postsManage";
	}
	
	/**********************************
	 * 지원서 상세 페이지 확인
	 * @param applicationId 지원서 ID
	 * @return 지원서 상세 JSP
	 *********************************/
	@GetMapping(value="/applications/{applicationId}")
	@PreAuthorize("isAuthenticated()")
	public String viewApplication(@PathVariable Long applicationId, Model model) {
		return "forward:/WEB-INF/views/jobs/application.jsp";
	}

	/***********************************
	 * @return 내가 지원한 공고/이력서
	 **********************************/
	@GetMapping(value="/my/applications")
	@PreAuthorize("isAuthenticated() and hasRole('USER')")
	public String applicationsPage() {
		return "jobs/my/applications";
	}

	/***********************************
	 * @return 스크랩(즐겨찾기)
	 **********************************/
	@GetMapping(value="/my/favorites")
	@PreAuthorize("isAuthenticated() and hasRole('USER')")
	public String favoritesPage() {
		return "jobs/my/favorites";
	}
	
}
