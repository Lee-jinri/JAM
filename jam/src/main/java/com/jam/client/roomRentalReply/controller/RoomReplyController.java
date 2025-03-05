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
import lombok.extern.log4j.Log4j;

@RestController
@RequestMapping(value="roomreplies")
@AllArgsConstructor
@Log4j
public class RoomReplyController {

	private RoomReplyService roomreplyService;
	
	
	/***************************
	 * 합주실 댓글을 조회하는 메서드입니다.
	 * @param Long roomRental_no 조회할 합주실 글 번호
	 * @param RoomReplyVO rrvo
	 * @return 합주실 댓글 리스트
	 ****************************/
	@DateTimeFormat 
	@GetMapping(value = "/all/{roomRental_no}", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<RoomReplyVO> replyList(@PathVariable("roomRental_no") Long roomRental_no, @ModelAttribute("data") RoomReplyVO rrvo, @RequestParam(value = "user_id", required = false) String user_id, HttpServletRequest request, Model model){
		
		
		List<RoomReplyVO> reply = null;
		reply = roomreplyService.roomReplyList(roomRental_no);
		
		rrvo.setUser_id(user_id);
   		
		return reply;
	}
	
	/************************
	 * 합주실 댓글을 작성하는 메서드입니다.
	 * @param RoomRentalReplyVO rrvo
	 * @return 댓글 작성 실행 결과
	 ***************************/
	@JsonFormat
	@PostMapping(value="/reply",consumes = "application/json", produces = MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity<String> replyInsert(@RequestBody RoomReplyVO rrvo, HttpServletRequest request, Model model) {
		
		String body = "";
		
		if(rrvo == null) body = "RoomRentalReplyVO is required.";
		else if(rrvo.getRoomReply_content() == null) body = "RoomRental reply contents is required";
		else if(rrvo.getUser_id() == null) body = "user id is required.";
		else if(rrvo.getUser_name() == null) body = "user name is required.";
		else if(rrvo.getRoomRental_no() == null) body = "RoomRental no is required.";
		else {
			try {
				roomreplyService.replyInsert(rrvo);
				
				return new ResponseEntity<>(HttpStatus.OK);
			}catch (Exception e) {
				log.error("합주실/연습실 댓글 입력 중 오류: " + e.getMessage());
				return ResponseEntity.internalServerError().body(e.getMessage());
			}
		}
		
		return ResponseEntity.badRequest().body(body);
		
	}
	
	/*****************************
	 * 합주실 댓글을 수정하는 메서드입니다.
	 * @param Long roomReply_no 수정할 댓글 번호
	 * @param RoomRentalReplyVO rrvo 수정할 댓글 내용
	 * @return 댓글 수정 결과
	 *******************************/
	@PutMapping(value = "/{roomReply_no}", consumes = "application/json", produces = MediaType.TEXT_PLAIN_VALUE)
	public String replyUpdate(@PathVariable("roomReply_no") Long roomReply_no, @RequestBody RoomReplyVO rrvo) {
		
		rrvo.setRoomReply_no(roomReply_no);
		int result = roomreplyService.replyUpdate(rrvo);
		return(result ==1) ? "SUCCESS" : "FAILURE";
	}
	
	/***********************
	 * 합주실 댓글을 삭제하는 메서드입니다.
	 * @param Long roomReply_no 삭제할 댓글 번호
	 * @return 댓글 삭제 결과
	 ***********************/
	@DeleteMapping(value = "/{roomReply_no}", produces = MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity<String> replyDelete(@PathVariable("roomReply_no")Long roomReply_no){

		int result = roomreplyService.replyDelete(roomReply_no);
		
		return result == 1?
		new ResponseEntity<String>("SUCCESS", HttpStatus.OK) :new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);	
	}
}
