package com.jam.client.community.controller;


import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.dao.DataAccessException;
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

import com.jam.client.community.service.CommunityService;
import com.jam.client.community.vo.CommunityVO;
import com.jam.common.vo.PageDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;

@RestController
@RequestMapping("/api/community/")
@RequiredArgsConstructor
@Log4j
public class CommunityRestController {

	private final CommunityService comService;
	
	/**
	 * community 게시판 조회 API
	 * 요청 파라미터(pageNum, keyword)에 따라 커뮤니티 글 목록을 조회하고 페이징 정보를 함께 반환합니다.
	 * 
	 * @param request	HttpServletRequest, userId 추출용
	 * @param pageNum	요청한 페이지 번호
	 * @param keyword	검색 키워드 (없을 경우 전체 조회)
	 * @return communityList(커뮤니티 글 목록), pageMaker(페이징 정보)
	 */
	@GetMapping(value = "boards")
	public ResponseEntity<Map<String, Object>> getBoards(
			HttpServletRequest request,
			@RequestParam (defaultValue = "1")int pageNum,
			@RequestParam(required=false) String keyword){
			
		try {
			CommunityVO community = new CommunityVO();
			community.setPageNum(pageNum);
			if(!keyword.isEmpty()) community.setKeyword(keyword);
			
			String user_id = (String)request.getAttribute("userId");
			if(user_id != null) community.setUser_id(user_id);
			
			Map<String, Object> result = new HashMap<>();

			List<CommunityVO> communityList = comService.getBoards(community);
			result.put("communityList", communityList);
			
			int total = comService.listCnt(community);
			PageDTO pageMaker = new PageDTO(community, total);
	        result.put("pageMaker", pageMaker);

	        return ResponseEntity.ok(result);
		}catch(Exception e) {
			log.error(e.getMessage());
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(Collections.singletonMap("error", "An unexpected error occurred"));
		}
		
	}
	
	/**
	 * 커뮤니티 인기글 조회 API
	 * 조회수, 댓글수를 기준으로 상위 인기 게시글(15개)을 반환합니다.
	 *
	 * @param request  사용자 인증 정보(userId) 추출용 HttpServletRequest
	 * @return popularList: 인기 게시글 목록
	 */
	@GetMapping(value = "/board/popular")
	public ResponseEntity<Map<String, Object>> getPopularBoard(HttpServletRequest request) {
		try {
			String user_id = (String) request.getAttribute("userId");

			CommunityVO community = new CommunityVO();
			if (user_id != null) community.setUser_id(user_id);

			List<CommunityVO> popularList = comService.getPopularBoard(community);

			Map<String, Object> result = new HashMap<>();
			result.put("popularList", popularList);

			return ResponseEntity.ok(result);
		} catch (Exception e) {
			log.error("Error fetching popular board: "+ e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Collections.singletonMap("error", "Failed to fetch popular boards"));
		}
	}
	
