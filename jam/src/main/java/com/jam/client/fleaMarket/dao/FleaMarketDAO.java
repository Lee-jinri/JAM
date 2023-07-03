package com.jam.client.fleaMarket.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.jam.client.fleaMarket.vo.FleaMarketVO;

public interface FleaMarketDAO {

	// 중고거래 list
	public List<FleaMarketVO> fleaList(FleaMarketVO flea_vo);

	// 중고거래 페이징
	public int fleaListCnt(FleaMarketVO flea_vo);
	
	// 중고거래 조회수 증가 메소드
	public void fleaReadCnt(FleaMarketVO flea_vo);

	// 중고거래 detail
	public FleaMarketVO fleaDetail(FleaMarketVO flea_vo);

	// 중고거래 insert
	public int fleaInsert(FleaMarketVO flea_vo);

	// 중고거래 update Form
	public FleaMarketVO fleaUpdateForm (FleaMarketVO flea_vo);
	
	// 중고거래 update
	public int fleaUpdate(FleaMarketVO flea_vo);

	// 중고거래 delete
	public int fleaDelete(FleaMarketVO flea_vo);

	// 중고거래 댓글 개수 증감
	public void updateReplyCnt(@Param("flea_no") int flea_no, @Param("amount") int amount);

}
