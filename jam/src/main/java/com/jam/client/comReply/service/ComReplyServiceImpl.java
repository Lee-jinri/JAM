package com.jam.client.comReply.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jam.client.comReply.dao.ComReplyDAO;
import com.jam.client.comReply.vo.ComReplyVO;
import com.jam.client.community.dao.CommunityDAO;

import lombok.Setter;
import lombok.extern.log4j.Log4j;

@Log4j
@Service
public class ComReplyServiceImpl implements ComReplyService {
	
	private final static int reply_Add = 1;
	private final static int reply_Del = -1;
	
	@Setter(onMethod_=@Autowired)
	private ComReplyDAO comreplyDao;
	
	@Setter(onMethod_=@Autowired)
	private CommunityDAO comDao;
	
	@Override
	public List<ComReplyVO> comReplyList(Integer com_no) {
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
	public int replyDelete(Integer comReply_no) {
		
		// 댓글 개수 감소
		ComReplyVO vo = comreplyDao.replyRead(comReply_no);
		comDao.updateReplyCnt(vo.getCom_no(), reply_Del);
		
		return comreplyDao.replyDelete(comReply_no);
	}



}
