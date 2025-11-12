package com.jam.client.comComment.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jam.client.comComment.dao.CommentDAO;
import com.jam.client.comComment.vo.CommentVO;
import com.jam.client.community.dao.CommunityDAO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
	
	private final static int COMMENT_ADD  = 1;
	private final static int COMMENT_DEL  = -1;
	
	private final CommentDAO commentDao;
	private final CommunityDAO comDao;
	
	@Override
	public List<CommentVO> commentList(Long post_id, String user_id) {
		List<CommentVO> list = commentDao.commentList(post_id, user_id);
		
		return list;
	}
	
	@Transactional
	@Override
	public int insertComment(CommentVO c) {
   
		// 댓글 개수 증가
		comDao.updateCommentCnt(c.getPost_id(), COMMENT_ADD);
		return commentDao.insertComment(c);
	}

	@Override
	public int updateComment(CommentVO c) {
		return commentDao.updateComment(c);
	}

	@Transactional
	@Override
	public int deleteComment(Long comment_id, String user_id) {
		
		// 댓글 개수 감소
		Long post_id = commentDao.getPostIdByCommentId(comment_id);
		comDao.updateCommentCnt(post_id, COMMENT_DEL);
		
		return commentDao.deleteComment(comment_id, user_id);
	}
}
