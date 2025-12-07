package com.jam.client.community.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.jam.client.community.dao.CommunityDAO;
import com.jam.client.community.vo.CommunityVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommunityServiceImpl implements CommunityService {
	
	private final CommunityDAO comDao;
	
	@Override
	public List<CommunityVO>getBoard(CommunityVO com_vo){
		
		List<CommunityVO> list = new ArrayList<>();
		
		if(com_vo.getUser_id() == null) list = comDao.getBoard(com_vo);
		else list = comDao.getBoardWithFavorite(com_vo);
		
		return list;
	}
	
	@Override
	public int listCnt(CommunityVO com_vo) {
		return comDao.listCnt(com_vo);
	}
	
	@Override
	public void incrementReadCnt(Long post_id) {
		comDao.incrementReadCnt(post_id);
	}
	
	@Override
	public CommunityVO getPost(Long post_id) {
		
		return comDao.getPost(com_no);
	}

	@Override
	public int writePost(CommunityVO com_vo) {
		
		return comDao.writePost(com_vo);
	}
	
	@Override
	public CommunityVO getPostById(Long com_no) {
		
		return comDao.getPostById(com_no);
	}
	
	@Override
	public int editPost(CommunityVO com_vo, String user_id) {
		
		return comDao.editPost(com_vo, user_id);
	}

	@Override
	public int deletePost(Long com_no, String user_id) {
		
		return comDao.deletePost(com_no, user_id);
	}

	@Override
	public List<CommunityVO> getUserPosts(CommunityVO com_vo){
		return comDao.getUserPosts(com_vo);
	}

	@Override
	public int getUserPostCnt(CommunityVO com_vo) {
		return comDao.getUserPostCnt(com_vo);
	}

	@Override
	public List<CommunityVO> getPopularBoard(CommunityVO community) {
		return comDao.getPopularBoard(community);
	}
}
