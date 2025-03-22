package com.jam.client.member.dao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.jam.client.member.vo.MemberBoardVO;
import com.jam.config.RootConfig;

import lombok.Setter;
import lombok.extern.log4j.Log4j;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = RootConfig.class)
@Log4j
public class MemberBoardMapperTest {
	
	@Setter(onMethod_=@Autowired)
	private MemberBoardDAO memberBoardDao;
	
	/*
	@Test
	public void testGetFavoriteCommunity() {
		MemberBoardVO favorite = new MemberBoardVO();
		
		favorite.setUser_id("abcd1234");
		favorite.setPageNum(1);
		
		log.info(memberBoardDao.getFavoriteCommunity(favorite));
		log.info(memberBoardDao.listCnt("community", "abcd1234"));
	}
	
	
	@Test
	public void testGetFavoriteJob() {
		
		
		MemberBoardVO favorite = new MemberBoardVO();
		
		favorite.setUser_id("abcd1234");
		favorite.setPageNum(1);
		
		log.info(memberBoardDao.getFavoriteJob(favorite));
		log.info(memberBoardDao.listCnt("job", "abcd1234"));
	}
	
	@Test
	public void testGetFavoriteFlea() {
		MemberBoardVO favorite = new MemberBoardVO();
		
		favorite.setUser_id("abcd1234");
		favorite.setPageNum(1);
		
		log.info(memberBoardDao.listCnt("flaeMarket", "abcd1234"));
		
		log.info(memberBoardDao.getFavoriteFlea(favorite));
	}
	@Test
	public void testGetFavoriteRoom() {
		
		
		MemberBoardVO favorite = new MemberBoardVO();
		
		favorite.setUser_id("abcd1234");
		favorite.setPageNum(1);
		
		log.info(memberBoardDao.listCnt("flaeMarket", "abcd1234"));
		
		log.info(memberBoardDao.getFavoriteRoom(favorite));
	}
	*/
	/*
	@Test
	public void testAddFavorite() {
		log.info(memberBoardDao.addFavorite("abcd1234", "job", 42L));
	}*/
	
	/** 작성한 글 조회 */
	
	@Test
	public void testGetWrittenCommunity() {
		MemberBoardVO written = new MemberBoardVO();
		
		written.setUser_id("abcd1234");
		written.setPageNum(1);
		//written.setSearch("com_title");
		//written.setKeyword("1");
		written.setBoard_type("community");
		
		log.info(memberBoardDao.getWrittenCommunity(written));
		log.info(memberBoardDao.writtenListCnt(written));
	}
	/*
	
	@Test
	public void testGetWrittenJob() {
		
		
		MemberBoardVO written = new MemberBoardVO();
		
		written.setUser_id("abcd1234");
		written.setPageNum(1);
		written.setSearch("job_title");
		written.setKeyword("1");
		written.setBoard_type("job");
		
		log.info(memberBoardDao.getFavoriteJob(written));
		log.info(memberBoardDao.writtenListCnt(written));
	}
	
	@Test
	public void testGetWrittenFlea() {
		MemberBoardVO written = new MemberBoardVO();
		
		written.setUser_id("abcd1234");
		written.setPageNum(1);
		written.setSearch("flea_title");
		written.setKeyword("1");
		written.setBoard_type("fleaMarket");
		
		
		log.info(memberBoardDao.writtenListCnt(written));
		
		log.info(memberBoardDao.getFavoriteFlea(written));
	}
	@Test
	public void testGetWrittenRoom() {
		
		
		MemberBoardVO written = new MemberBoardVO();
		
		written.setUser_id("abcd1234");
		written.setPageNum(1);
		written.setSearch("roomRental_title");
		written.setKeyword("1");
		written.setBoard_type("roomRental");
		
		log.info(memberBoardDao.writtenListCnt(written));
		
		log.info(memberBoardDao.getFavoriteRoom(written));
	}
	*/
}
