package com.jam.client.community.controller;

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
import org.springframework.security.access.prepost.PreAuthorize;
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
import com.jam.client.community.service.CommunityService;
import com.jam.client.community.vo.CommunityVO;
import com.jam.client.member.vo.MemberVO;
import com.jam.common.vo.PageDTO;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j;

@Controller
@RequestMapping("/community")
@AllArgsConstructor
@Log4j
public class CommunityController {
	
	@Autowired
	private CommunityService comService;
	
	/**************************************
	 * @param CommunityVO com_vo
	 * @return 커뮤니티 글 리스트 
	 ***************************************/
	@RequestMapping(value="/communityList", method=RequestMethod.GET)
	public String communityList(Model model, @ModelAttribute CommunityVO com_vo) {
		
		List<CommunityVO> communityList = comService.communityList(com_vo);
		
		model.addAttribute("communityList",communityList);
		
		// 페이징 처리
		int total = comService.comListCnt(com_vo);
		model.addAttribute("pageMaker", new PageDTO(com_vo, total));
		return "community/communityList";
	}
	
	/****************************************
	 * @param CommunityVO com_vo
	 * @return 커뮤니티 상세페이지
	 *****************************************/
	@RequestMapping(value="/communityDetail/{com_no}", method=RequestMethod.GET)
	public String communityDetail(@ModelAttribute("data")CommunityVO com_vo, Model model) throws Exception{
		
		// 조회수 증가
		comService.comReadCnt(com_vo);
		
		CommunityVO detail = comService.boardDetail(com_vo);
		
		model.addAttribute("detail",detail);
		
		return "community/communityDetail";
	}
	
	/***************************************
	 * @return 커뮤니티 글 작성 페이지
	 ***************************************/
	@RequestMapping(value="/communityWrite", method=RequestMethod.GET)
	public String communityWriteForm() throws Exception{
				
		return "community/communityWrite";
	}
	
	/******************************
	 * 커뮤니티 글 작성
	 * @param MemberVO member
	 * @param CommunityVO com_vo
	 * @return 성공 시 작성한 커뮤니티 글 상세 페이지/ 실패 시 커뮤니티 글 작성 페이지
	 *****************************/
	@RequestMapping(value="/communityWrite", method=RequestMethod.POST)
	public ModelAndView communityWrite(RedirectAttributes rttr, @ModelAttribute("data") CommunityVO com_vo, Model model) throws Exception{
		
		ModelAndView mav = new ModelAndView();
		
		try {
			comService.comInsert(com_vo);
			
			mav.setViewName("redirect:/community/communityDetail/"+com_vo.getCom_no());

			return mav;
		} catch (Exception e) {
			log.error("communityWrite 데이터 저장 중 오류 : " + e.getMessage());
			
			rttr.addFlashAttribute("result", "error");
			mav.setViewName("redirect:/community/communityWrite");
			
			return mav;
		}
	}
	
	/********************************
	 * @param MemberVO member
	 * @param CommunityVO com_vo
	 * @return 커뮤니티 글 수정 페이지
	 *********************************/
	@RequestMapping(value="/communityUpdateForm", method=RequestMethod.GET)
	public ModelAndView communityUpdateForm(CommunityVO com_vo, Model model) throws Exception{

		ModelAndView mav = new ModelAndView();
		
		CommunityVO updateData = comService.comUpdateForm(com_vo);
		
		model.addAttribute("updateData", updateData);
		
		mav.setViewName("community/communityUpdate");
		return mav;
	}
	
	/***********************************
	 * 커뮤니티 글 수정
	 * @param MemberVO member
	 * @param CommunityVO com_vo
	 * @return 성공 시 수정한 커뮤니티 글 상세 페이지 / 실패 시 커뮤니티 글 수정 페이지
	 ***********************************/
	@RequestMapping(value="/communityUpdate", method=RequestMethod.POST)
	public ModelAndView communityUpdate(RedirectAttributes rttr, CommunityVO com_vo, Model model) throws Exception{
		
		ModelAndView mav = new ModelAndView();
		
		try {
			comService.comUpdate(com_vo);
			
			mav.setViewName("redirect:/community/communityDetail/"+com_vo.getCom_no());
			
			return mav;
		} catch(Exception e) {
			log.error("communityUpdate 데이터 수정 중 오류 : " + e.getMessage());
			rttr.addFlashAttribute("result", "error");
			mav.setViewName("redirect:/community/communityUpdate");
			return mav;
		}
		
	}
	
	/**********************************
	 * 커뮤니티 글 삭제
	 * @param MemberVO member
	 * @param CommunityVO com_vo
	 * @return 성공 시 커뮤니티 글 목록 / 실패 시 커뮤니티 글 상세 페이지
	 **********************************/
	@RequestMapping(value="/communityDelete", method=RequestMethod.POST)
	public ModelAndView communityDelete(RedirectAttributes rttr, @ModelAttribute("data") CommunityVO com_vo, Model model) throws Exception{

		ModelAndView mav = new ModelAndView();
		
		try {
			comService.comDelete(com_vo);
			
			mav.setViewName("redirect:/community/communityList");
			return mav;
		} catch (Exception e) {
			log.error("communityDelete 데이터 삭제 중 오류 : " + e.getMessage());
			rttr.addFlashAttribute("result", "error");
			mav.setViewName("redirect:/community/communityDetail/"+com_vo.getCom_no());
			return mav;
		}
		
	}
	
	/********************************
	 * 커뮤니티 사진 업로드
	 * @return String 사진 저장 경로
	 ********************************/
	@RequestMapping(value="/uploadImageFile", produces = "application/json; charset=utf8")
	@ResponseBody
	public String uploadImageFile(@RequestParam("file") MultipartFile multipartFile, HttpServletRequest request )  {
		JsonObject jsonObject = new JsonObject();
		
		// 내부경로로 저장
		// getRealPath 실제 톰캣이 돌아가고 있는 컴퓨터에서 그 곳의 절대 주소값 리턴
		String contextRoot = new HttpServletRequestWrapper(request).getRealPath("/");
		String fileRoot = contextRoot+"resources/fileupload/";
		
		String originalFileName = multipartFile.getOriginalFilename();	//오리지날 파일명
		String extension = originalFileName.substring(originalFileName.lastIndexOf("."));	//파일 확장자
		String savedFileName = UUID.randomUUID() + extension;	//저장될 파일명
		
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