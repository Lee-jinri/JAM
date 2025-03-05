package com.jam.client.fleaMarket.service;

import java.util.List;

import com.jam.client.community.vo.CommunityVO;
import com.jam.client.fleaMarket.vo.FleaMarketVO;

public interface FleaMarketService {

	// 중고거래 전체 글 조회
	List<FleaMarketVO> getBoards(FleaMarketVO flea_vo);

	// 중고거래 페이징
	int listCnt(FleaMarketVO flea_vo);
	
	// 중고거래 조회수 증가
	void incrementReadCnt(Long flea_no);

	// 중고거래 상세페이지 조회
	FleaMarketVO getBoardDetail(Long flea_no);

	// 중고거래 글 작성
	int writeBoard(FleaMarketVO flea_vo);

	// 중고거래 수정할 글 정보 불러오기
	FleaMarketVO getBoardById(Long flea_no);

	// 중고거래  글 수정
	int editBoard(FleaMarketVO flea_vo);

	// 중고거래 글 삭제
	int boardDelete(Long flea_no);

	List<CommunityVO> getPosts(FleaMarketVO flea_vo);

	int getUserPostCnt(FleaMarketVO flea_vo);

	String getUserId(String user_name);

	boolean isValidUserName(String user_name) throws Exception;
	
}
