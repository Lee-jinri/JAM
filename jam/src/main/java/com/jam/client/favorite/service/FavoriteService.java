package com.jam.client.favorite.service;

import java.util.List;

import com.jam.client.community.vo.CommunityVO;
import com.jam.client.fleaMarket.vo.FleaMarketVO;
import com.jam.client.job.vo.JobVO;
import com.jam.client.roomRental.vo.RoomRentalVO;

public interface FavoriteService {

	boolean addFavorite(String user_id, String boardType, Long boardNo);

	List<CommunityVO> getFavoriteCommunity(String user_id);

	List<JobVO> getFavoriteJob(String user_id);

	List<FleaMarketVO> getFavoriteFlea(String user_id);

	List<RoomRentalVO> getFavoriteRoom(String user_id);

	boolean deleteFavorite(String user_id, Long boardNo, String boardType);

}
