package com.jam.client.fleaMarket.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.jam.client.community.vo.CommunityVO;
import com.jam.client.fleaMarket.vo.FleaMarketVO;

public interface FleaMarketDAO {

	// 중고거래 list
	public List<FleaMarketVO> getBoard(FleaMarketVO flea_vo);
	
	public List<FleaMarketVO> getBoardWithFavorite(FleaMarketVO flea_vo);

	// 중고거래 페이징
	public int listCnt(FleaMarketVO flea_vo);
	
	// 중고거래 조회수 증가 메소드
	public void incrementReadCnt(Long flea_no);

	// 중고거래 상세 페이지 조회
	public FleaMarketVO getPostDetail(Long flea_no);

	// 중고거래 글 작성
	public int writePost(FleaMarketVO flea_vo);

	// 중고거래 수정 글 정보
	public FleaMarketVO getPostForEdit(Long flea_no);
	
	// 중고거래 글 수정
	public int editPost(FleaMarketVO flea_vo);

	// 중고거래 글 삭제
	public int deletePost(Long flea_no);

	// 중고거래 댓글 개수 증감
	public void updateCommentCount(@Param("flea_no") Long flea_no, @Param("amount") int amount);

	public List<CommunityVO> getPosts(FleaMarketVO flea_vo);

	public int getUserPostCnt(FleaMarketVO flea_vo);

}
