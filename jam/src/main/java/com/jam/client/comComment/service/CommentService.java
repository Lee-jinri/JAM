package com.jam.client.comComment.service;

import java.util.List;

import com.jam.client.comComment.vo.CommentVO;

public interface CommentService {

	// 커뮤니티 댓글 리스트
	public List<CommentVO> commentList(Long post_id, String user_id);
	
	// 커뮤니티 댓글 입력
	public int insertComment(CommentVO c);

	// 커뮤니티 댓글 수정
	public int updateComment(CommentVO c);
	
	// 커뮤니티 댓글 삭제
	public int deleteComment(Long comment_id, String user_id);
}
