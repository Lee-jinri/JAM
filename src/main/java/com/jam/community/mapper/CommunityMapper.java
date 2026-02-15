package com.jam.community.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.jam.community.dto.CommunityDto;

public interface CommunityMapper {
	// 커뮤니티 전체 글 조회
	public List<CommunityDto> getBoard(CommunityDto com_vo);
	public List<CommunityDto> getBoardWithFavorite(CommunityDto com_vo);

	// 커뮤니티 페이징
	public int listCnt(CommunityDto com_vo);
	
	// 커뮤니티 조회수 증가
	public void incrementReadCnt(Long post_id);
	
	// 커뮤니티 상세 페이지
	public CommunityDto getPost(Long post_id);
	
	// 커뮤니티 글 작성
	public int writePost(CommunityDto com_vo);

	// 커뮤니티 수정할 글 정보 불러오기
	public CommunityDto getPostById(Long post_id);
	
	// 커뮤니티 글 수정
	public int editPost(CommunityDto com_vo);
	
	// 커뮤니티 글 삭제
	public int deletePost(@Param("post_id") Long post_id, @Param("user_id") String user_id);

	// 댓글 개수 수정
	public void updateCommentCnt(@Param("post_id") Long post_id, @Param("amount") int amount);
	
	// 인기글
	public List<CommunityDto> getPopularBoard();
	
	// 작성한 커뮤니티 글 조회
	public List<CommunityDto> getMyPosts(CommunityDto com_vo);
	
	// 작성한 커뮤니티 글 개수 
	public int getMyPostsCnt(CommunityDto com_vo);
	
	// 작성한 커뮤니티 글 삭제
	public void deleteMyPosts(@Param("user_id") String userId, @Param("postIds") List<Long> postIds);
	
	// 북마크한 글
	public List<CommunityDto> getFavorites(CommunityDto community);
	
	// 북마크한 글 개수
	public int favoritesListCnt(CommunityDto community);
}
