package com.jam.client.community.controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import com.jam.client.community.service.CommunityService;
import com.jam.client.community.vo.CommunityVO;
import com.jam.client.member.service.MemberService;
import com.jam.common.vo.PageDTO;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j;

@Controller
@RequestMapping("/community")
@AllArgsConstructor
@Log4j
public class CommunityController {
	
	/*************************
	 * 
	 */
	@RequestMapping(value = "/boards")
	public String communityBoards() {
		return "community/boards";
	}
	
	/**************************************************
	 * 커뮤니티 글의 상세 페이지를 반환하는 메서드 입니다.
	 * @param com_no 조회할 커뮤니티의 글 번호
	 * @return 커뮤니티 상세 페이지
	 **************************************************/
	@RequestMapping(value = "/board/{com_no}", method = RequestMethod.GET)
	public String communityDetail(@PathVariable("com_no") Long com_no, Model model) {
		model.addAttribute("com_no", com_no);
		
	    return "community/board";
	}
	
	
	/***************************************
	 * @return 커뮤니티 글 작성 페이지
	 ***************************************/
	@RequestMapping(value="/board/write", method=RequestMethod.GET)
	public String writeView() throws Exception{
				
		return "community/write";
	}
	
	
	
	/********************************
	 *  커뮤니티 글의 수정 페이지를 반환하는 메서드 입니다.
	 * @param com_no 수정할 커뮤니티의 글 번호
	 * @return 커뮤니티 글 수정 페이지
	 *********************************/
	@RequestMapping(value="/board/edit/{com_no}", method=RequestMethod.GET)
	public String updateView(@PathVariable("com_no") Long com_no, Model model) throws Exception{

		model.addAttribute("com_no", com_no);
		return "community/update";
	}
	
	
	/********************************
	 * 커뮤니티 사진 업로드
	 * @return String 사진 저장 경로
	 ********************************/
	@RequestMapping(value="/board/uploadImageFile", produces = "application/json; charset=utf8")
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
	
	/*********************************
	 * @return 특정 회원의 커뮤니티 글 페이지 
	 * @throws Exception 
	 *********************************/
	@GetMapping(value="/comPosts")
	public String viewPosts() throws Exception {
		
		return "community/comPosts";
	}
	
	
	
	
}