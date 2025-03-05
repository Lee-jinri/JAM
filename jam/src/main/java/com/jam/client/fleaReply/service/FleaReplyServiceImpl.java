package com.jam.client.fleaReply.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jam.client.fleaMarket.dao.FleaMarketDAO;
import com.jam.client.fleaReply.dao.FleaReplyDAO;
import com.jam.client.fleaReply.vo.FleaReplyVO;

import lombok.Setter;

@Service
public class FleaReplyServiceImpl implements FleaReplyService {

	private final static int reply_Add = 1;
	private final static int reply_Del = -1;
	
	@Setter(onMethod_=@Autowired)
	private FleaReplyDAO fleareplyDao;
	
	@Setter(onMethod_=@Autowired)
	private FleaMarketDAO fleaDao;

	@Override
	public List<FleaReplyVO> fleaReplyList(Long flea_no) {
		List<FleaReplyVO> list = fleareplyDao.replyList(flea_no);
		return list;
	}

	@Transactional
	@Override
	public int replyInsert(FleaReplyVO frvo) {
		fleaDao.updateReplyCnt(frvo.getFlea_no(), reply_Add);
		return fleareplyDao.replyInsert(frvo);
	}

	@Override
	public int replyUpdate(FleaReplyVO frvo) {
		return fleareplyDao.replyUpdate(frvo);
	}

	@Transactional
	@Override
	public int replyDelete(Long fleaReply_no) {
		
		FleaReplyVO vo = fleareplyDao.replyRead(fleaReply_no);
		fleaDao.updateReplyCnt(vo.getFlea_no(), reply_Del);
		
		return fleareplyDao.replyDelete(fleaReply_no);
	}
	
}
