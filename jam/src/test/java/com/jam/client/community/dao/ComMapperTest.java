package com.jam.client.community.dao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.jam.client.community.dao.ComMapperTest;
import com.jam.client.community.vo.CommunityVO;
import com.jam.config.RootConfig;

import lombok.Setter;
import lombok.extern.log4j.Log4j;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = RootConfig.class)
@Log4j
public class ComMapperTest {
	
	@Setter(onMethod_=@Autowired)
	private CommunityDAO communityDao;
	
	/* 전체 글 조회*/ 
	@Test
	public void testComList() {
		CommunityVO com_vo = new CommunityVO();
		
		log.info("커뮤니티 조회");
		log.info(communityDao.getBoards(com_vo));
	}
	
	
	/* 상세 페이지 조회 
	@Test
	public void testComDatail() {
		
		log.info("커뮤니티 글 상세페이지 조회");
		
		Long com_no = 93L;
		
		log.info(communityDao.getBoardDetail(com_no));
	}*/
	
	/* 조회수 증가 
	@Test
	public void testIncrementReadCnt() {
		Long com_no = 93L;
		
		communityDao.incrementReadCnt(com_no);
	}*/
	
	/* 글 작성 
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
		log.info(communityDao.writeBoard(com_vo));
		
	}*/
	
	/* 수정할 글 정보 조회 
	@Test
	public void testGetBoardById() {
		Long com_no = 93L;
		communityDao.getBoardById(com_no);
	}*/
	
	/* 글 수정
	@Test
	public void testComUpdate() {
		CommunityVO com_vo = new CommunityVO();
		log.info("커뮤니티 글 수정");
		com_vo.setCom_no(93L);
		com_vo.setCom_title("?번째 글 수정");
		com_vo.setCom_content("?번째 글 수정");
		log.info(communityDao.editBoard(com_vo));
	}
	*/
	
	/* 글 삭제 
	@Test
	public void testComDelete() {
		Long com_no = 93L;
		log.info("커뮤니티 글 삭제 " + communityDao.boardDelete(com_no));
	}*/
	
	/* 댓글 개수 수정 
	@Test
	public void testUpdateReplyCnt() {
		
		Long com_no = 92L;
		int amount = 1;
		
		communityDao.updateReplyCnt(com_no, amount);
	}*/
}
