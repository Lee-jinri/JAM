package com.jam.client.service;

import java.util.List;

import com.jam.client.community.vo.CommunityVO;
import com.jam.client.fleaMarket.vo.FleaMarketVO;
import com.jam.client.roomRental.vo.RoomRentalVO;

public interface MainService {

	public List<RoomRentalVO> roomList();

	public List<FleaMarketVO> fleaList();

	public List<CommunityVO> comList();

}
