package com.jam.client.job.controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.google.gson.JsonObject;
import com.jam.client.job.service.JobService;
import com.jam.client.job.vo.JobVO;
import com.jam.common.vo.PageDTO;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j;

@Controller
@RequestMapping("/job")
@AllArgsConstructor
@Log4j
public class JobController {

	@Autowired
	private JobService jobService;
	
	/******************************
	 * @param JobVO job_vo
	 * @return 구인구직 글 리스트 페이지
	 ******************************/
	@RequestMapping(value="/boards", method=RequestMethod.GET)
	public String boards() {
		
		return "job/boards";
	}
	
	
	/**************************************************
	 * 구인구직 글의 상세 페이지를 반환하는 메서드 입니다.
	 * @param job_no 조회할 구인구직 글 번호
	 * @param model
	 * @return 구인구직 상세 페이지
	 **************************************************/
	@RequestMapping(value = "/board/{job_no}", method = RequestMethod.GET)
	public String jobDetail(@PathVariable("job_no") Long job_no, Model model) {
		model.addAttribute("job_no", job_no);
		
	    return "job/board";
	}
	
	
	/***************************************
	 * @return 구인구직 글 작성 페이지
	 ***************************************/
	@RequestMapping(value="/board/write", method=RequestMethod.GET)
	public String writeView() throws Exception{
		
		return "job/write";
	}
	
	/********************************
	 * 구인구직 글의 수정 페이지를 반환하는 메서드 입니다.
	 * @param job_no 수정할 구인구직 글 번호
	 * @return 구인구직 글 수정 페이지
	 *********************************/
	@RequestMapping(value="/board/edit/{job_no}", method=RequestMethod.GET)
	public String updateView(@PathVariable Long job_no, Model model){
	
		model.addAttribute("job_no", job_no);
		
		return "job/update";
	}
	
	/********************************
	 * 구인구직 사진 업로드
	 * @return String 사진 저장 경로
	 ********************************/
	@RequestMapping(value="/uploadImageFile", produces = "application/json; charset=utf8")
	@ResponseBody
	public String uploadImageFile(@RequestParam("file") MultipartFile multipartFile, HttpServletRequest request )  {
		JsonObject jsonObject = new JsonObject();
		
		// 내부경로로 저장
		String contextRoot = new HttpServletRequestWrapper(request).getRealPath("/");
		String fileRoot = contextRoot+"resources/fileupload/";
		
		String originalFileName = multipartFile.getOriginalFilename();	//오리지날 파일명
		String extension = originalFileName.substring(originalFileName.lastIndexOf("."));	//파일 확장자
		String savedFileName = UUID.randomUUID() + extension;	//저장될 파일 명
		
		File targetFile = new File(fileRoot + savedFileName);	
		try {
			InputStream fileStream = multipartFile.getInputStream();
			FileUtils.copyInputStreamToFile(fileStream, targetFile);	//파일 저장
			jsonObject.addProperty("url", "/resources/fileupload/"+savedFileName); // contextroot + resources + 저장할 내부 폴더명
			jsonObject.addProperty("responseCode", "success");
				
		} catch (IOException e) {
			FileUtils.deleteQuietly(targetFile);	//저장된 파일 삭제
			jsonObject.addProperty("responseCode", "error");
			e.printStackTrace();
		}
		return jsonObject.toString();
	}
	
	/*********************************
	 * @return 특정 회원의 구인구직 글 페이지 
	 *********************************/
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
		
		return "job/jobPosts";
	}
	
}
