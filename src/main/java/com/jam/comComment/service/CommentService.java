package com.jam.comComment.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jam.comComment.dto.CommentDto;
import com.jam.comComment.mapper.CommentMapper;
import com.jam.community.repository.CommunityRepository;
import com.jam.global.exception.ForbiddenException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentService {

	private final static int COMMENT_ADD  = 1;
	private final static int COMMENT_DEL  = -1;
	
	private final CommentMapper commentMapper;
	private final CommunityRepository communityRepository;
	
	public List<CommentDto> commentList(Long post_id, String user_id) {
		List<CommentDto> list = commentMapper.commentList(post_id, user_id);
		return list;
	}
	
	@Transactional
	public int insertComment(CommentDto c) {
		// 댓글 개수 증가
		communityRepository.updateCommentCount(c.getPost_id(), COMMENT_ADD);
		return commentMapper.insertComment(c);
	}

	public int updateComment(CommentDto c) {
		int updated = commentMapper.updateComment(c);
		if (updated != 1) {
			throw new ForbiddenException("수정 권한이 없습니다.");
		}
		return updated;
	}

	@Transactional
	public int deleteComment(Long comment_id, String user_id) {
		Long postId = commentMapper.getPostIdByCommentId(comment_id);
		int deleted = commentMapper.deleteComment(comment_id, user_id);
		if (deleted != 1) {
			throw new ForbiddenException("삭제 권한이 없습니다.");
		}

		// 댓글 개수 감소 (실제 삭제가 성공했을 때만)
		communityRepository.updateCommentCount(postId, COMMENT_DEL);

		return deleted;
	}
}
