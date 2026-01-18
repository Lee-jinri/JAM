package com.jam.client.community.controller;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
import com.jam.client.member.vo.MemberVO;
import com.jam.common.vo.PageDTO;
import com.jam.global.exception.BadRequestException;
import com.jam.global.exception.NotFoundException;
import com.jam.global.exception.UnauthorizedException;
import com.jam.global.util.HtmlSanitizer;
import com.jam.global.util.ValueUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
@Slf4j
public class CommunityRestController {

	private final CommunityService comService;
	
	/**
	 * community 게시판 조회 API
	 * 요청 파라미터(pageNum, keyword)에 따라 커뮤니티 글 목록을 조회하고 페이징 정보를 함께 반환합니다.
	 * 
	 * @param user		현재 로그인한 사용자
	 * @param pageNum	요청한 페이지 번호
	 * @param keyword	검색 키워드 (없을 경우 전체 조회)
	 * @return communityList(커뮤니티 글 목록), pageMaker(페이징 정보)
	 */
	@GetMapping(value = "board")
	public ResponseEntity<Map<String, Object>> getBoards(
			HttpServletRequest request,
			@RequestParam (defaultValue = "1")int pageNum,
			@RequestParam(required=false) String keyword,
			@AuthenticationPrincipal MemberVO user){
			
		CommunityVO community = new CommunityVO();
		community.setPageNum(pageNum);
		
		keyword = (ValueUtils.sanitizeForLike(keyword));
		if (keyword != null && !keyword.trim().isEmpty()) {
		    community.setKeyword(keyword);
		}
		
		if (user != null) {
			community.setUser_id(user.getUser_id());
		}
		
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
	 * @param post_id 조회할 커뮤니티 글의 번호
     * @param user 현재 접속 중인 인증된 사용자 정보 (Spring Security)
	 * @return ResponseEntity<Map<String, Object>> - 게시글 정보, 작성자 여부를 포함한 응답
	 *************************************/
	@GetMapping(value = "/post/{post_id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, Object>> getBoardDetail(
			@PathVariable("post_id") Long post_id, 
			@AuthenticationPrincipal MemberVO user) {
		
		// 상세 페이지 조회
		CommunityVO detail = comService.getPost(post_id);
		
		if (detail == null) {
		    throw new NotFoundException("존재하지 않는 게시글입니다.");
		}

        // 조회수 증가
		comService.incrementReadCnt(post_id);
	
		Map<String, Object> response = new HashMap<>();
		response.put("detail", detail);

		boolean isAuthor = user != null && user.getUser_id() != null && user.getUser_id().equals(detail.getUser_id());
        response.put("isAuthor", isAuthor);
		
        return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@PreAuthorize("isAuthenticated()")
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
	
	/******************************
	 * 커뮤니티 글을 작성하는 메서드 입니다.
	 * 
	 * @param request  사용자 인증 정보(userId) 추출용 HttpServletRequest
	 * @param CommunityVO com_vo 제목과 내용 
	 * @return HTTP 상태 코드
	 * 			성공 시 HttpStatus.OK와 작성된 글 번호를 반환하고 실패 시 HttpStatus.INTERNAL_SERVER_ERROR를 반환합니다.
	 *****************************/
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/post")
	public ResponseEntity<String> writeBoard(@RequestBody CommunityVO community, HttpServletRequest request) throws Exception{

	    String userId = (String)request.getAttribute("userId");
		
		if(userId == null) {
			log.error("Community writeBoard User is not Authenticated.");
	        throw new UnauthorizedException("로그인이 필요한 서비스입니다.");
		}
		
		if (community == null) {
	        log.error("Request body (community) is missing.");
	        throw new BadRequestException("요청 데이터가 올바르지 않습니다.");
	    }

		String title = community.getTitle();
		String content = community.getContent();
		
	    if (title == null || title.trim().isEmpty()) {
	        log.error("Title cannot be null or empty.");
	        throw new BadRequestException("제목을 입력하세요.");
	    }
	    if (content == null || content.trim().isEmpty()) {
	        log.error("Content cannot be null or empty.");
	        throw new BadRequestException("내용을 입력하세요.");
	    }

		String t = HtmlSanitizer.sanitizeTitle(title);
		String c = HtmlSanitizer.sanitizeHtml(content);
		community.setTitle(t);
	    community.setContent(c);
	    		
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
	@PreAuthorize("isAuthenticated()")
	@PutMapping("/post")
	public ResponseEntity<String> editBoard(@RequestBody CommunityVO community, HttpServletRequest request) throws Exception{
		
		if (community == null) { 
	        log.error("Request body (community) is missing.");
	        throw new BadRequestException("요청 데이터가 올바르지 않습니다.");
		}

		String user_id = (String)request.getAttribute("userId");

		if(user_id == null || user_id.isEmpty()) {
			log.error("Community writeBoard User is not Authenticated.");
	        throw new UnauthorizedException("로그인이 필요한 서비스입니다.");
		}
		community.setUser_id(user_id);
		
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
		
		String t = HtmlSanitizer.sanitizeTitle(title);
		String c = HtmlSanitizer.sanitizeHtml(content);
		community.setTitle(t);
	    community.setContent(c);
	    
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
	@PreAuthorize("isAuthenticated()")
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

	@PreAuthorize("isAuthenticated()")
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
        
        keyword = (ValueUtils.sanitizeForLike(keyword));
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

	@PreAuthorize("isAuthenticated()")
	@DeleteMapping("/my/posts")
	public ResponseEntity<Void> deletePosts(@RequestBody List<Long> postIds, HttpServletRequest request) {
	    String userId = (String)request.getAttribute("userId");
	    if (userId == null || userId.isEmpty()) {
	        throw new UnauthorizedException("로그인이 필요한 서비스입니다.");
        }
	    
	    comService.deleteMyPosts(userId, postIds);

	    return ResponseEntity.ok().build();
	}
	
	/**
	 * 커뮤니티 북마크한 글을 조회하는 메서드 입니다.
	 * 
	 * @param user		현재 로그인한 사용자 정보
	 * @param pageNum	요청한 페이지 번호
	 * 
	 * @return HttpStatus.OK와 북마크 글 리스트를 반환
	 */
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/my/favorites")
	public ResponseEntity<Map<String, Object>> getFavorites(
			@AuthenticationPrincipal MemberVO user,
			@RequestParam(value = "pageNum", defaultValue = "1") int pageNum) {
		
	    if(user == null || user.getUser_id() == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("message", "로그인이 필요한 서비스 입니다."));
	    
	    CommunityVO community = new CommunityVO();
	    community.setUser_id(user.getUser_id());
	    community.setPageNum(pageNum);
	    
	    List<CommunityVO> favorites = new ArrayList<>();
	    
	    favorites = comService.getFavorites(community);

	    int total = comService.favoritesListCnt(community);
		PageDTO pageMaker = new PageDTO(community, total);
		
	    Map<String, Object> result = new HashMap<>();
	    result.put("favorites", favorites);
		result.put("pageMaker", pageMaker);

		return ResponseEntity.ok(result);
	}
}
