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
	public List<CommunityVO>communityList(CommunityVO com_vo){
		
		List<CommunityVO> list = comDao.communityList(com_vo);
		
		return list;
	}
	
	@Override
	public int comListCnt(CommunityVO com_vo) {
		return comDao.comListCnt(com_vo);
	}
	
	@Override
	public void comReadCnt(CommunityVO com_vo) {
		comDao.comReadCnt(com_vo);
	}

	@Override
	public CommunityVO boardDetail(CommunityVO com_vo) {
		
		return comDao.communityDetail(com_vo);
	}

	@Override
	public int comInsert(CommunityVO com_vo) throws Exception {
		
		return comDao.comInsert(com_vo);
	}
	
	@Override
	public CommunityVO comUpdateForm(CommunityVO com_vo) {
		
		return comDao.comUpdateForm(com_vo);
	}

	@Override
	public int comUpdate(CommunityVO com_vo) {
		
		return comDao.comUpdate(com_vo);
	}

	@Override
	public int comDelete(CommunityVO com_vo) {
		
		return comDao.comDelete(com_vo);
	}

}
