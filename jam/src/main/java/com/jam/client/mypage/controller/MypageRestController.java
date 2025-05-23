package com.jam.client.mypage.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jam.client.member.service.MemberService;
import com.jam.client.member.vo.MemberVO;
import com.jam.client.mypage.service.MypageService;
import com.jam.client.mypage.vo.MemberBoardVO;
import com.jam.common.vo.PageDTO;
import com.jam.global.jwt.JwtTokenProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;

@RestController
@RequestMapping(value="/api/mypage")
@RequiredArgsConstructor
@Log4j
public class MypageRestController {
	
    private final MypageService mypageService;
	private final MemberService memberService;
	private final JwtTokenProvider jwtTokenProvider;


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
	            favoriteList = mypageService.getFavoriteCommunity(favorite);
	            break;
	        case "job":
	            favoriteList = mypageService.getFavoriteJob(favorite);
	            break;
	        case "fleaMarket":
	            favoriteList = mypageService.getFavoriteFlea(favorite);
	            break;
	        case "roomRental":
	            favoriteList = mypageService.getFavoriteRoom(favorite);
	            break;
	        default:
	            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Invalid board type"));
	    }
	    
	    
	    Map<String, Object> result = new HashMap<>();
	    
	    result.put("favoriteList", favoriteList);
	    
	    int total = mypageService.listCnt(boardType, userId);
	    
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
			boolean added = mypageService.addFavorite(user_id, boardType, boardNo);
	        
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
			boolean deleted = mypageService.deleteFavorite(user_id, boardNo, boardType);
			
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
			@RequestParam(value = "user_id") String user_id,
			@RequestParam("boardType") String boardType, 
			HttpServletRequest request,
			@RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
			@RequestParam(value = "search", required = false) String search,
			@RequestParam(value = "keyword", required = false) String keyword) {
		
		String loginUserId = (String) request.getAttribute("userId");
		
		// 로그인 한 사용자만 이용 가능
		if(loginUserId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("message", "로그인 후 이용할 수 있는 서비스 입니다."));
				
		MemberBoardVO written = new MemberBoardVO();
		
	    List<MemberBoardVO> writtenList = new ArrayList<>();
	    
	    written.setUser_id(user_id);
	    written.setPageNum(pageNum);
	    written.setBoard_type(boardType);
	    // FIXME: 이거 뭐임?
	    //written.setSearch(search);
	    //written.setKeyword(keyword);
	    
	    log.info(search + keyword);
	    
	    switch (boardType) {
	        case "community":
	        	writtenList = mypageService.getWrittenCommunity(written);
	            break;
	        case "job":
	        	writtenList = mypageService.getWrittenJob(written);
	            break;
	        case "fleaMarket":
	        	writtenList = mypageService.getWrittenFlea(written);
	            break;
	        case "roomRental":
	        	writtenList = mypageService.getWrittenRoom(written);
	            break;
	        default:
	            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Invalid board type"));
	    }
	    
	    
	    Map<String, Object> result = new HashMap<>();
	    
	    result.put("writtenList", writtenList);
	    
	    int total = mypageService.writtenListCnt(written);
		PageDTO pageMaker = new PageDTO(written, total);
		result.put("pageMaker", pageMaker);
		
		MemberVO userProfile = memberService.getUserProfile(user_id);
		result.put("userProfile", userProfile);
		
		boolean isMine = loginUserId.equals(user_id);
		result.put("isMine", isMine);
			
		log.info(writtenList);
		
		return ResponseEntity.ok(result);
	}
	
	/**
	 * 마이페이지 - 사용자 정보 조회
	 * 
	 * @return HTTP 응답 상태코드, 사용자 아이디, 닉네임, 주소, 전화번호, 소셜로그인 여부 
	 */
	@GetMapping(value = "/account", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<MemberVO> getAccount(HttpServletRequest request, HttpServletResponse response){
		try {
			boolean isPasswordVerified = isPasswordVerified(request);
			
			MemberVO account = new MemberVO();
			
			if(isPasswordVerified) {
				if(response.getStatus() == 200) {
					String userId = (String) request.getAttribute("userId");
					
					account = mypageService.account(userId);
				}
			}
			return ResponseEntity.ok().body(account);
		}catch(Exception e) {
			log.error(e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	
	/**
	 * 비밀번호 검증 여부를 확인합니다.
	 * @return boolean 
	 */
	@GetMapping(value = "/isPasswordVerified")
	public boolean isPasswordVerified(HttpServletRequest request) {
		HttpSession session = request.getSession();
		String tempToken = (String)session.getAttribute("passwordChecked");
		
		boolean passwordChecked = false;
				
		if(tempToken != null) {
			String purpose = jwtTokenProvider.extractPurpose(tempToken);
			passwordChecked = purpose.equals("passwordChecked") ? true : false;
		}
		
		return passwordChecked;
	}
	
	
}
