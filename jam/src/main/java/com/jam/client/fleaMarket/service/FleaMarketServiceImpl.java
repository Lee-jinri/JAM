package com.jam.client.fleaMarket.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jam.client.fleaMarket.dao.FleaMarketDAO;
import com.jam.client.fleaMarket.vo.FleaMarketVO;

@Service
public class FleaMarketServiceImpl implements FleaMarketService {

	@Autowired
	private FleaMarketDAO fleaDao;
	
	@Override
	public List<FleaMarketVO> fleaList(FleaMarketVO flea_vo) {
		
		List<FleaMarketVO> list = fleaDao.fleaList(flea_vo);
		
		return list;
	}
	
	@Override
	public int fleaListCnt(FleaMarketVO flea_vo) {
		return fleaDao.fleaListCnt(flea_vo);
	}

	@Override
	public void fleaReadCnt(FleaMarketVO flea_no) {
		
		fleaDao.fleaReadCnt(flea_no);
	}

	@Override
	public FleaMarketVO fleaDetail(FleaMarketVO flea_no) {
		
		FleaMarketVO detail = fleaDao.fleaDetail(flea_no);
		
		return detail;
	}

	@Override
	public int fleaInsert(FleaMarketVO flea_vo) {
		return fleaDao.fleaInsert(flea_vo);
	}

	@Override
	public FleaMarketVO fleaUpdateForm(FleaMarketVO flea_vo) {
		
		FleaMarketVO updateData =  fleaDao.fleaUpdateForm(flea_vo);
		
		return updateData;
	}

	@Override
	public int fleaUpdate(FleaMarketVO flea_vo) {
		
		return fleaDao.fleaUpdate(flea_vo);
	}

	@Override
	public int fleaDelete(FleaMarketVO flea_vo) {
	
		return fleaDao.fleaDelete(flea_vo);
	}

	

}
