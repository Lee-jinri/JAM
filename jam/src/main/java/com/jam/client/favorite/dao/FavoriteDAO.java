package com.jam.client.favorite.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.jam.client.community.vo.CommunityVO;
import com.jam.client.fleaMarket.vo.FleaMarketVO;
import com.jam.client.job.vo.JobVO;
import com.jam.client.roomRental.vo.RoomRentalVO;

public interface FavoriteDAO {

	int addFavorite(@Param("user_id") String user_id,  @Param("board_type") String boardType, @Param("board_no") Long boardNo);

	int deleteFavorite(@Param("user_id") String user_id, @Param("board_type") String boardType, @Param("board_no") Long boardNo);
	List<CommunityVO> getFavoriteCommunity(String user_id);
	List<JobVO> getFavoriteJob(String user_id);
	List<FleaMarketVO> getFavoriteFlea(String user_id);
	List<RoomRentalVO> getFavoriteRoom(String user_id);
	
}
