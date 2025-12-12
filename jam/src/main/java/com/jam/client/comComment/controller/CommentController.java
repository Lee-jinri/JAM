package com.jam.client.comComment.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jam.client.comComment.service.CommentService;
import com.jam.client.comComment.vo.CommentVO;
import com.jam.global.exception.UnauthorizedException;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping(value="/community")
@AllArgsConstructor
@Slf4j
public class CommentController {
	
	private CommentService commentService;
	
	/***************************
	 * 커뮤니티 댓글을 조회 API
	 * @param postId 	조회할 커뮤니티 글의 번호
	 * @param request	HttpServletRequest, userId 추출용
	 * 
	 * @return 커뮤니티 댓글 리스트 (작성자 본인 여부(author) 포함)
	 *  - 로그인 사용자가 있을 경우 DB 조회 시 비교하여 author true/false 반환합니다.
	 ****************************/
	// TODO: 페이징 추가
	@GetMapping(value = "/posts/{postId}/comments", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<CommentVO> commentList(@PathVariable("postId") Long postId, HttpServletRequest request){
		String userId = (String) request.getAttribute("userId");
		if(userId == null) userId = "";
		List<CommentVO> comment = commentService.commentList(postId, userId);
		return comment;
	}
	
	/*************************
	 * 커뮤니티 댓글을 작성 API
	 * @param postId 	댓글이 작성될 커뮤니티 글 번호
	 * @param c 		작성할 댓글 내용이 담긴 객체 (content)
	 * 
	 * @return 댓글 작성 실행 결과
	 *  - 로그인된 사용자만 작성 가능합니다.
	 **************************/
	@JsonFormat
	@PostMapping(value="/posts/{postId}/comments",consumes = "application/json", produces = MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity<Void> insertComment(@PathVariable("postId") Long postId, @RequestBody CommentVO c, HttpServletRequest request) {

		String userId= (String)request.getAttribute("userId");
		if(userId == null || userId.isEmpty()) throw new UnauthorizedException("로그인이 필요한 서비스 입니다.");
		
		c.setUser_id(userId);
		c.setPost_id(postId);
		commentService.insertComment(c);
		
		return ResponseEntity.ok().build();
	}
	
	/*******************************
	 * 커뮤니티 댓글을 수정 API
	 * @param commentId 	수정할 댓글 번호 
	 * @param c 			수정할 댓글 내용 (content)
	 * 
	 * @return 댓글 수정 결과
	 *  - 본인 댓글만 수정 가능합니다.
	 *******************************/
	@PutMapping(value = "/comments/{commentId}")
	public ResponseEntity<Void> updateComment(@PathVariable("commentId") Long commentId, @RequestBody CommentVO c, HttpServletRequest request) {
		
		String userId= (String)request.getAttribute("userId");
		if(userId == null || userId.isEmpty()) throw new UnauthorizedException("로그인이 필요한 서비스 입니다.");
		
		c.setUser_id(userId);
		c.setComment_id(commentId);
		
		commentService.updateComment(c);

		return ResponseEntity.ok().build();
	}
	
	/*******************************
	 * 커뮤니티 댓글을 삭제 API
	 * @param commentId 삭제할 댓글 번호
	 * 
	 * @return 댓글 삭제 결과
	 *  - 본인 댓글만 삭제 가능합니다.
	 ********************************/
	@DeleteMapping(value = "/comments/{commentId}", produces = MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity<Void> deleteComment(@PathVariable("commentId") Long commentId, HttpServletRequest request){
		
		String userId= (String)request.getAttribute("userId");
		if(userId == null || userId.isEmpty()) throw new UnauthorizedException("로그인이 필요한 서비스 입니다.");
		
		commentService.deleteComment(commentId, userId);		

		return ResponseEntity.ok().build();
	}
}