package com.jam.client.community.service;

import java.util.List;

import com.jam.client.community.vo.CommunityVO;

public interface CommunityService {
	
	// 커뮤니티 list
	public List<CommunityVO> communityList(CommunityVO com_vo);
	
	// 커뮤니티 페이징
	public int comListCnt(CommunityVO com_vo);
	
	// 커뮤니티 조회수
	public void comReadCnt(CommunityVO com_vo);
	
	// 커뮤니티 detail
	public CommunityVO boardDetail(CommunityVO com_vo);

	// 커뮤니티 insert
	public int comInsert(CommunityVO com_vo) throws Exception;
	
	// 커뮤니티 update Form
	public CommunityVO comUpdateForm(CommunityVO com_vo);
	
	// 커뮤니티 update
	public int comUpdate(CommunityVO com_vo);
	
	// 커뮤니티 delete
	public int comDelete(CommunityVO com_vo);

	

	
	
	

}
