package com.jam.comComment.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jam.comComment.dto.CommentDto;
import com.jam.comComment.mapper.CommentMapper;
import com.jam.community.mapper.CommunityMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentService {

	private final static int COMMENT_ADD  = 1;
	private final static int COMMENT_DEL  = -1;
	
	private final CommentMapper commentMapper;
	private final CommunityMapper communityMapper;
	
	public List<CommentDto> commentList(Long post_id, String user_id) {
		List<CommentDto> list = commentMapper.commentList(post_id, user_id);
		return list;
	}
	
	@Transactional
	public int insertComment(CommentDto c) {
		// 댓글 개수 증가
		communityMapper.updateCommentCnt(c.getPost_id(), COMMENT_ADD);
		return commentMapper.insertComment(c);
	}

	public int updateComment(CommentDto c) {
		return commentMapper.updateComment(c);
	}

	@Transactional
	public int deleteComment(Long comment_id, String user_id) {
		// 댓글 개수 감소
		Long post_id = commentMapper.getPostIdByCommentId(comment_id);
		communityMapper.updateCommentCnt(post_id, COMMENT_DEL);
		
		return commentMapper.deleteComment(comment_id, user_id);
	}
}
