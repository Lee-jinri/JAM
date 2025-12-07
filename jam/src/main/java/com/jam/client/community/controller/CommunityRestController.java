package com.jam.client.community.controller;


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

import com.jam.client.community.service.CommunityService;
import com.jam.client.community.vo.CommunityVO;
import com.jam.common.vo.PageDTO;
import com.jam.global.exception.BadRequestException;
import com.jam.global.exception.NotFoundException;
import com.jam.global.exception.UnauthorizedException;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;

@RestController
@RequestMapping("/api/community")
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
	@GetMapping(value = "board")
	public ResponseEntity<Map<String, Object>> getBoards(
			HttpServletRequest request,
			@RequestParam (defaultValue = "1")int pageNum,
			@RequestParam(required=false) String keyword){
			
		CommunityVO community = new CommunityVO();
		community.setPageNum(pageNum);
		
		if (keyword != null && !keyword.trim().isEmpty()) {
		    community.setKeyword(keyword);
		}
		
		String user_id = (String)request.getAttribute("userId");
		if(user_id != null) community.setUser_id(user_id);
		
		Map<String, Object> result = new HashMap<>();

		List<CommunityVO> communityList = comService.getBoard(community);
		result.put("communityList", communityList);
		
		int total = comService.listCnt(community);
		PageDTO pageMaker = new PageDTO(community, total);
        result.put("pageMaker", pageMaker);

        return ResponseEntity.ok(result);
	}
	
	/**
	 * 커뮤니티 인기글 조회 API
	 * 조회수, 댓글수를 기준으로 상위 인기 게시글(15개)을 반환합니다.
	 *
	 * @param request  사용자 인증 정보(userId) 추출용 HttpServletRequest
	 * @return popularList: 인기 게시글 목록
	 */
	@GetMapping(value = "/board/popular")
	public ResponseEntity<Map<String, Object>> getPopularBoard() {

		List<CommunityVO> popularList = comService.getPopularBoard();

		Map<String, Object> result = new HashMap<>();
		result.put("popularList", popularList);

		return ResponseEntity.ok(result);
	}
	
	/********************************
	 * 커뮤니티 글을 조회하는 메서드입니다.
	 *
	 * @param com_no 조회할 커뮤니티 글의 번호
	 * @return ResponseEntity<CommunityVO> - 조회된 커뮤니티 글의 정보와 HTTP 상태 코드를 포함한 응답 VO
	 * @throws Exception 데이터 조회 중 발생한 예외
	 *************************************/
	@GetMapping(value = "/post/{post_id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, Object>> getBoardDetail(
			@PathVariable("post_id") Long post_id, 
			HttpServletRequest request) {
		
		
		// 상세 페이지 조회
		CommunityVO detail = comService.getPost(post_id);
		
		if (detail == null) {
		    throw new NotFoundException("존재하지 않는 게시글입니다.");
		}

        // 조회수 증가
		comService.incrementReadCnt(post_id);
	
		Map<String, Object> response = new HashMap<>();
		response.put("detail", detail);
		
		String userId = (String)request.getAttribute("userId");
		
		boolean isAuthor = false;
		
        if (userId != null && userId.equals(detail.getUser_id())) {
            isAuthor = true;
        }
        
        response.put("isAuthor", isAuthor);
		
        return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping(value = "/posts/{post_id}/edit-data", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, Object>> getEditData(
			@PathVariable("post_id") Long post_id, 
			HttpServletRequest request) {
		
		Map<String, Object> data = comService.getPostForEdit(post_id);
		
		if (data.get("post") == null) {
		    throw new NotFoundException("존재하지 않는 게시글입니다.");
		}
		
        return new ResponseEntity<>(data, HttpStatus.OK);
	}
	
	// fleaMarket, job, roomRental도 오류 메시지 이렇게 바꾸삼 ㅠ ....
	// 그리고 이건 사용자 아이디, 닉네임도 추가한거임!!
	/******************************
	 * 커뮤니티 글을 작성하는 메서드 입니다.
	 * 
	 * @param request  사용자 인증 정보(userId) 추출용 HttpServletRequest
	 * @param CommunityVO com_vo 제목과 내용 
	 * @return HTTP 상태 코드
	 * 			성공 시 HttpStatus.OK와 작성된 글 번호를 반환하고 실패 시 HttpStatus.INTERNAL_SERVER_ERROR를 반환합니다.
	 *****************************/
	@PostMapping("/post")
	public ResponseEntity<String> writeBoard(@RequestBody CommunityVO community, HttpServletRequest request) throws Exception{
		
		if (community == null) {
	        log.error("Request body (community) is missing.");
	        throw new BadRequestException("요청 데이터가 올바르지 않습니다.");
	    }

	    if (community.getTitle() == null || community.getTitle().trim().isEmpty()) {
	        log.error("Title cannot be null or empty.");
	        throw new BadRequestException("제목을 입력하세요.");
	    }
	    if (community.getContent() == null || community.getContent().trim().isEmpty()) {
	        log.error("Content cannot be null or empty.");
	        throw new BadRequestException("내용을 입력하세요.");
	    }
	    
	    String userId = (String)request.getAttribute("userId");
		
		if(userId == null) {
			log.error("Community writeBoard User is not Authenticated.");
	        throw new UnauthorizedException("로그인이 필요한 서비스입니다.");
		}
		community.setUser_id(userId);
		
		String post_id = comService.writePost(community).toString();
		
		return new ResponseEntity<>(post_id, HttpStatus.OK);
	}
	
	/***********************************
	 * 커뮤니티 글을 수정하는 메서드 입니다.
	 * 
	 * @param CommunityVO community 수정할 글 번호, 제목과 내용
	 * @param request  사용자 인증 정보(userId) 추출용 HttpServletRequest
	 * @return HTTP 상태 코드
	 * 			성공 시 HttpStatus.OK와 글 번호를 반환하고 실패 시 HttpStatus.INTERNAL_SERVER_ERROR를 반환합니다.
	 ***********************************/
	@PutMapping("/post")
	public ResponseEntity<String> editBoard(@RequestBody CommunityVO community, HttpServletRequest request) throws Exception{
		
		if (community == null) { 
	        log.error("Request body (community) is missing.");
	        throw new BadRequestException("요청 데이터가 올바르지 않습니다.");
		}
		String title = community.getTitle();
		String content = community.getContent();
		
		if (title == null || title.trim().isEmpty()) {
			log.error("community editBoard title is null.");
	        throw new BadRequestException("제목을 입력하세요.");
		}
		if (content == null || content.trim().isEmpty()) {
			log.error("community editBoard content is null.");
	        throw new BadRequestException("내용을 입력하세요.");
		}
		
		String user_id = (String)request.getAttribute("userId");

		if(user_id == null || user_id.isEmpty()) {
			log.error("Community writeBoard User is not Authenticated.");
	        throw new UnauthorizedException("로그인이 필요한 서비스입니다.");
		}
		
		community.setUser_id(user_id);
		
		comService.editPost(community);
		String post_id = community.getPost_id().toString();
		
		return new ResponseEntity<>(post_id, HttpStatus.OK);
	}

	/**********************************
	 * 커뮤니티 글을 삭제하는 메서드 입니다.
	 * @param Long com_no 삭제할 글 번호
	 * @return HTTP 상태 코드
	 * 			성공 시 HttpStatus.OK를 반환하고 실패 시 HttpStatus.INTERNAL_SERVER_ERROR를 반환합니다.
	 **********************************/
	@DeleteMapping("/post/{postId}")
	public ResponseEntity<Void> postDelete(@PathVariable("postId") Long postId, HttpServletRequest request) throws Exception{
		
		String user_id = (String)request.getAttribute("userId");
		if(user_id == null || user_id.isEmpty()) {
			log.error("Community postDelete User is not Authenticated.");
	        throw new UnauthorizedException("로그인이 필요한 서비스입니다.");
		}
		
		comService.deletePost(postId, user_id);
		
		return new ResponseEntity<>(HttpStatus.OK);
	}
	
	@GetMapping("/my/posts")
	public ResponseEntity<Map<String, Object>> getMyPosts(
			@RequestParam(defaultValue = "1") int pageNum,
			@RequestParam(required=false) String keyword,
			HttpServletRequest request){
			
		CommunityVO community = new CommunityVO();
		
        String userId = (String) request.getAttribute("userId");
        if (userId == null || userId.isEmpty()) {
	        throw new UnauthorizedException("로그인이 필요한 서비스입니다.");
        }
        
        community.setUser_id(userId);
        community.setPageNum(pageNum);
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            community.setKeyword(keyword);
        }
        
        Map<String, Object> result = new HashMap<>();

        List<CommunityVO> posts = comService.getMyPosts(community);
        result.put("posts", posts);

        int total = comService.getMyPostsCnt(community);
        PageDTO pageMaker = new PageDTO(community, total);
        result.put("pageMaker", pageMaker);

        return ResponseEntity.ok(result);
	}
	
	@DeleteMapping("/posts/my")
	public ResponseEntity<Void> deletePosts(@RequestBody List<Long> postIds, HttpServletRequest request) {
	    String userId = (String)request.getAttribute("userId");
	    if (userId == null || userId.isEmpty()) {
	        throw new UnauthorizedException("로그인이 필요한 서비스입니다.");
        }
	    
	    comService.deleteMyPosts(userId, postIds);

	    return ResponseEntity.ok().build();
	}
}
