package com.jam.client.community.dao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.jam.client.community.dao.ComMapperTest;
import com.jam.client.community.vo.CommunityVO;

import lombok.Setter;
import lombok.extern.log4j.Log4j;

@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/main/webapp/WEB-INF/spring/root-context.xml")
@Log4j
public class ComMapperTest {
	
	@Setter(onMethod_=@Autowired)
	private CommunityDAO communityDao;
	
	/* 커뮤니티 리스트 조회 
	@Test
	public void testComList() {
		CommunityVO com_vo = new CommunityVO();
		
		log.info("커뮤니티 조회");
		log.info(communityDao.communityList(com_vo));
	}*/
	
	
	/* 커뮤니티 detail 조회 
	@Test
	public void testComDatail() {
		CommunityVO com_vo = new CommunityVO();
		log.info("커뮤니티 글 상세");
		com_vo.setCom_no(5);
		log.info(communityDao.communityDetail(com_vo));
	}*/
	
	/* 커뮤니티 insert 
	@Test
	public void testComInsert() {
		CommunityVO com_vo = new CommunityVO();
		log.info("커뮤니티 글 입력");
		com_vo.setCom_title("?번째 글");
		com_vo.setCom_content("?번째 글");
		com_vo.setUser_id("abcd123");
		com_vo.setUser_name("김철수");
		com_vo.setCom_hits(0);
		com_vo.setCom_reply_cnt(0);
		log.info(communityDao.comInsert(com_vo));
		
	}*/
	
	/* 커뮤니티 update 
	@Test
	public void testComUpdate() {
		CommunityVO com_vo = new CommunityVO();
		log.info("커뮤니티 글 수정");
		com_vo.setCom_no(9);
		com_vo.setCom_title("?번째 글 수정");
		com_vo.setCom_content("?번째 글 수정");
		log.info(communityDao.comUpdate(com_vo));
	}*/
	
	/* 커뮤니티 delete
	@Test
	public void testComDelete() {
		CommunityVO com_vo = new CommunityVO();
		com_vo.setCom_no(7);
		log.info("커뮤니티 글 삭제 " + communityDao.comDelete(com_vo));
	}*/ 
}
