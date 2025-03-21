package com.jam.client.favorite.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jam.client.community.vo.CommunityVO;
import com.jam.client.favorite.dao.FavoriteDAO;
import com.jam.client.favorite.vo.FavoriteVO;
import com.jam.client.fleaMarket.vo.FleaMarketVO;
import com.jam.client.job.vo.JobVO;
import com.jam.client.roomRental.vo.RoomRentalVO;

@Service
public class FavoriteServiceImpl implements FavoriteService{
	@Autowired
	private FavoriteDAO favoriteDao;
	
	@Override
	public List<FavoriteVO> getFavoriteCommunity(FavoriteVO favorite) {
		return favoriteDao.getFavoriteCommunity(favorite);
	}
	
	@Override
	public List<FavoriteVO> getFavoriteJob(FavoriteVO favorite){
		return favoriteDao.getFavoriteJob(favorite);
	}
	
	@Override
	public List<FavoriteVO> getFavoriteFlea(FavoriteVO favorite){
		return favoriteDao.getFavoriteFlea(favorite);
	}
	
	@Override
	public List<FavoriteVO> getFavoriteRoom(FavoriteVO favorite){
		return favoriteDao.getFavoriteRoom(favorite);
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

	@Override
	public int listCnt(String boardType, String userId) {
		return favoriteDao.listCnt(boardType, userId);
	}
	
	

}
