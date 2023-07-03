package com.jam.client.fleaMarket.service;

import java.util.List;

import com.jam.client.fleaMarket.vo.FleaMarketVO;

public interface FleaMarketService {

	// 중고거래 list
	public List<FleaMarketVO> fleaList(FleaMarketVO flea_vo);

	// 중고거래 list 페이징
	public int fleaListCnt(FleaMarketVO flea_vo);
	
	// 중고거래 조회수 증가 메소드
	public void fleaReadCnt(FleaMarketVO flea_vo);

	// 중고거래 detail
	public FleaMarketVO fleaDetail(FleaMarketVO flea_no);

	// 중고거래 insert
	public int fleaInsert(FleaMarketVO flea_vo);

	// 중고거래 update Form
	public FleaMarketVO fleaUpdateForm(FleaMarketVO flea_vo);

	// 중고거래 update
	public int fleaUpdate(FleaMarketVO flea_vo);

	// 중고거래 delete
	public int fleaDelete(FleaMarketVO flea_vo);

	

	
}
