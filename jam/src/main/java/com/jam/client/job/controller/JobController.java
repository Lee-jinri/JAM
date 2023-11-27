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
import org.springframework.web.bind.annotation.ModelAttribute;
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
	@RequestMapping(value="/jobList", method=RequestMethod.GET)
	public String jobList(Model model, @ModelAttribute JobVO job_vo) {
		
		List<JobVO> jobList = jobService.jobList(job_vo);
		model.addAttribute("jobList",jobList);
		
		// 페이징 처리
		int total = jobService.jobListCnt(job_vo);
		model.addAttribute("pageMaker", new PageDTO(job_vo, total));
		return "job/jobList";
	}
	
	/****************************************
	 * @param JobVO job_vo
	 * @return 구인구직 상세페이지
	 *****************************************/
	@RequestMapping(value="/jobDetail/{job_no}", method=RequestMethod.GET)
	public String jobDetail(@ModelAttribute("data")JobVO job_vo, Model model) throws Exception{
		
		// 구인구직 조회수 증가
		jobService.jobReadCnt(job_vo);
		
		JobVO detail = jobService.boardDetail(job_vo);
		
		model.addAttribute("detail",detail);
		
		return "job/jobDetail";
	}
	
	/***************************************
	 * @return 구인구직 글 작성 페이지
	 ***************************************/
	@RequestMapping(value="/jobWrite", method=RequestMethod.GET)
	public String jobWriteForm() throws Exception{
		
		return "job/jobWrite";
	}
	
	/******************************
	 * 구인구직 글 작성
	 * @param MemberVO member
	 * @param JobVO job_vo
	 * @return 성공 시 작성한 구인구직 글 상세 페이지 / 실패 시 구인구직 글 작성 페이지
	 *****************************/
	@RequestMapping(value="/jobWrite", method=RequestMethod.POST)
	public ModelAndView jobWrite(RedirectAttributes rttr, @ModelAttribute("data") JobVO job_vo, Model model) throws Exception{
	
		ModelAndView mav = new ModelAndView();
		
		try {
			jobService.jobInsert(job_vo);
			
			mav.setViewName("redirect:/job/jobDetail/"+job_vo.getJob_no());
			
			return mav;
		}catch(Exception e) {
			log.error("jobWrite 데이터 저장 중 오류 : " + e.getMessage());
			
			rttr.addFlashAttribute("result", "error");
			mav.setViewName("redirect:/job/jobWrite");
			return mav;
		}
	}
	
	/********************************
	 * @param MemberVO member
	 * @param Job_VO job_vo
	 * @return 구인구직 글 수정 페이지
	 *********************************/
	@RequestMapping(value="/jobUpdateForm", method=RequestMethod.GET)
	public ModelAndView jobUpdateForm(JobVO job_vo, Model model) throws Exception{
	
		ModelAndView mav = new ModelAndView();
		
		JobVO updateData = jobService.jobUpdateForm(job_vo);
		model.addAttribute("updateData", updateData);
		
		mav.setViewName("job/jobUpdate");
		return mav;
	}
	
	/***********************************
	 * 구인구직 글 수정
	 * @param MemberVO member
	 * @param Job_vo job_vo
	 * @return 성공 시 수정한 구인구직 글 상세페이지 / 실패 시 구인구직 글 수정 페이지
	 ***********************************/
	@RequestMapping(value="/jobUpdate", method=RequestMethod.POST)
	public ModelAndView jobUpdate(RedirectAttributes rttr, JobVO job_vo, Model model) throws Exception{
		
		ModelAndView mav = new ModelAndView();
		
		try {
			jobService.jobUpdate(job_vo);

			mav.setViewName("redirect:/job/jobDetail/"+job_vo.getJob_no());
			return mav;
		}catch(Exception e) {
			log.error("jobUpdate 데이터 수정 중 오류 : " + e.getMessage());
			
			rttr.addFlashAttribute("result", "error");
			mav.setViewName("redirect:/job/jobUpdate");
			
			return mav;
		}
	}
	
	/**********************************
	 * 구인구직 글 삭제
	 * @param MemberVO member
	 * @param Job_VO job_vo
	 * @return 성공 시 구인구직 글 목록 / 실패 시 구인구직 글 상세페이지
	 **********************************/
	@RequestMapping(value="/jobDelete", method=RequestMethod.POST)
	public ModelAndView jobDelete(RedirectAttributes rttr, @ModelAttribute("data") JobVO job_vo, Model model) throws Exception{
		
		ModelAndView mav = new ModelAndView();
		
		try {
			jobService.jobDelete(job_vo);
			
			mav.setViewName("redirect:/job/jobList");
			return mav;
		}catch(Exception e) {
			log.error("jobDelete 데이터 수정 중 오류 : " + e.getMessage());
			
			rttr.addFlashAttribute("result", "error");
			mav.setViewName("redirect:/job/jobDetail/"+job_vo.getJob_no());
			return mav;
		}
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
}
