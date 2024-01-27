package com.jam.client.fleaMarket.service;

import java.util.List;

import com.jam.client.fleaMarket.vo.FleaMarketVO;

public interface FleaMarketService {

	// 중고거래 전체 글 조회
	public List<FleaMarketVO> getBoards(FleaMarketVO flea_vo);

	// 중고거래 페이징
	public int listCnt(FleaMarketVO flea_vo);
	
	// 중고거래 조회수 증가
	public void incrementReadCnt(Long flea_no);

	// 중고거래 상세페이지 조회
	public FleaMarketVO getBoardDetail(Long flea_no);

	// 중고거래 글 작성
	public int writeBoard(FleaMarketVO flea_vo);

	// 중고거래 수정할 글 정보 불러오기
	public FleaMarketVO getBoardById(Long flea_no);

	// 중고거래  글 수정
	public int editBoard(FleaMarketVO flea_vo);

	// 중고거래 글 삭제
	public int boardDelete(Long flea_no);

	

	

	
}
