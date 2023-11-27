package com.jam.client.roomRentalReply.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jam.client.member.vo.MemberVO;
import com.jam.client.roomRentalReply.service.RoomReplyService;
import com.jam.client.roomRentalReply.vo.RoomReplyVO;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping(value="roomreplies")
@AllArgsConstructor
public class RoomReplyController {

	private RoomReplyService roomreplyService;
	
	
	/***************************
	 * @param Integer roomRental_no
	 * @param RoomReplyVO rrvo
	 * @param MemberVO member
	 * @return 합주실 댓글 리스트
	 ****************************/
	@DateTimeFormat 
	@GetMapping(value = "/all/{roomRental_no}", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<RoomReplyVO> replyList(@PathVariable("roomRental_no") Integer roomRental_no, @ModelAttribute("data") RoomReplyVO rrvo, @RequestParam(value = "user_id", required = false) String user_id, HttpServletRequest request, Model model){
		
		
		List<RoomReplyVO> reply = null;
		reply = roomreplyService.roomReplyList(roomRental_no);
		
		rrvo.setUser_id(user_id);
   		
		return reply;
	}
	
	/************************
	 * 합주실 댓글 작성
	 * @param RoomRentalReplyVO rrvo
	 * @param MemberVO member
	 * @return 댓글 작성 실행 결과
	 ***************************/
	@JsonFormat
	@PostMapping(value="/replyInsert",consumes = "application/json", produces = MediaType.TEXT_PLAIN_VALUE)
	public String replyInsert(@RequestBody RoomReplyVO rrvo,@ModelAttribute("data") MemberVO member, HttpServletRequest request, Model model) {
		
		int result = 0;
		
		result = roomreplyService.replyInsert(rrvo);
		
		return(result ==1)? "SUCCESS" : "FAILURE";
	}
	
	/*****************************
	 * 합주실 댓글 수정
	 * @param Integer roomReply_no
	 * @param RoomRentalReplyVO rrvo
	 * @return 댓글 수정 결과
	 *******************************/
	@PutMapping(value = "/{roomReply_no}", consumes = "application/json", produces = MediaType.TEXT_PLAIN_VALUE)
	public String replyUpdate(@PathVariable("roomReply_no") Integer roomReply_no, @RequestBody RoomReplyVO rrvo) {
		
		rrvo.setRoomReply_no(roomReply_no);
		int result = roomreplyService.replyUpdate(rrvo);
		return(result ==1) ? "SUCCESS" : "FAILURE";
	}
	
	/***********************
	 * 합주실 댓글 삭제
	 * @param Integer roomReply_no
	 * @return 댓글 삭제 결과
	 ***********************/
	@DeleteMapping(value = "/{roomReply_no}", produces = MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity<String> replyDelete(@PathVariable("roomReply_no")Integer roomReply_no){

		int result = roomreplyService.replyDelete(roomReply_no);
		
		return result == 1?
		new ResponseEntity<String>("SUCCESS", HttpStatus.OK) :new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);	
	}
}
