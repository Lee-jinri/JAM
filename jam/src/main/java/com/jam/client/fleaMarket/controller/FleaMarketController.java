package com.jam.client.fleaMarket.controller;

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
import com.jam.client.fleaMarket.service.FleaMarketService;
import com.jam.client.fleaMarket.vo.FleaMarketVO;
import com.jam.client.member.vo.MemberVO;
import com.jam.common.vo.PageDTO;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j;

@Controller
@RequestMapping("/fleaMarket")
@AllArgsConstructor
@Log4j

public class FleaMarketController {
	
	@Autowired
	private FleaMarketService fleaService;
	
	/**************************************
	 * @param FleaMarketVO flea_vo
	 * @return 중고악기 글 리스트 
	 ***************************************/
	@RequestMapping(value="/fleaMarketList", method=RequestMethod.GET)
	public String fleaMarketList(Model model, @ModelAttribute FleaMarketVO flea_vo) {
		
		List<FleaMarketVO> fleaMarketList = fleaService.fleaList(flea_vo);
		
		model.addAttribute("fleaMarketList",fleaMarketList);
		
		int total = fleaService.fleaListCnt(flea_vo);
		model.addAttribute("pageMaker", new PageDTO(flea_vo, total));
		return "fleaMarket/fleaMarketList";
	}
	
	/*****************************************
	 * @param FleaMarketVO flea_vo
	 * @return 중고악기 상세페이지
	 *****************************************/
	@RequestMapping(value="/fleaMarketDetail/{flea_no}", method=RequestMethod.GET)
	public ModelAndView fleaDetail(@ModelAttribute("data")FleaMarketVO flea_vo, Model model) throws Exception{
		
		// 조회수 증가
		fleaService.fleaReadCnt(flea_vo);
		
		FleaMarketVO detail = fleaService.fleaDetail(flea_vo);
		
		ModelAndView mav = new ModelAndView();
		
		mav.addObject("detail", detail);
		mav.setViewName("fleaMarket/fleaMarketDetail");
		
		return mav;
	}
	
	/***************************************
	 * @return 중고악기 글 작성 페이지
	 **************************************/
	@RequestMapping(value="/fleaWrite", method=RequestMethod.GET)
	public String fleaMarketWriteForm() {
		
		return "fleaMarket/fleaMarketWrite";
	}
	
	/***********************************
	 * 중고 악기 글 작성
	 * @param MemberVO member
	 * @param FleaMarketVO flea_vo
	 * @return 성공 시 작성한 중고악기 글 상세 페이지 / 실패 시 중고악기 글 작성 페이지
	 **********************************/
	@RequestMapping(value="/fleaWrite", method=RequestMethod.POST)
	public ModelAndView fleaMarketWrite(MemberVO member, RedirectAttributes rttr, @ModelAttribute("data") FleaMarketVO flea_vo, Model model) {
		
		ModelAndView mav = new ModelAndView();
		
		try {
			fleaService.fleaInsert(flea_vo);
			
			mav.setViewName("redirect:/fleaMarket/fleaMarketDetail/"+flea_vo.getFlea_no());
			return mav;
		}catch (Exception e){
			log.error("fleaWrite 데이터 저장 중 오류 : " + e.getMessage());
			rttr.addFlashAttribute("result","error");
			mav.setViewName("redirect:/fleaMarket/fleaMarketWrite");
			
			return mav;
		}
	}
	
	/******************************
	 * @param MemberVO member
	 * @param FleaMarketVO flea_vo
	 * @return 중고악기 글 수정 페이지
	 ******************************/
	@RequestMapping(value="/fleaUpdateForm", method=RequestMethod.GET)
	public ModelAndView fleaMarketUpdateForm(FleaMarketVO flea_vo, Model model) throws Exception{
	
		ModelAndView mav = new ModelAndView();
		
		FleaMarketVO updateData = fleaService.fleaUpdateForm(flea_vo);
		
		model.addAttribute("updateData", updateData);
		
		mav.setViewName("fleaMarket/fleaMarketUpdate");
		return mav;
	}
	
	/********************************************************
	 * 중고악기 글 수정
	 * @param MemberVO member
	 * @param FleaMarket_VO flea_vo
	 * @return 성공 시 수정한 중고악기 글 상세 페이지 / 실패 시 중고악기 글 수정 페이지
	 *******************************************************/
	@RequestMapping(value="/fleaUpdate", method=RequestMethod.POST)
	public ModelAndView fleaMarketUpdate(RedirectAttributes rttr, @ModelAttribute("data") FleaMarketVO flea_vo, Model model) {
		
		ModelAndView mav = new ModelAndView();
		
		try {
			fleaService.fleaUpdate(flea_vo);
			
			mav.setViewName("redirect:/fleaMarket/fleaMarketDetail/"+flea_vo.getFlea_no());
			return mav;
		}catch(Exception e) {
			log.error("fleaUpdate 데이터 수정 중 오류 : " + e.getMessage());
			
			rttr.addFlashAttribute("result","error");
			mav.setViewName("redirect:/fleaMarket/fleaMarketUpdate");
			return mav;
		}
	}
	
	/*********************************
	 * 중고 악기 글 삭제 
	 * @param MemberVO member
	 * @param FleaMarket_VO flea_vo
	 * @return 성공 시 중고악기 글 목록 / 실패 시 중고악기 글 상세페이지
	 ***********************************/
	@RequestMapping(value="/fleaDelete", method=RequestMethod.POST)
	public ModelAndView fleaMarketDelete(RedirectAttributes rttr, @ModelAttribute("data") FleaMarketVO flea_vo, Model model) throws Exception{
		
		ModelAndView mav = new ModelAndView();
		
		try {
			 fleaService.fleaDelete(flea_vo);
			 
			 mav.setViewName("redirect:/fleaMarket/fleaMarketList");
			 return mav;
		}catch(Exception e) {
			log.error("fleaDelete 데이터 삭제 중 오류 : " + e.getMessage());
			
			rttr.addFlashAttribute("result", "error");
			mav.setViewName("redirect:/fleaMarket/fleaMarketDetail/"+flea_vo.getFlea_no());
			return mav;
		}
	}
	

	/********************************
	 * 중고악기 사진 업로드
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
