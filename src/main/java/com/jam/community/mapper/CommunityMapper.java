package com.jam.community.mapper;

import java.util.List;

import com.jam.community.dto.CommunityDto;

public interface CommunityMapper {
	// 커뮤니티 전체 글 조회
	public List<CommunityDto> getBoard(CommunityDto com_vo);
	public List<CommunityDto> getBoardWithFavorite(CommunityDto com_vo);

	// 커뮤니티 페이징
	public int listCnt(CommunityDto com_vo);

	// 인기글
	public List<CommunityDto> getPopularBoard();

	// 북마크한 글
	public List<CommunityDto> getFavorites(CommunityDto community);
	
	// 북마크한 글 개수
	public int favoritesListCnt(CommunityDto community);
}
