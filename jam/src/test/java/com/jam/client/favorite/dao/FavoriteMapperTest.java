package com.jam.client.favorite.dao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.jam.client.favorite.vo.FavoriteVO;
import com.jam.config.RootConfig;

import lombok.Setter;
import lombok.extern.log4j.Log4j;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = RootConfig.class)
@Log4j
public class FavoriteMapperTest {
	
	@Setter(onMethod_=@Autowired)
	private FavoriteDAO favoriteDao;
	
	/*
	@Test
	public void testGetFavoriteCommunity() {
		FavoriteVO favorite = new FavoriteVO();
		
		favorite.setUser_id("abcd1234");
		favorite.setPageNum(1);
		
		log.info(favoriteDao.getFavoriteCommunity(favorite));
		log.info(favoriteDao.listCnt("community", "abcd1234"));
	}
	
	@Test
	public void testGetFavoriteJob() {
		
		
		FavoriteVO favorite = new FavoriteVO();
		
		favorite.setUser_id("abcd1234");
		favorite.setPageNum(1);
		
		log.info(favoriteDao.getFavoriteJob(favorite));
		log.info(favoriteDao.listCnt("job", "abcd1234"));
	}
	
	@Test
	public void testGetFavoriteFlea() {
		FavoriteVO favorite = new FavoriteVO();
		
		favorite.setUser_id("abcd1234");
		favorite.setPageNum(1);
		
		log.info(favoriteDao.listCnt("flaeMarket", "abcd1234"));
		
		log.info(favoriteDao.getFavoriteFlea(favorite));
	}*/
	/*
	@Test
	public void testGetFavoriteRoom() {
		
		
		FavoriteVO favorite = new FavoriteVO();
		
		favorite.setUser_id("abcd1234");
		favorite.setPageNum(1);
		
		log.info(favoriteDao.listCnt("flaeMarket", "abcd1234"));
		
		log.info(favoriteDao.getFavoriteRoom(favorite));
	}*/
	
	/*
	@Test
	public void testAddFavorite() {
		log.info(favoriteDao.addFavorite("abcd1234", "job", 40L));
	}*/
	
}
