package com.jam.client.fleaMarket.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jam.client.community.vo.CommunityVO;
import com.jam.client.fleaMarket.dao.FleaMarketDAO;
import com.jam.client.fleaMarket.vo.FleaMarketVO;
import com.jam.client.member.service.MemberService;

@Service
public class FleaMarketServiceImpl implements FleaMarketService {

	@Autowired
	private FleaMarketDAO fleaDao;
	
	@Autowired
	private MemberService memberService;
	
	@Override
	public List<FleaMarketVO> getBoards(FleaMarketVO flea_vo) {
		
		List<FleaMarketVO> list = fleaDao.getBoards(flea_vo);
		
		return list;
	}
	
	@Override
	public int listCnt(FleaMarketVO flea_vo) {
		return fleaDao.listCnt(flea_vo);
	}

	@Override
	public void incrementReadCnt(Long flea_no) {
		
		fleaDao.incrementReadCnt(flea_no);
	}

	@Override
	public FleaMarketVO getBoardDetail(Long flea_no) {
		
		FleaMarketVO detail = fleaDao.getBoardDetail(flea_no);
		
		return detail;
	}

	@Override
	public int writeBoard(FleaMarketVO flea_vo) {
		return fleaDao.writeBoard(flea_vo);
	}

	@Override
	public FleaMarketVO getBoardById(Long flea_no) {
		
		FleaMarketVO updateData =  fleaDao.getBoardById(flea_no);
		
		return updateData;
	}

	@Override
	public int editBoard(FleaMarketVO flea_vo) {
		
		return fleaDao.editBoard(flea_vo);
	}

	@Override
	public int boardDelete(Long flea_no) {
	
		return fleaDao.boardDelete(flea_no);
	}

	@Override
	public List<CommunityVO> getPosts(FleaMarketVO flea_vo) {
		return fleaDao.getPosts(flea_vo);
	}

	@Override
	public int getUserPostCnt(FleaMarketVO flea_vo) {
		return fleaDao.getUserPostCnt(flea_vo);
	}

	@Override
	public String getUserId(String user_name) {
		return memberService.getUserId(user_name);
	}
	
	@Override
	public boolean isValidUserName(String user_name) throws Exception {
		int count = memberService.nameCheck(user_name);
		
		return count != 0 ? true : false;
	}
	

}
