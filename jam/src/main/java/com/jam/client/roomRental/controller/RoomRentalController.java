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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.jam.client.roomRental.vo.RoomRentalVO;
import com.google.gson.JsonObject;
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
	@RequestMapping(value="/roomRentalList", method=RequestMethod.GET)
	public String roomList(Model model, @ModelAttribute RoomRentalVO room_vo) {
		
		List<RoomRentalVO> roomList = roomService.roomList(room_vo);
		model.addAttribute("roomList",roomList);
		
		// 페이징 처리
		int total = roomService.roomListCnt(room_vo);
		model.addAttribute("pageMaker", new PageDTO(room_vo, total));
		return "roomRental/roomRentalList";
	}
	
	/****************************************
	 * @return 합주실/연습실 상세페이지
	 *****************************************/
	@RequestMapping(value="/roomRentalDetail/{roomRental_no}", method=RequestMethod.GET)
	public String roomDetail(@ModelAttribute("data")RoomRentalVO room_vo, Model model) throws Exception{
		
		// 합주실/연습실 조회수 증가
		roomService.roomReadCnt(room_vo);
		
		RoomRentalVO detail = roomService.boardDetail(room_vo);
		
		model.addAttribute("detail",detail);
		
		return "roomRental/roomRentalDetail";
	}
	
	/***************************************
	 * @param RoomRentalVO room_vo
	 * @return 합주실/연습실 글 작성 페이지
	 ***************************************/
	@RequestMapping(value="/roomRentalWrite", method=RequestMethod.GET)
	public String roomWriteForm() throws Exception{

		return "roomRental/roomRentalWrite";
	}
	
	/******************************
	 * 합주실/연습실 글 작성
	 * @param MemberVO member
	 * @param RoomRentalVO room_vo
	 * @return 성공 시 작성한 합주실 글 상세 페이지 / 실패 시 합주실 글 작성 페이지
	 *****************************/
	@RequestMapping(value="/roomRentalInsert", method=RequestMethod.POST)
	public ModelAndView roomWrite(RedirectAttributes rttr, @ModelAttribute("data") RoomRentalVO room_vo, Model model) throws Exception{
		
		ModelAndView mav = new ModelAndView();
		
		try {
			roomService.roomInsert(room_vo);
			
			mav.setViewName("redirect:/roomRental/roomRentalDetail/"+room_vo.getRoomRental_no());
			return mav;
			
		}catch(Exception e) {
			log.error("roomRentalInsert 데이터 저장 중 오류 : " + e.getMessage());
			
			rttr.addFlashAttribute("result", "error");
			mav.setViewName("redirect:/roomRental/roomRentalWrite");
			return mav;
		}
	}
	
	/********************************
	 * @param MemberVO member
	 * @param RoomRentalVO room_vo
	 * @return 합주실/연습실 글 수정 페이지
	 *********************************/
	@RequestMapping(value="/roomRentalUpdateForm", method=RequestMethod.GET)
	public ModelAndView roomUpdateForm(RoomRentalVO room_vo, Model model) throws Exception{
	
		ModelAndView mav = new ModelAndView();
		
		RoomRentalVO updateData = roomService.roomUpdateForm(room_vo);
		
		model.addAttribute("updateData", updateData);
		
		mav.setViewName("roomRental/roomRentalUpdate");
		return mav;
	}
	
	/***********************************
	 * 합주실/연습실 글 수정
	 * @param MemberVO member
	 * @param RoomRentalVO room_vo
	 * @return 성공 시 수정한 합주실 글 상세페이지 / 실패 시 합주실 글 수정 페이지
	 ***********************************/
	@RequestMapping(value="/roomRentalUpdate", method=RequestMethod.POST)
	public ModelAndView roomUpdate(RedirectAttributes rttr, RoomRentalVO room_vo, Model model) throws Exception{
		
		ModelAndView mav = new ModelAndView();
		
		try {
			roomService.roomUpdate(room_vo);
			
			mav.setViewName("redirect:/roomRental/roomRentalDetail/"+room_vo.getRoomRental_no());
			return mav;
		}catch(Exception e) {
			log.error("roomRentalUpdate 데이터 수정 중 오류 : " + e.getMessage());
			
			rttr.addFlashAttribute("result", "error");
			mav.setViewName("redirect:/roomRental/roomRentalUpdate");
			return mav;
		}
	}
	
	/**********************************
	 * 합주실/연습실 글 삭제
	 * @param MemberVO member
	 * @param RoomRentalVO room_vo
	 * @return 성공 시 합주실 글 목록 / 실패 시 합주실 글 상세페이지
	 **********************************/
	@RequestMapping(value="/roomRentalDelete", method=RequestMethod.POST)
	public ModelAndView roomDelete(RedirectAttributes rttr, @ModelAttribute("data") RoomRentalVO room_vo, Model model) throws Exception{
		
		ModelAndView mav = new ModelAndView();
		
		try {
			roomService.roomDelete(room_vo);
			mav.setViewName("redirect:/roomRental/roomRentalList");
			
			return mav;
		}catch(Exception e){
			log.error("roomRentalDelete 데이터 삭제 중 오류 : " + e.getMessage());
			
			rttr.addFlashAttribute("result", "error");
			mav.setViewName("redirect:/roomRental/roomRentalDetail/"+room_vo.getRoomRental_no());
			return mav;
		}
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
	
}
