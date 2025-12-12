package com.jam.client.job.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.jam.client.job.service.JobService;
import com.jam.client.job.vo.JobVO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/jobs")
@RequiredArgsConstructor
@Slf4j
public class JobController {

	private final JobService jobService;
	
	/******************************
	 * @param JobVO job_vo
	 * @return 구인구직 글 리스트 페이지
	 ******************************/

	@GetMapping("/board")
	public String boards() {
		
		return "jobs/board";
	}
	
	
	/**************************************************
	 * 구인구직 글의 상세 페이지를 반환하는 메서드 입니다.
	 * @param id 글 번호
	 * @param model
	 * @return 상세 페이지
	 **************************************************/
	@GetMapping("/post/{postId}")
	public String jobDetail(@PathVariable("postId") Long postId, Model model) {
		model.addAttribute("postId", postId);
		
	    return "jobs/post";
	}
	
	
	/***************************************
	 * @return 구인구직 글 작성 페이지
	 ***************************************/
	@GetMapping("/post/write")
	public String writeView() throws Exception{
		
		return "jobs/write";
	}
	
	/********************************
	 * 구인구직 글의 수정 페이지를 반환하는 메서드 입니다.
	 * @param job_no 수정할 구인구직 글 번호
	 * @return 구인구직 글 수정 페이지
	 *********************************/
	@GetMapping("/post/update/{post_id}")
	public String updateView(@PathVariable Long post_id, Model model){
	
		JobVO post = jobService.getPostById(post_id);
		model.addAttribute("post", post);
		
		return "jobs/update";
	}

	/**********************************
	 * @return 기업회원 전환 페이지
	 *********************************/
	@GetMapping(value="/toBusiness")
	public String convertToBusinessPage() {
		return "forward:/WEB-INF/views/jobs/toBusiness.jsp";
	}
	
	/**************************************
	 * @return 지원서 작성 폼
	 *************************************/
	@GetMapping(value="/applyForm/{postId}")
	public String applyFormPage(@PathVariable("postId") String postId, @RequestParam String category, Model model) {
		
	    model.addAttribute("postId", postId);
	    model.addAttribute("category", category);
	    
		return "forward:/WEB-INF/views/jobs/applyForm.jsp";
	}

	/**************************************
	 * @return 작성한 기업 공고/ 멤버 모집 글
	 *************************************/
	@GetMapping(value="/postsManage/{mode}")
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
	public String viewApplication(@PathVariable Long applicationId, Model model) {
		return "forward:/WEB-INF/views/jobs/application.jsp";
	}

	/***********************************
	 * @return 내가 지원한 공고/이력서
	 **********************************/
	@GetMapping(value="/my/applications")
	public String applicationsPage() {
		return "jobs/my/applications";
	}

	/***********************************
	 * @return 스크랩(즐겨찾기)
	 **********************************/
	@GetMapping(value="/my/favorites")
	public String favoritesPage() {
		return "jobs/my/favorites";
	}
	
}
