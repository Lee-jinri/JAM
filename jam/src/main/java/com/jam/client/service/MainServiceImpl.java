package com.jam.client.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.jam.client.community.vo.CommunityVO;
import com.jam.client.dao.MainDAO;
import com.jam.client.fleaMarket.vo.FleaMarketVO;
import com.jam.client.roomRental.vo.RoomRentalVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MainServiceImpl implements MainService {

	private final MainDAO mainDao;

	@Override
	public List<RoomRentalVO> roomList() {
		return mainDao.roomList();
	}

	@Override
	public List<FleaMarketVO> fleaList() {
		return mainDao.fleaList();
	}

	@Override
	public List<CommunityVO> comList() {
		return mainDao.comList();
	}
	

}
