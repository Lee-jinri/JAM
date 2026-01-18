package com.jam.client.community.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.jam.client.community.vo.CommunityVO;

public interface CommunityDAO {
	// 커뮤니티 전체 글 조회
	public List<CommunityVO> getBoard(CommunityVO com_vo);
	public List<CommunityVO> getBoardWithFavorite(CommunityVO com_vo);

	// 커뮤니티 페이징
	public int listCnt(CommunityVO com_vo);
	
	// 커뮤니티 조회수 증가
	public void incrementReadCnt(Long post_id);
	
	// 커뮤니티 상세 페이지
	public CommunityVO getPost(Long post_id);
	
	// 커뮤니티 글 작성
	public int writePost(CommunityVO com_vo);

	// 커뮤니티 수정할 글 정보 불러오기
	public CommunityVO getPostById(Long post_id);
	
	// 커뮤니티 글 수정
	public int editPost(CommunityVO com_vo);
	
	// 커뮤니티 글 삭제
	public int deletePost(@Param("post_id") Long post_id, @Param("user_id") String user_id);

	// 댓글 개수 수정
	public void updateCommentCnt(@Param("post_id") Long post_id, @Param("amount") int amount);
	
	// 인기글
	public List<CommunityVO> getPopularBoard();
	
	// 작성한 커뮤니티 글 조회
	public List<CommunityVO> getMyPosts(CommunityVO com_vo);
	
	// 작성한 커뮤니티 글 개수 
	public int getMyPostsCnt(CommunityVO com_vo);
	
	// 작성한 커뮤니티 글 삭제
	public void deleteMyPosts(@Param("user_id") String userId, @Param("postIds") List<Long> postIds);
	
	// 북마크한 글
	public List<CommunityVO> getFavorites(CommunityVO community);
	
	// 북마크한 글 개수
	public int favoritesListCnt(CommunityVO community);
}
