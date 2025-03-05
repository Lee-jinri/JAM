package com.jam.client.community.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.jam.client.community.vo.CommunityVO;

public interface CommunityDAO {
	// 커뮤니티 전체 글 조회
	public List<CommunityVO> getBoards(CommunityVO com_vo);

	// 커뮤니티 페이징
	public int listCnt(CommunityVO com_vo);
	
	// 커뮤니티 조회수 증가
	public void incrementReadCnt(Long com_no);
	
	// 커뮤니티 상세 페이지
	public CommunityVO getBoardDetail(Long com_no);
	
	// 커뮤니티 글 작성
	public int writeBoard(CommunityVO com_vo);

	// 커뮤니티 수정할 글 정보 불러오기
	public CommunityVO getBoardById(Long com_no);
	
	// 커뮤니티 글 수정
	public int editBoard(CommunityVO com_vo, String user_id);
	
	// 커뮤니티 글 삭제
	public int boardDelete(Long com_no, String user_id);

	// 댓글 개수 수정
	public void updateReplyCnt(@Param("com_no") Long com_no, @Param("amount") int amount);

	// 특정 회원의 커뮤니티 글 조회
	public List<CommunityVO> getPosts(CommunityVO com_vo);
	
	// 특정 회원의 커뮤니티 글 개수 
	public int getUserPostCnt(CommunityVO com_vo);
	
}
