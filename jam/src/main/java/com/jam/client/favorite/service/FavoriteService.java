package com.jam.client.favorite.service;

import java.util.List;

import com.jam.client.community.vo.CommunityVO;
import com.jam.client.favorite.vo.FavoriteVO;
import com.jam.client.fleaMarket.vo.FleaMarketVO;
import com.jam.client.job.vo.JobVO;
import com.jam.client.roomRental.vo.RoomRentalVO;

public interface FavoriteService {

	boolean addFavorite(String user_id, String boardType, Long boardNo);
	boolean deleteFavorite(String user_id, Long boardNo, String boardType);

	List<FavoriteVO> getFavoriteCommunity(FavoriteVO favorite);
	List<FavoriteVO> getFavoriteJob(FavoriteVO favorite);
	List<FavoriteVO> getFavoriteFlea(FavoriteVO favorite);
	List<FavoriteVO> getFavoriteRoom(FavoriteVO favorite);

	int listCnt(String boardType, String userId);

}
