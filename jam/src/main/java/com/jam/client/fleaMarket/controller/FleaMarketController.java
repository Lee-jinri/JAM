package com.jam.client.fleaMarket.controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.JsonObject;
import com.jam.client.fleaMarket.service.FleaMarketService;
import com.jam.client.fleaMarket.vo.FleaMarketVO;
import com.jam.common.vo.PageDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;

@Controller
@RequestMapping("/fleaMarket")
@RequiredArgsConstructor
@Log4j

public class FleaMarketController {
	
	private final FleaMarketService fleaService;
	
	/**************************************
	 * @param FleaMarketVO flea_vo
	 * @return 중고악기 글 리스트 
	 ***************************************/
	@RequestMapping(value="/board", method=RequestMethod.GET)
	public String fleaMarketList(Model model, @ModelAttribute FleaMarketVO flea_vo) {
		
		List<FleaMarketVO> fleaMarketList = fleaService.getBoard(flea_vo);
		
		model.addAttribute("fleaMarketList",fleaMarketList);
		
		int total = fleaService.listCnt(flea_vo);
		model.addAttribute("pageMaker", new PageDTO(flea_vo, total));
		return "fleaMarket/board";
	}
	
	/**************************************************
	 * 중고악기 글의 상세 페이지를 반환하는 메서드 입니다.
	 * @param com_no 조회할 중고악기 글 번호
	 * @return 중고악기 상세 페이지
	 **************************************************/
	@RequestMapping(value = "/post/{post_id}", method = RequestMethod.GET)
	public String fleaDetail(@PathVariable("post_id") Long post_id, Model model) {
		model.addAttribute("post_id", post_id);
		
	    return "fleaMarket/post";
	}
	
	/***************************************
	 * @return 중고악기 글 작성 페이지
	 ***************************************/
	@RequestMapping(value="/board/write", method=RequestMethod.GET)
	public String writeView() throws Exception{
				
		return "fleaMarket/write";
	}
	
	/******************************
	 * 중고악기 글의 수정 페이지를 반환하는 메서드 입니다.
	 * @param flea_no 수정할 중고악기 글 번호
	 * @return 중고악기 글 수정 페이지
	 ******************************/
	@RequestMapping(value="/board/edit/{flea_no}", method=RequestMethod.GET)
	public String updateView(@PathVariable("flea_no") Long flea_no, Model model) throws Exception{
	
		model.addAttribute("flea_no",flea_no);
		
		return "fleaMarket/update";
	}

	/********************************
	 * 중고악기 사진 업로드
	 * @return String 사진 저장 경로 
	 ********************************/
	@RequestMapping(value="/uploadImageFile", method=RequestMethod.POST, produces = "application/json; charset=utf8")
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
	 * @return 특정 회원의 중고악기 글 페이지 
	 *********************************/
	@GetMapping(value="/fleaPosts")
	public String viewPosts() {
		
		return "fleaMarket/fleaPosts";
	}
	
}
