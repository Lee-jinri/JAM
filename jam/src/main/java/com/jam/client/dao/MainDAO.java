package com.jam.client.dao;

import java.util.List;

import com.jam.client.community.vo.CommunityVO;
import com.jam.client.fleaMarket.vo.FleaMarketVO;
import com.jam.client.roomRental.vo.RoomRentalVO;

public interface MainDAO {
	List<RoomRentalVO> roomList();
	List<FleaMarketVO> fleaList();
	List<CommunityVO> comList();
}
