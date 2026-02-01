package com.jam.client.community.dao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.jam.client.community.vo.CommunityVO;
import com.jam.config.RootConfig;
import com.jam.global.util.HtmlSanitizer;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = RootConfig.class)
@Slf4j
public class ComMapperTest {
	
	@Setter(onMethod_=@Autowired)
	private CommunityDAO communityDao;
	
	/* 전체 글 조회 
	@Test
	public void testComList() {
		CommunityVO com_vo = new CommunityVO();
		
		log.info("커뮤니티 조회");
		com_vo.setUser_id("abcd1234");
		
		// 로그인 X 즐겨찾기 글 조회 X
		//log.info(communityDao.getBoards(com_vo));
		
		// 즐겨찾기 한 글 조회
		log.info(communityDao.getBoardWithFavorite(com_vo));
	}*/
	
	
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
		com_vo.setTitle("XSS 테스트");
		com_vo.setContent("?번째 글");
		com_vo.setUser_id("abcd123");
		com_vo.setUser_name("김철수");
		communityDao.writePost(com_vo);
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
	
	@Test
	public void testHtmlInput() {
		String c = HtmlSanitizer.sanitizeHtml("<script>alert(1)</script><p>안녕</p>");
		String cc = HtmlSanitizer.sanitizeHtml("<p style=\"text-align:center;\">중앙</p>");
		log.info(cc);
	}
}
