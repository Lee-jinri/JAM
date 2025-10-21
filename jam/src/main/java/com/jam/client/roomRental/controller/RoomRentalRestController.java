package com.jam.client.roomRental.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jam.client.roomRental.service.RoomRentalService;
import com.jam.client.roomRental.vo.RoomRentalVO;
import com.jam.common.vo.PageDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;

@RestController
@RequestMapping("/api/roomRental")
@RequiredArgsConstructor
@Log4j
public class RoomRentalRestController {
	
	private final RoomRentalService roomService;
	
	@GetMapping(value = "boards")
	public ResponseEntity<Map<String, Object>> getBoards(
			RoomRentalVO room_vo, HttpServletRequest request){
		
		try {
			Map<String, Object> result = new HashMap<>();

			String user_id = (String)request.getAttribute("userId");
			if(user_id != null) room_vo.setUser_id(user_id);
			
			List<RoomRentalVO> roomList = roomService.getBoards(room_vo);
			result.put("roomList", roomList);
			
			int total = roomService.listCnt(room_vo);
			PageDTO pageMaker = new PageDTO(room_vo, total);
	        result.put("pageMaker", pageMaker);

	        return ResponseEntity.ok(result);
		}catch(Exception e) {
			log.error(e.getStackTrace());
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(Collections.singletonMap("error", "An unexpected error occurred"));
		}
		
	}
	
	
	/***************************************
	 * 합주실 글 상세정보를 조회하는 메서드입니다.
	 *
	 * @param room_no 조회할 합주실 글의 번호
	 * @return ResponseEntity<RoomRentalVO> - 조회된 합주실 글의 정보와 HTTP 상태 코드를 포함한 응답 VO
	 * @throws Exception 데이터 조회 중 발생한 예외
	 ***************************************/
	@GetMapping(value = "/board/{roomRental_no}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<RoomRentalVO> getBoardDetail(@PathVariable("roomRental_no") Long room_no) throws Exception{
		if (room_no == null) { 
			log.error("room_no is required");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}
		
		try {
	        // 조회수 증가
			roomService.incrementReadCnt(room_no);
			
			// 상세 페이지 조회
			RoomRentalVO detail = roomService.getBoardDetail(room_no);
	       
			log.info(detail.getRoomRental_status());
			
	        return new ResponseEntity<>(detail, HttpStatus.OK);
	    } catch (Exception e) {
	        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}

	/******************************
	 * 합주실/연습실 글을 작성하는 메서드 입니다.
	 * @param RoomRentalVO room_vo 제목과 내용, 가격, 작성자 아이디와 닉네임
	 * @return HTTP 상태 코드
	 * 			성공 시 HttpStatus.OK를 반환하고 실패 시 HttpStatus.INTERNAL_SERVER_ERROR를 반환합니다.
	 *****************************/
	@PostMapping("/board")
	public ResponseEntity<String> writeBoard(@RequestBody RoomRentalVO room_vo) throws Exception{
		
		String errorMsg;
		
		if(room_vo == null) {
			log.error("room_vo is null.");
			errorMsg = "room_vo is null.";
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMsg);
		}
		
		// 유효성 검사
		String room_title = room_vo.getRoomRental_title();
		String room_content = room_vo.getRoomRental_content();
		
		if(room_title == null) {
			log.error("room_title is null.");
			errorMsg = "room_title is null.";
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMsg);
		}
		if(room_content == null) {
			log.error("room_content is null.");
			errorMsg = "room_content is null.";
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMsg);
		}
		
		try {
			roomService.writeBoard(room_vo);
			
			String room_no = room_vo.getRoomRental_no().toString();
			
			return new ResponseEntity<>(room_no,HttpStatus.OK);
			
		} catch(Exception e) {
			log.error("합주실/연습실 글 작성 데이터 저장 중 오류 : " + e.getMessage());
			
			String responseBody = e.getMessage();
			return new ResponseEntity<>(responseBody, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	/********************************
	 * 합주실의 수정할 글 정보(제목, 내용, 거래 완료 여부, 가격)를 불러오는 메서드 입니다.
	 * @param RoomRentalVO room_vo 
	 * @return ResponseEntity<RoomRentalVO> - 조회된 글의 정보와 HTTP 상태 코드를 포함한 응답 VO
	 *********************************/
	@PutMapping("/board/edit/{roomRental_no}")
	public ResponseEntity<RoomRentalVO> getBoardById(@PathVariable("roomRental_no") Long roomRental_no) throws Exception{
	
		if(roomRental_no == null) {
			log.error("room_no is required.");
			
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}else {
			try {
				RoomRentalVO board = roomService.getBoardById(roomRental_no);
				board.setRoomRental_no(roomRental_no);
				
				return ResponseEntity.ok(board);
			}catch(Exception e) {
				log.error("합주실/연습실 수정 글 불러오는 중 오류 : " + e.getMessage());
				
				return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
	}
	
	/***********************************
	 * 합주실/연습실 글을 수정하는 메서드입니다.
	 * @param RoomRentalVO room_vo 제목, 내용, 거래 완료 여부, 가격
	 * @return HTTP 상태 코드
	 * 			성공 시 HttpStatus.OK를 반환하고 실패 시 HttpStatus.INTERNAL_SERVER_ERROR를 반환합니다.
	 ***********************************/
	@PutMapping("/board")
	public ResponseEntity<String> roomUpdate(@RequestBody RoomRentalVO room_vo) throws Exception{
		String errorMsg;
		if(room_vo == null) {
			log.error("room_vo is null.");
			errorMsg = "room_vo is null.";
			
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMsg);
		}
		
		// 유효성 검사
		String room_title = room_vo.getRoomRental_title();
		String room_content = room_vo.getRoomRental_content();
		
		if(room_title == null) {
			log.error("room_title is null.");
			errorMsg = "room_title is null.";
			
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMsg);
		}
		if(room_content == null) {
			log.error("room_content is null.");
			errorMsg = "room_content is null.";
			
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMsg);
		}
		
		try {
			roomService.editBoard(room_vo);
			String room_no = room_vo.getRoomRental_no().toString();
			
			return new ResponseEntity<>(room_no, HttpStatus.OK);
		}catch(Exception e) {
			log.error("합주실/연습실 editBoard 데이터 수정 중 오류 : " + e.getMessage());
			String responseBody = e.getMessage();
			
			return new ResponseEntity<>(responseBody, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
	}
	
	// 본인 인지 확인 필수!!!!
	/**********************************
	 * 합주실/연습실 글을 삭제하는 메서드 입니다.
	 * @param Long roomRental_no 삭제할 글 번호
	 * @return HTTP 상태 코드
	 * 			성공 시 HttpStatus.OK를 반환하고 실패 시 HttpStatus.INTERNAL_SERVER_ERROR를 반환합니다.
	 **********************************/
	@DeleteMapping("/board")
	public ResponseEntity<String> boardDelete(@RequestParam("roomRental_no") Long roomRental_no) throws Exception{
		
		if(roomRental_no == null) {
			log.error("room_no is null.");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("room_no is null.");
		}
		
		try {
			roomService.boardDelete(roomRental_no);
			
			return new ResponseEntity<>(HttpStatus.OK);
		}catch(Exception e) {
			log.error("합주실/연습실 delete 데이터 삭제 중 오류 : " + e.getMessage());
			
			String responseBody = "합주실/연습실 delete 데이터 삭제 중 오류 : " + e.getMessage();
			
			return new ResponseEntity<>(responseBody, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	
}
