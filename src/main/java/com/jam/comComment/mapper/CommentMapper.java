package com.jam.comComment.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.jam.comComment.dto.CommentDto;

public interface CommentMapper {
	// 커뮤니티 댓글 리스트
	public List<CommentDto> commentList(@Param("post_id") Long post_id, @Param("user_id") String user_id);

	//커뮤니티 댓글 입력
	public int insertComment(CommentDto c);

	// 커뮤니티 댓글 수정
	public int updateComment(CommentDto c);

	// 커뮤니티 댓글 삭제
	public int deleteComment(@Param("comment_id") Long comment_id, @Param("user_id") String user_id);

	// 커뮤니티 댓글의 글 번호 조회
	public Long getPostIdByCommentId(Long comment_id);
}
