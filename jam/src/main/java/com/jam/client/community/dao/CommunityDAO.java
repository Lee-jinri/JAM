package com.jam.client.community.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.jam.client.community.vo.CommunityVO;

public interface CommunityDAO {
	
	// 커뮤니티 list
	public List<CommunityVO> communityList(CommunityVO com_vo);

	// 커뮤니티 페이징
	public int comListCnt(CommunityVO com_vo);
	
	// 커뮤니티 조회수 
	public void comReadCnt(CommunityVO com_vo);
	
	// 커뮤니티 detail
	public CommunityVO communityDetail(CommunityVO com_vo);
	
	// 커뮤니티 insert
	public int comInsert(CommunityVO com_vo);

	// 커뮤니티 update Form
	public CommunityVO comUpdateForm(CommunityVO com_vo);
	
	// 커뮤니티 update
	public int comUpdate(CommunityVO com_vo);
	
	// 커뮤니티 delete
	public int comDelete(CommunityVO com_vo);

	// 댓글 개수 수정
	public void updateReplyCnt(@Param("com_no") int com_no, @Param("amount") int amount);
	
}