	/********************************
	 * 커뮤니티 글을 조회하는 메서드입니다.
	 *
	 * @param com_no 조회할 커뮤니티 글의 번호
	 * @return ResponseEntity<CommunityVO> - 조회된 커뮤니티 글의 정보와 HTTP 상태 코드를 포함한 응답 VO
	 * @throws Exception 데이터 조회 중 발생한 예외
	 *************************************/
	@GetMapping(value = "/board/{com_no}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, Object>> getBoardDetail(@PathVariable("com_no") Long com_no, HttpServletRequest request) throws Exception{
		
		if (com_no == null) { 
			log.error("com_no is required.");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}
		
		try {
	        // 조회수 증가
			comService.incrementReadCnt(com_no);
			
			// 상세 페이지 조회
			CommunityVO detail = comService.getBoardDetail(com_no);
	       
			Map<String, Object> response = new HashMap<>();
			response.put("detail", detail);
			
			String userId = (String)request.getAttribute("userId");
			
			boolean isAuthor = false;
			
	        if (userId != null && userId.equals(detail.getUser_id())) {
	            isAuthor = true;
	        }
	        
	        response.put("isAuthor", isAuthor);
			
	        return new ResponseEntity<>(response, HttpStatus.OK);
	    } catch (Exception e) {
	        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}
	
	// fleaMarket, job, roomRental도 오류 메시지 이렇게 바꾸삼 ㅠ ....
	// 그리고 이건 사용자 아이디, 닉네임도 추가한거임!!
	/******************************
	 * 커뮤니티 글을 작성하는 메서드 입니다.
	 * @param CommunityVO com_vo 커뮤니티 글 번호, 작성자 id와 닉네임, 제목과 내용 
	 * @return HTTP 상태 코드
	 * 			성공 시 HttpStatus.OK를 반환하고 실패 시 HttpStatus.INTERNAL_SERVER_ERROR를 반환합니다.
	 *****************************/
	@PostMapping("/board")
	public ResponseEntity<String> writeBoard(@RequestBody CommunityVO com_vo, HttpSession session) throws Exception{
		
		if (com_vo == null) {
	        log.error("Request body (com_vo) is missing.");
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Request body (com_vo) is missing.");
	    }

	    if (com_vo.getTitle() == null || com_vo.getTitle().trim().isEmpty()) {
	        log.error("Title (com_title) cannot be null or empty.");
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Title (com_title) cannot be null or empty.");
	    }
	    if (com_vo.getContent() == null || com_vo.getContent().trim().isEmpty()) {
	        log.error("Content (com_content) cannot be null or empty.");
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Content (com_content) cannot be null or empty.");
	    }
		
		try {
			String userId = (String)session.getAttribute("userId");
			String userName = (String)session.getAttribute("userName");
			
			if(userId == null || userName == null) {
				log.error("User is not Authenticated.");
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not Authenticated.");
			}
			
			com_vo.setUser_id(userId);
			com_vo.setUser_name(userName);
			
			comService.writeBoard(com_vo);
			
			String com_no = com_vo.getPost_id().toString();
			
			return new ResponseEntity<>(com_no,HttpStatus.OK);
		} catch (NullPointerException e) {
	        log.error("NullPointerException 발생: ", e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("NullPointerException occurred.");
	    } catch (DataAccessException e) { // DB 관련 예외 처리
	        log.error("Database 오류 발생: ", e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Database error occurred.");
	    } catch (Exception e) {
	        log.error("커뮤니티 글 작성 데이터 저장 중 오류: ", e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage() != null ? e.getMessage() : "Unknown error occurred.");
	    }
	}
	
	
	
	/*******************************************
	 * 커뮤니티의 수정할 글 정보(제목, 내용, 사진 파일 이름, 글쓴이 id)를 불러오는 메서드 입니다.
	 * 
	 * @param Long com_no 수정을 위해 불러올 글 번호
	 * @return ResponseEntity<CommunityVO> - 조회된 커뮤니티 글의 정보와 HTTP 상태 코드를 포함한 응답 VO
	 *******************************************/
	@GetMapping(value = "/board/edit/{com_no}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<CommunityVO> getBoardById(@PathVariable Long com_no, HttpServletRequest request) {
		if (com_no == null) { 
			log.error("com_no is required");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}
		try {
			CommunityVO board = comService.getBoardById(com_no);
			
			board.setPost_id(com_no);
			    
			return ResponseEntity.ok(board);
		}catch (Exception e) {
			log.error("커뮤니티 수정 글 불러오는 중 오류 발생 : " + e.getMessage());
				
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	/***********************************
	 * 커뮤니티 글을 수정하는 메서드 입니다.
	 * @param CommunityVO com_vo 수정할 글 번호, 제목과 내용
	 * @return HTTP 상태 코드
	 * 			성공 시 HttpStatus.OK와 글 번호를 반환하고 실패 시 HttpStatus.INTERNAL_SERVER_ERROR를 반환합니다.
	 ***********************************/
	@PutMapping("/board")
	public ResponseEntity<String> editBoard(@RequestBody CommunityVO com_vo, HttpServletRequest request) throws Exception{
		
		if (com_vo == null) { 
			log.error("com_vo is null.");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("com_vo is null.");
		}
		String com_title = com_vo.getTitle();
		String com_content = com_vo.getContent();
		
		if (com_title == null) {
			log.error("com_title is null.");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("com_title is null.");
		}
		if (com_content == null) {
			log.error("com_content is null.");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("com_content is null.");
		}
		
		String user_id = (String)request.getAttribute("userId");
		if(user_id.isEmpty() || user_id.equals("")) {
			log.error("Not Authenticated.");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not Authenticated.");
		}
		
		try {
			comService.editBoard(com_vo, user_id);
			String com_no = com_vo.getPost_id().toString();
			
			return new ResponseEntity<>(com_no, HttpStatus.OK);
		} catch(Exception e) {
			log.error("커뮤니티 editBoard 데이터 수정 중 오류 : " + e.getMessage());
			String responseBody = e.getMessage();
			
			return new ResponseEntity<>(responseBody, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
	}
	

	// 이거 수정해야됨 
	// 삭제하는 사람이 본인인지 확인하는거 db에 where user_id = #{user_id} 추가하는 걸로 바꿔ㅕㅆ음\
	// 다른 테이블도 이렇게 해야됨! 
	/**********************************
	 * 커뮤니티 글을 삭제하는 메서드 입니다.
	 * @param Long com_no 삭제할 글 번호
	 * @return HTTP 상태 코드
	 * 			성공 시 HttpStatus.OK를 반환하고 실패 시 HttpStatus.INTERNAL_SERVER_ERROR를 반환합니다.
	 **********************************/
	@DeleteMapping("/board")
	public ResponseEntity<String> boardDelete(@RequestParam("com_no") Long com_no, HttpServletRequest request) throws Exception{
		
		if (com_no == null) { 
			log.error("com_no is required");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("com_no is required");
		}
		
		String user_id = (String)request.getAttribute("userId");
		if(user_id.isEmpty() || user_id.equals("")) {
			log.error("Not Authenticated.");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not Authenticated.");
		}
		
		try {
			comService.boardDelete(com_no, user_id);
			
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (Exception e) {
			log.error("커뮤니티 delete 데이터 삭제 중 오류 : " + e.getMessage());
			
			String responseBody = "커뮤니티 delete 데이터 삭제 중 오류 : " + e.getMessage();
			
			return new ResponseEntity<>(responseBody, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GetMapping("/posts")
	public ResponseEntity<Map<String, Object>> posts(
			/*@RequestParam Map<String, String> params,*/
			@RequestParam String type,
			@RequestParam(required=false) String userId, 
			@RequestParam(defaultValue = "1") int pageNum,
			@RequestParam(required=false) String search,
			@RequestParam(required=false) String keyword,
			HttpServletRequest request) throws Exception {
			
		CommunityVO com_vo = new CommunityVO();
		
		log.info(keyword);
		
	    if (!type.equals("my") && !type.equals("other")) {
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
	                .body(Collections.singletonMap("error", "Invalid type parameter"));
	    }

	    if ("my".equals(type)) {
	        String loggedInUserId = (String) request.getAttribute("userId");
	        if (loggedInUserId == null) {
	            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	                    .body(Collections.singletonMap("error", "User not authenticated"));
	        }
	        com_vo.setUser_id(loggedInUserId);
	    }else if ("other".equals(type)) {
	        if (com_vo.getUser_id() == null) {
	            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
	                    .body(Collections.singletonMap("error", "Missing userId parameter for type=other"));
	        }
	        com_vo.setUser_id(userId);
	    }

	    try {
	        com_vo.setPageNum(pageNum);
	        
	        Map<String, Object> result = new HashMap<>();

	        List<CommunityVO> posts = comService.getPosts(com_vo);
	        result.put("posts", posts);

	        int total = comService.getUserPostCnt(com_vo);
	        PageDTO pageMaker = new PageDTO(com_vo, total);
	        result.put("pageMaker", pageMaker);

	        return ResponseEntity.ok(result);
	    } catch (Exception e) {
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(Collections.singletonMap("error", "An unexpected error occurred"));
	    }
	}
}
