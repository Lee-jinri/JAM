package com.jam.client.mypage.dao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import com.jam.client.mypage.vo.MemberBoardVO;
import com.jam.config.RootConfig;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = RootConfig.class)
@Slf4j
public class MypageMapperTest {
	
	@Setter(onMethod_=@Autowired)
	private MypageDAO mypageDao;
	
	/*
	@Test
	public void testGetFavoriteCommunity() {
		MemberBoardVO favorite = new MemberBoardVO();
		
		favorite.setUser_id("abcd1234");
		favorite.setPageNum(1);
		
		log.info(mypageDao.getFavoriteCommunity(favorite));
		log.info(mypageDao.listCnt("community", "abcd1234"));
	}
	
	
	@Test
	public void testGetFavoriteJob() {
		
		
		MemberBoardVO favorite = new MemberBoardVO();
		
		favorite.setUser_id("abcd1234");
		favorite.setPageNum(1);
		
		log.info(mypageDao.getFavoriteJob(favorite));
		log.info(mypageDao.listCnt("job", "abcd1234"));
	}
	
	@Test
	public void testGetFavoriteFlea() {
		MemberBoardVO favorite = new MemberBoardVO();
		
		favorite.setUser_id("abcd1234");
		favorite.setPageNum(1);
		
		log.info(mypageDao.listCnt("flaeMarket", "abcd1234"));
		
		log.info(mypageDao.getFavoriteFlea(favorite));
	}
	@Test
	public void testGetFavoriteRoom() {
		
		
		MemberBoardVO favorite = new MemberBoardVO();
		
		favorite.setUser_id("abcd1234");
		favorite.setPageNum(1);
		
		log.info(mypageDao.listCnt("flaeMarket", "abcd1234"));
		
		log.info(mypageDao.getFavoriteRoom(favorite));
	}
	*/
	/*
	@Test
	public void testAddFavorite() {
		log.info(mypageDao.addFavorite("abcd1234", "job", 42L));
	}*/
	
	/** 작성한 글 조회
	
	@Test
	public void testGetWrittenCommunity() {
		MemberBoardVO written = new MemberBoardVO();
		
		written.setUser_id("abcd1234");
		written.setPageNum(1);
		//written.setSearch("com_title");
		//written.setKeyword("1");
		written.setBoard_type("community");
		
		log.info(mypageDao.getMyPostCommunity(written));
		log.info(mypageDao.getMyPostCnt(written));
	} */
	/*
	
	@Test
	public void testGetWrittenJob() {
		
		
		MemberBoardVO written = new MemberBoardVO();
		
		written.setUser_id("abcd1234");
		written.setPageNum(1);
		written.setSearch("job_title");
		written.setKeyword("1");
		written.setBoard_type("job");
		
		log.info(mypageDao.getFavoriteJob(written));
		log.info(mypageDao.writtenListCnt(written));
	}
	
	@Test
	public void testGetWrittenFlea() {
		MemberBoardVO written = new MemberBoardVO();
		
		written.setUser_id("abcd1234");
		written.setPageNum(1);
		written.setSearch("flea_title");
		written.setKeyword("1");
		written.setBoard_type("fleaMarket");
		
		
		log.info(mypageDao.writtenListCnt(written));
		
		log.info(mypageDao.getFavoriteFlea(written));
	}
	@Test
	public void testGetWrittenRoom() {
		
		
		MemberBoardVO written = new MemberBoardVO();
		
		written.setUser_id("abcd1234");
		written.setPageNum(1);
		written.setSearch("roomRental_title");
		written.setKeyword("1");
		written.setBoard_type("roomRental");
		
		log.info(mypageDao.writtenListCnt(written));
		
		log.info(mypageDao.getFavoriteRoom(written));
	}
	*/
	
	/* 회원 정보
	@Test
	public void testAccount() {
		String user_id="abcd1234";
		log.info(mypageDao.account(user_id));
	}*/
}
