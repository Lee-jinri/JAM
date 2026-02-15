package com.jam.fleaMarket.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.jam.fleaMarket.dto.FleaMarketDto;

public interface FleaMarketMapper {
	// 중고거래 list
	public List<FleaMarketDto> getBoard(FleaMarketDto flea_vo);
	
	public List<FleaMarketDto> getBoardWithFavorite(FleaMarketDto flea_vo);

	// 중고거래 페이징
	public int listCnt(FleaMarketDto flea_vo);
	
	// 중고거래 조회수 증가 메소드
	public void incrementReadCnt(Long post_id);

	// 중고거래 상세 페이지 조회
	public FleaMarketDto getPostDetail(FleaMarketDto flea);

	// 중고거래 글 작성
	int writePost(FleaMarketDto flea_vo);

	// 중고거래 수정 글 정보
	public FleaMarketDto getPostForEdit(Long post_id);
	
	// 중고거래 글 수정
	public int editPost(FleaMarketDto flea_vo);

	// 중고거래 글 삭제
	public int deletePost(@Param("post_id") Long post_id, @Param("user_id") String userId);

	// 중고거래 댓글 개수 증감
	public void updateCommentCount(@Param("post_id") Long post_id, @Param("amount") int amount);

	public List<FleaMarketDto> getMyStore(FleaMarketDto flea_vo);

	public int getMyStoreCnt(FleaMarketDto flea_vo);

	public Long getNextPostId();

	public List<FleaMarketDto> getFavorites(FleaMarketDto flea);
}
