package com.jam.client.job.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.jam.client.job.service.JobService;
import com.jam.client.job.vo.JobVO;
import com.jam.common.vo.PageDTO;

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
	@RequestMapping(value = "/post/{post_id}", method = RequestMethod.GET)
	public String jobDetail(@PathVariable("post_id") Long post_id, Model model) {
		model.addAttribute("post_id", post_id);
		
	    return "jobs/post";
	}
	
	
	/***************************************
	 * @return 구인구직 글 작성 페이지
	 ***************************************/
	@RequestMapping(value="/board/write", method=RequestMethod.GET)
	public String writeView() throws Exception{
		
		return "jobs/write";
	}
	
	/********************************
	 * 구인구직 글의 수정 페이지를 반환하는 메서드 입니다.
	 * @param job_no 수정할 구인구직 글 번호
	 * @return 구인구직 글 수정 페이지
	 *********************************/
	@RequestMapping(value="/board/edit/{post_id}", method=RequestMethod.GET)
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
	
}
