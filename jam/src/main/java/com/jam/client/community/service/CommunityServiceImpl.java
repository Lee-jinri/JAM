package com.jam.client.community.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jam.client.community.dao.CommunityDAO;
import com.jam.client.community.vo.CommunityVO;
import com.jam.client.member.service.MemberService;

@Service
public class CommunityServiceImpl implements CommunityService {
	
	@Autowired
	private CommunityDAO comDao;
	
	@Autowired
	private MemberService memberService;
	
	@Override
	public List<CommunityVO>getBoards(CommunityVO com_vo){
		
		List<CommunityVO> list = new ArrayList<>();
		
		if(com_vo.getUser_id() == null) list = comDao.getBoards(com_vo);
		else list = comDao.getBoardsWithFavorite(com_vo);
		
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
	public int editBoard(CommunityVO com_vo, String user_id) {
		
		return comDao.editBoard(com_vo, user_id);
	}

	@Override
	public int boardDelete(Long com_no, String user_id) {
		
		return comDao.boardDelete(com_no, user_id);
	}

	@Override
	public List<CommunityVO> getPosts(CommunityVO com_vo){
		return comDao.getPosts(com_vo);
	}

	@Override
	public int getUserPostCnt(CommunityVO com_vo) {
		return comDao.getUserPostCnt(com_vo);
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
