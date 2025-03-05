package com.jam.client.community.service;

import java.util.List;

import com.jam.client.community.vo.CommunityVO;

public interface CommunityService {
	
	// 커뮤니티 전체 글 조회
	public List<CommunityVO> getBoards(CommunityVO com_vo);
	
	// 커뮤니티 페이징
	public int listCnt(CommunityVO com_vo);
	
	// 커뮤니티 조회수 증가
	public void incrementReadCnt(Long com_no);
	
	// 커뮤니티 상세페이지 조회
	public CommunityVO getBoardDetail(Long com_no);

	// 커뮤니티 글 작성
	public int writeBoard(CommunityVO com_vo);
	
	// 커뮤니티 수정할 글 정보 불러오기
	public CommunityVO getBoardById(Long com_no);
	
	// 커뮤니티 글 수정
	public int editBoard(CommunityVO com_vo, String user_id);
	
	// 커뮤니티 글 삭제
	public int boardDelete(Long com_no, String user_id);	

	// 특정 회원의 커뮤니티 글
	public List<CommunityVO> getPosts(CommunityVO com_vo);

	// 특정 회원의 커뮤니티 글 개수
	public int getUserPostCnt(CommunityVO com_vo);

	// 닉네임으로 사용자 아이디 가져오기
	public String getUserId(String user_name);

	// 존재하는 사용자 닉네임인지 확인 
	// 사용자가 주소값 임의로 변경해서 오류 날 수도 있으니까
	public boolean isValidUserName(String user_name) throws Exception;
}
