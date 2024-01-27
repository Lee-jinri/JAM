package com.jam.client.fleaMarket.dao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.jam.client.fleaMarket.vo.FleaMarketVO;
import com.jam.config.RootConfig;

import lombok.Setter;
import lombok.extern.log4j.Log4j;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = RootConfig.class)
@Log4j
public class FleaMarketMapperTest {
	
	@Setter(onMethod_=@Autowired)
	private FleaMarketDAO fleaDao;
	
	/* 전체 글 조회 */
	@Test
	public void testFleaList() {
		FleaMarketVO flea_vo = new FleaMarketVO();
		
		log.info("중고악기 전체 글  조회");
		log.info(fleaDao.getBoards(flea_vo));
	}
	
	/* 상세 페이지 조회
	@Test
	public void testFleaDetail() {
		log.info("중고악기 상세페이지");
		log.info(fleaDao.getBoardDetail(7L));
	}
	*/
	/* 조회수 증가 
	@Test
	public void testIncrementReadCnt() {
		fleaDao.incrementReadCnt(7L);
	}*/
	
	/* 글 작성
	@Test
	public void testFleaInsert() {
		FleaMarketVO vo = new FleaMarketVO();
		
		log.info("글 작성 테스트");
		
		vo.setFlea_title("테스트");
		vo.setFlea_content("테스트");
		vo.setUser_id("abcd123");
		vo.setUser_name("김철수");
		vo.setFlea_category(0);
		vo.setPrice(1000);
		log.info(fleaDao.writeBoard(vo));
		
	}*/
	
	/* 수정할 글 정보 가져오기
	@Test
	public void testGetBoardById() {
		log.info(fleaDao.getBoardById(35L));
	}*/
	
	/* 글 수정
	@Test
	public void testFleaUpdate() {
		FleaMarketVO vo = new FleaMarketVO();
		
		log.info("글 수정");
		vo.setFlea_no(35L);
		vo.setFlea_title("수정 테스트");
		vo.setFlea_content("수정 테스트");
		vo.setPrice(2000);
		vo.setFlea_category(1);
		
		log.info(fleaDao.editBoard(vo));
	}*/ 
	
	/* 글 삭제
	@Test
	public void testFleaDelete() {
		log.info("글 삭제 " + fleaDao.boardDelete(35L));
	} */
	
}
