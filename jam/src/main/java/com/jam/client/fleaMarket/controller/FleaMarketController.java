package com.jam.client.fleaMarket.controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.google.gson.JsonObject;
import com.jam.client.community.vo.CommunityVO;
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
	
	@RequestMapping(value="/fleaMarketList", method=RequestMethod.GET)
	public String fleaMarketList(Model model, @ModelAttribute FleaMarketVO flea_vo) {
		
		List<FleaMarketVO> fleaMarketList = fleaService.fleaList(flea_vo);
		
		model.addAttribute("fleaMarketList",fleaMarketList);
		
		
		int total = fleaService.fleaListCnt(flea_vo);
		model.addAttribute("pageMaker", new PageDTO(flea_vo, total));
		return "fleaMarket/fleaMarketList";
	}
	
	@RequestMapping(value="/fleaMarketDetail/{flea_no}", method=RequestMethod.GET)
	public ModelAndView communityDetail(@ModelAttribute("data")FleaMarketVO flea_vo, Model model) throws Exception{
		
		fleaService.fleaReadCnt(flea_vo);
		FleaMarketVO detail = fleaService.fleaDetail(flea_vo);
		
		ModelAndView mav = new ModelAndView();
		
		mav.addObject("detail", detail);
		mav.setViewName("fleaMarket/fleaMarketDetail");
		
		return mav;
	}
	
	@RequestMapping(value="/fleaWrite", method=RequestMethod.GET)
	public ModelAndView fleaMarketWriteForm(HttpServletRequest request, Model model) {
		
		HttpSession session = request.getSession(false);
		ModelAndView mav = new ModelAndView();
		
		if (session != null) {
			MemberVO member = (MemberVO) session.getAttribute("member");
			if(member != null) {
				mav.setViewName("fleaMarket/fleaMarketWrite");
			}else {
				mav.setViewName("member/login");
			}
		}else {
			mav.setViewName("member/login");
		}
		
		return mav;
	}
	
	@RequestMapping(value="/fleaWrite", method=RequestMethod.POST)
	public ModelAndView fleaMarketWrite(HttpServletRequest request, HttpServletResponse response, MemberVO member, RedirectAttributes rttr, @ModelAttribute("data") FleaMarketVO flea_vo, Model model) {
		
		HttpSession session = request.getSession();
		MemberVO vo = (MemberVO)session.getAttribute("member");
		session.setAttribute("member", vo);
		System.out.println(vo);
		
		member.setUser_id(vo.getUser_id());
		member.setUser_name(vo.getUser_name());
		
		ModelAndView mav = new ModelAndView();
		
		int result = 0;
		result = fleaService.fleaInsert(flea_vo);
		
		if(result == 1) {
			mav.setViewName("redirect:/fleaMarket/fleaMarketDetail/"+flea_vo.getFlea_no());
			return mav;
		}else {
			rttr.addFlashAttribute("result",1);
			mav.setViewName("redirect:/fleaMarket/fleaMarketWrite");
			return mav;
		}
	}
	
	@RequestMapping(value="/fleaUpdateForm", method=RequestMethod.POST)
	public ModelAndView fleaMarketUpdateForm(HttpServletRequest request, HttpServletResponse response, MemberVO member, RedirectAttributes rttr, FleaMarketVO flea_vo, Model model) throws Exception{
		
		HttpSession session = request.getSession();
		MemberVO vo = (MemberVO)session.getAttribute("member");
		session.setAttribute("member", vo);
		System.out.println(vo);
		
		member.setUser_id(vo.getUser_id());
		member.setUser_name(vo.getUser_name());
		
		ModelAndView mav = new ModelAndView();
		
		FleaMarketVO updateData = fleaService.fleaUpdateForm(flea_vo);
		
		model.addAttribute("updateData", updateData);
		
		mav.setViewName("fleaMarket/fleaMarketUpdate");
		return mav;
	}
	
	@RequestMapping(value="/fleaUpdate", method=RequestMethod.POST)
	public ModelAndView fleaMarketUpdate(HttpServletRequest request, HttpServletResponse response, MemberVO member, RedirectAttributes rttr, @ModelAttribute("data") FleaMarketVO flea_vo, Model model) {
		
		HttpSession session = request.getSession();
		MemberVO vo = (MemberVO)session.getAttribute("member");
		session.setAttribute("member", vo);
		
		member.setUser_id(vo.getUser_id());
		member.setUser_name(vo.getUser_name());
		
		ModelAndView mav = new ModelAndView();
		
		int result = 0;
		result = fleaService.fleaUpdate(flea_vo);
		
		if(result == 1){
			mav.setViewName("redirect:/fleaMarket/fleaMarketDetail/"+flea_vo.getFlea_no());
			return mav;
		}else {
			rttr.addFlashAttribute("result",1);
			mav.setViewName("redirect:/fleaMarket/fleaMarketUpdate");
			return mav;
		}
	}
	
	@RequestMapping(value="/fleaDelete", method=RequestMethod.POST)
	public ModelAndView fleaMarketDelete(HttpServletRequest request, HttpServletResponse response, MemberVO member, RedirectAttributes rttr, @ModelAttribute("data") FleaMarketVO flea_vo, Model model) throws Exception{
		
		HttpSession session = request.getSession();
		MemberVO vo = (MemberVO)session.getAttribute("member");
		session.setAttribute("member", vo);
		System.out.println(vo);
		
		member.setUser_id(vo.getUser_id());
		member.setUser_name(vo.getUser_name());
		
		ModelAndView mav = new ModelAndView();
		
		int result = 0;
		result = fleaService.fleaDelete(flea_vo);
		
		if(result == 1) {
			mav.setViewName("redirect:/fleaMarket/fleaMarketList");
			return mav;
		}else {
			rttr.addFlashAttribute("result", 1);
			mav.setViewName("redirect:/fleaMarket/fleaMarketDetail/"+flea_vo.getFlea_no());
			return mav;
		}
	}
	

	/********************************
	 * 중고악기 사진 업로드
	 * @return String
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
		String a = jsonObject.toString();
		return a;
	}
}
