package com.jam.client.fleaMarket.dao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.jam.client.fleaMarket.vo.FleaMarketVO;

import lombok.Setter;
import lombok.extern.log4j.Log4j;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/main/webapp/WEB-INF/spring/root-context.xml")
@Log4j
public class FleaMarketMapperTest {
	
	@Setter(onMethod_=@Autowired)
	private FleaMarketDAO fleaDao;
	
	/* 중고악기 리스트 조회
	@Test
	public void testFleaList() {
		FleaMarketVO vo = new FleaMarketVO();
		
		log.info("중고악기 리스트 조회");
		log.info(fleaDao.fleaList(vo));
	}*/
	
	/* 중고악기 상세페이지 
	@Test
	public void testFleaDetail() {
		FleaMarketVO vo = new FleaMarketVO();
		log.info("중고악기 상세페이지");
		vo.setFlea_no(1);
		log.info(fleaDao.fleaDetail(vo));
	}*/
	
	/* 중고악기 insert 
	@Test
	public void testFleaInsert() {
		FleaMarketVO vo = new FleaMarketVO();
		
		log.info("글 입력");
		
		vo.setFlea_title("?번째 글");
		vo.setFlea_content("?번째 글");
		vo.setUser_id("abcd123");
		vo.setUser_name("김철수");
		vo.setFlea_hits(0);
		vo.setFlea_reply_cnt(0);
		log.info(fleaDao.fleaInsert(vo));
		
	}*/
	
	/* 중고악기 update 
	@Test
	public void testFleaUpdate() {
		FleaMarketVO vo = new FleaMarketVO();
		
		log.info("글 수정");
		vo.setFlea_no(3);
		vo.setFlea_title("?번째 글 수정");
		vo.setFlea_content("?번째 글 수정");
		log.info(fleaDao.fleaUpdate(vo));
	}*/
	
	/* 중고악기 delete
	@Test
	public void testFleaDelete() {
		FleaMarketVO vo = new FleaMarketVO();
		vo.setFlea_no(3);
		log.info("글 삭제 " + fleaDao.fleaDelete(vo));
	}*/ 
	
}
