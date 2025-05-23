package com.jam.client.comReply.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jam.client.comReply.dao.ComReplyDAO;
import com.jam.client.comReply.vo.ComReplyVO;
import com.jam.client.community.dao.CommunityDAO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ComReplyServiceImpl implements ComReplyService {
	
	private final static int reply_Add = 1;
	private final static int reply_Del = -1;
	
	private final ComReplyDAO comreplyDao;
	private final CommunityDAO comDao;
	
	@Override
	public List<ComReplyVO> comReplyList(Long com_no) {
		List<ComReplyVO> list = comreplyDao.replyList(com_no);
		
		return list;
	}
	
	@Transactional
	@Override
	public int replyInsert(ComReplyVO crvo) {
		
		// 댓글 개수 증가
		comDao.updateReplyCnt(crvo.getCom_no(), reply_Add);
		return comreplyDao.replyInsert(crvo);
	}

	@Override
	public int replyUpdate(ComReplyVO crvo) {
		return comreplyDao.replyUpdate(crvo);
	}

	@Transactional
	@Override
	public int replyDelete(Long comReply_no, String user_id) {
		
		// 댓글 개수 감소
		Long com_no = comreplyDao.getBoardNoByReplyNo(comReply_no);
		comDao.updateReplyCnt(com_no, reply_Del);
		
		return comreplyDao.replyDelete(comReply_no, user_id);
	}



}
