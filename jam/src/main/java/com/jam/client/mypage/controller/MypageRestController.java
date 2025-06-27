package com.jam.client.mypage.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
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
import com.jam.global.jwt.JwtService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;

@RestController
@RequestMapping(value="/api/mypage")
@RequiredArgsConstructor
@Log4j
public class MypageRestController {
	
    private final MypageService mypageService;
	private final MemberService memberService;
	private final JwtService jwtService;


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
	
	/**
	 * 마이페이지 - 사용자가 작성한 게시글 목록 조회
	 * 
	 * @param user_id    조회 대상 사용자 ID
	 * @param boardType  게시판 타입 (community, fleaMarket)
	 * @param request    로그인 사용자 확인용 (토큰 기반)
	 * @param pageNum    페이지 번호 (기본값 1)
	 * @param search     검색 조건 (title, content 등)
	 * @param keyword    검색어
	 * @return 작성한 게시글 목록 + 페이지 정보 + 유저 프로필 + isMine 여부
	 */
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
	    written.setSearch(search);
	    written.setKeyword(keyword);
	    
	    switch (boardType) {
	        case "community":
	        	writtenList = mypageService.getWrittenCommunity(written);
	            break;
	        case "fleaMarket":
	        	writtenList = mypageService.getWrittenFlea(written);
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
		
		return ResponseEntity.ok(result);
	}
	
	/**
	 * 마이페이지 - 사용자 정보 조회
	 *
	 * 세션 또는 소셜 로그인 여부를 통해 사용자의 인증 상태를 확인한 후,
	 * 인증이 완료된 사용자에 한해 사용자 정보를 조회하여 반환합니다.
	 * 인증되지 않은 경우 401(UNAUTHORIZED) 상태 코드를 반환합니다.
	 *
	 * @param request 클라이언트 요청 객체 (세션 및 쿠키 접근용)
	 * @return 사용자 정보(MemberVO) 또는 HTTP 상태 코드
	 */
	@GetMapping(value = "/account", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<MemberVO> getAccount(HttpServletRequest request){
		try {
			boolean verifyStatus = isVerified(request);
			
			if(verifyStatus) {
				String userId = (String) request.getAttribute("userId");
					
				MemberVO account = mypageService.account(userId);
				
				return ResponseEntity.ok().body(account);
			}
			
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
			
		}catch(Exception e) {
			log.error("계정 정보 조회 중 예외 발생", e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	/**
	 * 마이페이지 인증 상태(verifyStatus)를 확인합니다.
	 * 
	 * 세션에 인증 플래그가 존재하고, 유효 시간(10분) 내에 있는 경우 true를 반환합니다.
	 * 인증되지 않았거나 세션이 만료되면 false를 반환하며, 세션이 없는 경우 401 응답을 반환합니다.
	 *
	 * @param request 클라이언트 요청 객체
	 * @return 인증 상태(boolean) 또는 HTTP 상태 코드
	 */
	@GetMapping(value = "/account/verify-status")
	public ResponseEntity<Boolean> getVerifyStatus(HttpServletRequest request) {
		
		HttpSession session = request.getSession(false);;
		
		if (session == null) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(false);
	    }
		
		try {
			boolean verifyStatus = isVerified(request);
			
			Long verifiedTime = (Long) session.getAttribute("verifyStatusTime");
	
		    if (Boolean.TRUE.equals(verifyStatus) && verifiedTime != null) {
		        long now = System.currentTimeMillis();
		        if (now - verifiedTime <= 10 * 60 * 1000) { // 10분
		            return ResponseEntity.ok(true);
		        } else {
		            // 만료된 경우 인증 해제
		            session.removeAttribute("verifyStatus");
		            session.removeAttribute("verifyStatusTime");
		        }
		    }
		    
			return ResponseEntity.ok(verifyStatus); 
		
		}catch(IllegalStateException  e) {
			// 세션 무효화 된 경우
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(false);
		}
	}
	
	/**
	 * 비밀번호 인증 성공 시 호출되는 메서드
	 * 사용자의 세션에 verifyStatus 값을 true로 설정합니다.
	 * 프론트엔드에서 비밀번호 확인 이후 이 엔드포인트를 호출하여
	 * 계정 정보 접근 권한을 부여합니다.
	 */
	@GetMapping("/verify-status/set")
	public ResponseEntity<String> setVerifyStatusSession(HttpSession session) {
		session.setAttribute("verifyStatus", true);
		session.setAttribute("verifyStatusTime", System.currentTimeMillis());
		
		return ResponseEntity.ok("session set");
	}

	/**
	 * 세션 또는 소셜 로그인 여부를 통해 사용자의 인증 상태를 확인합니다.
	 * 
	 * 세션에 verifyStatus 플래그가 true이면 인증된 것으로 간주하고
	 * 소셜 로그인 사용자(naver 또는 kakao)는 별도 확인 없이 인증 처리합니다.
	 *
	 * @param request 클라이언트 요청 객체
	 * @return 인증 여부 (true: 인증됨, false: 인증되지 않음)
	 */
	private boolean isVerified(HttpServletRequest request) {
		
		HttpSession session = request.getSession(false);
		if (session == null) return false;
		
		Boolean verifyStatus = (Boolean) session.getAttribute("verifyStatus");

		if (Boolean.TRUE.equals(verifyStatus)) return true;

		String loginType = jwtService.extractLoginType(request, request.getCookies());
		
		if ("naver".equals(loginType) || "kakao".equals(loginType)) {
			session.setAttribute("verifyStatus", true);
			session.setAttribute("verifyStatusTime", System.currentTimeMillis());
			
			return true;
		}

		return false;
	}
}
