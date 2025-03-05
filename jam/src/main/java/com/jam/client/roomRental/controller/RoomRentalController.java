package com.jam.client.roomRental.controller;


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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.jam.client.roomRental.vo.RoomRentalVO;
import com.google.gson.JsonObject;
import com.jam.client.community.vo.CommunityVO;
import com.jam.client.roomRental.service.RoomRentalService;
import com.jam.common.vo.PageDTO;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j;

@Controller
@RequestMapping("/roomRental")
@AllArgsConstructor
@Log4j
public class RoomRentalController {
	
	@Autowired
	private RoomRentalService roomService;
	
	/******************************
	 * @param RoomRentalVO room_vo
	 * @return 합주실/연습실 글 리스트 페이지
	 ******************************/
	@RequestMapping(value="/boards", method=RequestMethod.GET)
	public String roomList(Model model, @ModelAttribute RoomRentalVO room_vo, @RequestParam(required=false) String cd) {
		
		List<RoomRentalVO> roomList = roomService.getBoards(room_vo);
		model.addAttribute("roomList",roomList);
		model.addAttribute("cd",cd);
		
		// 페이징 처리
		int total = roomService.listCnt(room_vo);
		model.addAttribute("pageMaker", new PageDTO(room_vo, total));
		
		return "roomRental/boards";
	}
	
	/****************************************
	 * 합주실 글의 상세 페이지를 반환하는 메서드 입니다.
	 * @param roomRental_no 조회할 합주실 글 번호
	 * @return 합주실/연습실 상세페이지
	 *****************************************/
	@RequestMapping(value="/board/{roomRental_no}", method=RequestMethod.GET)
	public String roomDetail(@PathVariable("roomRental_no") Long roomRental_no, Model model) throws Exception{
		
		model.addAttribute("roomRental_no",roomRental_no);
		
		return "roomRental/board";
	}
	
	/***************************************
	 * @return 합주실/연습실 글 작성 페이지
	 ***************************************/
	@RequestMapping(value="/board/write", method=RequestMethod.GET)
	public String roomWriteForm() throws Exception{

		return "roomRental/write";
	}
	
	/********************************
	 * 합주실 글의 수정 페이지를 반환하는 메서드 입니다.
	 * @param roomRental_no 수정할 합주실 글 번호
	 * @return 합주실 글 수정 페이지
	 *********************************/
	@RequestMapping(value="/board/edit/{roomRental_no}", method=RequestMethod.GET)
	public String updateView(@PathVariable("roomRental_no") Long roomRental_no, Model model) throws Exception{

		model.addAttribute("roomRental_no", roomRental_no);
		return "roomRental/update";
	}
	
	/********************************
	 * 합주실/연습실 사진 업로드
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
	 * @return 특정 회원의 합주실/연습실 글 페이지 
	 *********************************/
	@RequestMapping(value="/roomPosts")
	public String viewPosts(@ModelAttribute RoomRentalVO room_vo, Model model, HttpServletRequest request) throws Exception {

		if(room_vo.getUser_name() == null || roomService.isValidUserName(room_vo.getUser_name()))
			room_vo.setUser_id((String) request.getAttribute("userId"));
		else 
			room_vo.setUser_id(roomService.getUserId(room_vo.getUser_name()));
		
		List<RoomRentalVO> roomPosts = roomService.getRoomPosts(room_vo);
		
		model.addAttribute("roomPosts", roomPosts);
		
		int total = roomService.getUserPostCnt(room_vo);
		
		PageDTO pageMaker = new PageDTO(room_vo, total);
		
		log.info("pageMaker : " + pageMaker);
		model.addAttribute("pageMaker", pageMaker);
		
		return "roomRental/roomPosts";
	}
	
}
