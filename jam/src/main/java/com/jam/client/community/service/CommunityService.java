package com.jam.client.community.service;

import java.util.List;
import java.util.Map;

import com.jam.client.community.vo.CommunityVO;

public interface CommunityService {
	
	// 커뮤니티 전체 글 조회
	public List<CommunityVO> getBoard(CommunityVO com_vo);
	
	// 커뮤니티 페이징
	public int listCnt(CommunityVO com_vo);

	// 인기글
	public List<CommunityVO> getPopularBoard();
	
	// 커뮤니티 조회수 증가
	public void incrementReadCnt(Long com_no);
	
	// 커뮤니티 상세페이지 조회
	public CommunityVO getPost(Long com_no);

	// 커뮤니티 글 작성
	public Long writePost(CommunityVO com_vo);

	// 수정할 글 + 이미지 파일 조회
	public Map<String, Object> getPostForEdit(Long post_id);
	
	// 커뮤니티 글 수정
	public int editPost(CommunityVO com_vo);
	
	// 커뮤니티 글 삭제
	public void deletePost(Long com_no, String user_id);	

	// 작성한 커뮤니티 글
	public List<CommunityVO> getMyPosts(CommunityVO com_vo);

	// 작성한 커뮤니티 글 개수
	public int getMyPostsCnt(CommunityVO com_vo);

	// 작성한 글 삭제
	public void deleteMyPosts(String userId, List<Long> postIds);

	// 북마크한 글
	public List<CommunityVO> getFavorites(CommunityVO community);

	// 북마크한 글 개수
	public int favoritesListCnt(CommunityVO community);
}
