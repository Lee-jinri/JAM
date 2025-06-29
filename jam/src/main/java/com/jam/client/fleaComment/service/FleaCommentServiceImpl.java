package com.jam.client.fleaComment.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jam.client.fleaComment.dao.FleaCommentDAO;
import com.jam.client.fleaComment.vo.FleaCommentVO;
import com.jam.client.fleaMarket.dao.FleaMarketDAO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FleaCommentServiceImpl implements FleaCommentService{

	private final static int comment_Add = 1;
	private final static int comment_Del = -1;
	
	private final FleaCommentDAO fleaCommentDao;
	private final FleaMarketDAO fleaDao;
	
	@Override
	public List<FleaCommentVO> commentList(Long post_id) {
		List<FleaCommentVO> list = fleaCommentDao.commentList(post_id);
		return list;
	}

	@Transactional
	@Override
	public int commentInsert(FleaCommentVO comment) {
		fleaDao.updateCommentCount(comment.getPost_id(), comment_Add);
		return fleaCommentDao.commentInsert(comment);
	}

	@Override
	public int commentUpdate(FleaCommentVO comment) {
		return fleaCommentDao.commentUpdate(comment);
	}

	@Transactional
	@Override
	public int commentDelete(Long comment_id) {
		
		FleaCommentVO vo = fleaCommentDao.getPostIdByCommentId(comment_id);
		fleaDao.updateCommentCount(vo.getPost_id(), comment_Del);
		
		return fleaCommentDao.commentDelete(comment_id);
	}

	
}
