package com.jam.client.favorite.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jam.client.community.vo.CommunityVO;
import com.jam.client.favorite.dao.FavoriteDAO;
import com.jam.client.fleaMarket.vo.FleaMarketVO;
import com.jam.client.job.vo.JobVO;
import com.jam.client.roomRental.vo.RoomRentalVO;

@Service
public class FavoriteServiceImpl implements FavoriteService{
	@Autowired
	private FavoriteDAO favoriteDao;
	
	@Override
	public List<CommunityVO> getFavoriteCommunity(String user_id) {
		return favoriteDao.getFavoriteCommunity(user_id);
	}
	
	@Override
	public List<JobVO> getFavoriteJob(String user_id){
		return favoriteDao.getFavoriteJob(user_id);
	}
	
	@Override
	public List<FleaMarketVO> getFavoriteFlea(String user_id){
		return favoriteDao.getFavoriteFlea(user_id);
	}
	
	@Override
	public List<RoomRentalVO> getFavoriteRoom(String user_id){
		return favoriteDao.getFavoriteRoom(user_id);
	}
	
	@Override
	public boolean addFavorite(String user_id, String boardType, Long boardNo) {
		int result = favoriteDao.addFavorite(user_id, boardType, boardNo);
		return result == 1;
	}

	@Override
	public boolean deleteFavorite(String user_id, Long boardNo, String boardType) {
		int result = favoriteDao.deleteFavorite(user_id, boardType, boardNo);
		return result == 1;
	}
	
	

}
