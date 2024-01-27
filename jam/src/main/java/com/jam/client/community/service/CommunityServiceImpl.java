package com.jam.client.community.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jam.client.community.dao.CommunityDAO;
import com.jam.client.community.vo.CommunityVO;

@Service
public class CommunityServiceImpl implements CommunityService {
	
	@Autowired
	private CommunityDAO comDao;
	
	@Override
	public List<CommunityVO>getBoards(CommunityVO com_vo){
		
		List<CommunityVO> list = comDao.getBoards(com_vo);
		
		return list;
	}
	
	@Override
	public int listCnt(CommunityVO com_vo) {
		return comDao.listCnt(com_vo);
	}
	
	@Override
	public void incrementReadCnt(Long com_no) {
		comDao.incrementReadCnt(com_no);
	}
	
	@Override
	public CommunityVO getBoardDetail(Long com_no) {
		
		return comDao.getBoardDetail(com_no);
	}

	@Override
	public int writeBoard(CommunityVO com_vo) {
		
		return comDao.writeBoard(com_vo);
	}
	
	@Override
	public CommunityVO getBoardById(Long com_no) {
		
		return comDao.getBoardById(com_no);
	}

	@Override
	public int editBoard(CommunityVO com_vo) {
		
		return comDao.editBoard(com_vo);
	}

	@Override
	public int boardDelete(Long com_no) {
		
		return comDao.boardDelete(com_no);
	}



}
