package com.jam.client.fleaMarket.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.jam.client.fleaMarket.vo.FleaMarketVO;
import com.jam.file.vo.ImageFileVO;

public interface FleaMarketDAO {

	// 중고거래 list
	public List<FleaMarketVO> getBoard(FleaMarketVO flea_vo);
	
	public List<FleaMarketVO> getBoardWithFavorite(FleaMarketVO flea_vo);

	// 중고거래 페이징
	public int listCnt(FleaMarketVO flea_vo);
	
	// 중고거래 조회수 증가 메소드
	public void incrementReadCnt(Long post_id);

	// 중고거래 상세 페이지 조회
	public FleaMarketVO getPostDetail(FleaMarketVO flea);

	// 중고거래 글 작성
	int writePost(FleaMarketVO flea_vo);

	// 중고거래 수정 글 정보
	public FleaMarketVO getPostForEdit(Long post_id);
	
	// 중고거래 글 수정
	public int editPost(FleaMarketVO flea_vo);

	// 중고거래 글 삭제
	public int deletePost(@Param("post_id") Long post_id, @Param("user_id") String userId);

	// 중고거래 댓글 개수 증감
	public void updateCommentCount(@Param("post_id") Long post_id, @Param("amount") int amount);

	public List<FleaMarketVO> getMyStore(FleaMarketVO flea_vo);

	public int getMyStoreCnt(FleaMarketVO flea_vo);

	public Long getNextPostId();

	public List<FleaMarketVO> getFavorites(FleaMarketVO flea);

	public List<ImageFileVO> getImages(Long post_id);

}
