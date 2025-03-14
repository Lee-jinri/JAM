package com.jam.client.favorite.dao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

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
		log.info(favoriteDao.getFavoriteCommunity("abcd1234"));
	}
	
	@Test
	public void testGetFavoriteJob() {
		log.info(favoriteDao.getFavoriteJob("abcd1234"));
	}
	
	@Test
	public void testGetFavoriteFlea() {
		log.info(favoriteDao.getFavoriteFlea("abcd1234"));
	}
	
	@Test
	public void testGetFavoriteRoom() {
		log.info(favoriteDao.getFavoriteRoom("abcd1234"));
	}*/
	
	/**/
	@Test
	public void testAddFavorite() {
		log.info(favoriteDao.addFavorite("abcd1234", "job", 40L));
	}
}
