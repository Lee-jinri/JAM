package com.jam.client.favorite.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.jam.client.community.vo.CommunityVO;
import com.jam.client.favorite.vo.FavoriteVO;
import com.jam.client.fleaMarket.vo.FleaMarketVO;
import com.jam.client.job.vo.JobVO;
import com.jam.client.roomRental.vo.RoomRentalVO;

public interface FavoriteDAO {

	int addFavorite(@Param("user_id") String user_id,  @Param("board_type") String boardType, @Param("board_no") Long boardNo);
	int deleteFavorite(@Param("user_id") String user_id, @Param("board_type") String boardType, @Param("board_no") Long boardNo);
	
	List<FavoriteVO> getFavoriteCommunity(FavoriteVO favorite);
	List<FavoriteVO> getFavoriteJob(FavoriteVO favorite);
	List<FavoriteVO> getFavoriteFlea(FavoriteVO favorite);
	List<FavoriteVO> getFavoriteRoom(FavoriteVO favorite);

	int listCnt(@Param("board_type") String boardType, @Param("user_id") String userId);
	
}
