package com.jam.client.fleaReply.dao;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.jam.client.fleaReply.vo.FleaReplyVO;
import com.jam.config.RootConfig;

import lombok.Setter;
import lombok.extern.log4j.Log4j;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = RootConfig.class)
@Log4j
public class FleaReplyMapperTest {

	@Setter(onMethod_=@Autowired)
	private FleaReplyDAO fleareplyDao;
	
	/* 중고악기 댓글 리스트*/
	@Test
	public void testFleaReplyList(){
		
		log.info("중고악기 댓글 조회");
		log.info(fleareplyDao.replyList(7L));
	}

	/* 중고악기 댓글 입력
	@Test
	public void replyInsert() {
		FleaReplyVO fleareply_vo = new FleaReplyVO();
		fleareply_vo.setFlea_no(7L);
		fleareply_vo.setFleaReply_content("댓글 입력");
		fleareply_vo.setUser_id("abcd123");
		fleareply_vo.setUser_name("김철수");
		
		log.info(fleareplyDao.replyInsert(fleareply_vo));
	}*/

	/* 중고악기 댓글 수정
	@Test
	public void replyUpdate(){
		FleaReplyVO fleareply_vo = new FleaReplyVO();
		fleareply_vo.setFleaReply_no(10L);
		fleareply_vo.setFleaReply_content("댓글 수정");
		
		log.info(fleareplyDao.replyUpdate(fleareply_vo));
	}*/
		
	/* 중고악기 댓글 삭제
	@Test
	public void replyDelete() {
		log.info(fleareplyDao.replyDelete(10));
	}*/
		
	/* 중고악기 댓글의 글 번호 조회
	@Test
	public void replyRead() {
		log.info(fleareplyDao.replyRead(14));
	}*/
}
