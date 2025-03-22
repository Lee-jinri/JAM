package com.jam.client.member.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jam.client.member.service.MemberBoardService;
import com.jam.client.member.vo.MemberBoardVO;
import com.jam.common.vo.PageDTO;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j;

@RestController
@RequestMapping(value="/api")
@AllArgsConstructor
@Log4j
public class MemberBoardController {
	
	@Autowired
    private MemberBoardService memberBoardService;

	@GetMapping("/favorite/boards")
	public ResponseEntity<Map<String, Object>> getFavoriteByBoardType(
			@RequestParam("boardType") String boardType, 
			HttpServletRequest request,
			@RequestParam(value = "pageNum", defaultValue = "1") int pageNum) {
		
	    String userId = (String) request.getAttribute("userId");

	    if(userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("message", "로그인이 필요한 서비스 입니다."));
	    
	    List<MemberBoardVO> favoriteList = new ArrayList<>();
	    
	    MemberBoardVO favorite = new MemberBoardVO();
	    
	    log.info(pageNum);
	    
	    favorite.setUser_id(userId);
	    favorite.setPageNum(pageNum);
	    
	    switch (boardType) {
	        case "community":
	            favoriteList = memberBoardService.getFavoriteCommunity(favorite);
	            break;
	        case "job":
	            favoriteList = memberBoardService.getFavoriteJob(favorite);
	            break;
	        case "fleaMarket":
	            favoriteList = memberBoardService.getFavoriteFlea(favorite);
	            break;
	        case "roomRental":
	            favoriteList = memberBoardService.getFavoriteRoom(favorite);
	            break;
	        default:
	            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Invalid board type"));
	    }
	    
	    
	    Map<String, Object> result = new HashMap<>();
	    
	    result.put("favoriteList", favoriteList);
	    
	    int total = memberBoardService.listCnt(boardType, userId);
	    
		PageDTO pageMaker = new PageDTO(favorite, total);
		
		result.put("pageMaker", pageMaker);

		log.info(result);
		
		return ResponseEntity.ok(result);
	}

	@PostMapping(value = "/favorite/{boardNo}", produces = "application/json")
    public ResponseEntity<String> addFavorite(@PathVariable Long boardNo, @RequestParam String boardType, HttpServletRequest request) {
        
		String user_id = (String)request.getAttribute("userId");
		
		if(user_id == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body("로그인이 필요한 서비스 입니다. 로그인 하시겠습니까?");
		}
		
		try {
			boolean added = memberBoardService.addFavorite(user_id, boardType, boardNo);
	        
			if (!added) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                    .body("즐겨찾기 실패: 해당 게시글이 존재하지 않음");
	        }
			
			return ResponseEntity.ok("즐겨찾기 추가 완료");
			
		}catch(Exception e) {
			log.error("즐겨찾기 중 오류 발생: {}"+ e.getMessage());
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body("서버 오류로 인해 즐겨찾기에 실패했습니다.");
		}
    }
	
	@DeleteMapping(value = "/favorite/{boardNo}", produces = "application/json")
	public ResponseEntity<String> deleteFavorite(@PathVariable Long boardNo, @RequestParam String boardType, HttpServletRequest request){
		String user_id = (String)request.getAttribute("userId");
		
		if(user_id == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body("로그인 되지 않음.");
		}
		
		try {
			boolean deleted = memberBoardService.deleteFavorite(user_id, boardNo, boardType);
			
			if (!deleted) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                    .body("즐겨찾기 취소 실패: 해당 게시글이 존재하지 않음");
	        }
			
			return ResponseEntity.ok("즐겨찾기 완료");
		}catch(Exception e) {
			log.error("즐겨찾기 삭제 중 오류 발생: {}"+ e.getMessage());
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body("서버 오류로 인해 즐겨찾기 취소에 실패했습니다.");
		}
	}
	

	@GetMapping("/written/boards")
	public ResponseEntity<Map<String, Object>> getWrittenBoardType(
			@RequestParam("boardType") String boardType, 
			HttpServletRequest request,
			@RequestParam(value = "pageNum", defaultValue = "1") int pageNum) {
		
	    String userId = (String) request.getAttribute("userId");

	    if(userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("message", "로그인이 필요한 서비스 입니다."));
	    
	    List<MemberBoardVO> writtenList = new ArrayList<>();
	    
	    MemberBoardVO written = new MemberBoardVO();
	    
	    written.setUser_id(userId);
	    written.setPageNum(pageNum);
	    written.setBoard_type(boardType);
	    
	    switch (boardType) {
	        case "community":
	        	writtenList = memberBoardService.getWrittenCommunity(written);
	            break;
	        case "job":
	        	writtenList = memberBoardService.getWrittenJob(written);
	            break;
	        case "fleaMarket":
	        	writtenList = memberBoardService.getWrittenFlea(written);
	            break;
	        case "roomRental":
	        	writtenList = memberBoardService.getWrittenRoom(written);
	            break;
	        default:
	            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Invalid board type"));
	    }
	    
	    
	    Map<String, Object> result = new HashMap<>();
	    
	    result.put("writtenList", writtenList);
	    
	    int total = memberBoardService.writtenListCnt(written);
	    
		PageDTO pageMaker = new PageDTO(written, total);
		
		result.put("pageMaker", pageMaker);
		
		log.info(writtenList);

		return ResponseEntity.ok(result);
	}
}
