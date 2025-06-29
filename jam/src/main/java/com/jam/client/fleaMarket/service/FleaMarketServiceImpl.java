package com.jam.client.fleaMarket.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.jam.client.community.vo.CommunityVO;
import com.jam.client.fleaMarket.dao.FleaMarketDAO;
import com.jam.client.fleaMarket.vo.FleaMarketVO;
import com.jam.client.member.service.MemberService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FleaMarketServiceImpl implements FleaMarketService {

	private final FleaMarketDAO fleaDao;
	private final MemberService memberService;
	
	@Override
	public List<FleaMarketVO> getBoard(FleaMarketVO flea_vo) {
		
		List<FleaMarketVO> list = new ArrayList<>(); 
		
		if(flea_vo.getUser_id() == null) list = fleaDao.getBoard(flea_vo);
		else list = fleaDao.getBoardWithFavorite(flea_vo);
		
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
	public FleaMarketVO getPostDetail(Long flea_no) {
		
		FleaMarketVO detail = fleaDao.getPostDetail(flea_no);
		
		return detail;
	}

	@Override
	public int writePost(FleaMarketVO flea_vo) {
		return fleaDao.writePost(flea_vo);
	}

	@Override
	public FleaMarketVO getPostForEdit(Long flea_no) {
		
		FleaMarketVO updateData =  fleaDao.getPostForEdit(flea_no);
		
		return updateData;
	}

	@Override
	public int editPost(FleaMarketVO flea_vo) {
		
		return fleaDao.editPost(flea_vo);
	}

	@Override
	public int deletePost(Long flea_no) {
	
		return fleaDao.deletePost(flea_no);
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
