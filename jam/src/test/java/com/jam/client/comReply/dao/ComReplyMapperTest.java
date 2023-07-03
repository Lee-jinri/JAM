package com.jam.client.comReply.dao;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.jam.client.comReply.vo.ComReplyVO;

import lombok.Setter;
import lombok.extern.log4j.Log4j;

@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/main/webapp/WEB-INF/spring/root-context.xml")
@Log4j
public class ComReplyMapperTest {
	
	@Setter(onMethod_=@Autowired)
	private ComReplyDAO comreplyDao;
	
	/* 커뮤니티 댓글 리스트 
	@Test	
	public void testComReplyList(){
		
		log.info("커뮤니티 댓글 조회");
		log.info(comreplyDao.replyList(5));	
	}*/

	/* 커뮤니티 댓글 입력 
	@Test
	public void replyInsert() {
		ComReplyVO comreply_vo = new ComReplyVO();
		comreply_vo.setCom_no(5);
		comreply_vo.setComReply_content("댓글 입력");
		comreply_vo.setUser_id("abcd123");
		comreply_vo.setUser_name("김철수");
		
		log.info(comreplyDao.replyInsert(comreply_vo));
	}*/

	/* 커뮤니티 댓글 수정
	@Test
	public void replyUpdate() {
		ComReplyVO comreply_vo = new ComReplyVO();
		comreply_vo.setComReply_no(15);
		comreply_vo.setComReply_content("댓글 수정");
		
		log.info(comreplyDao.replyUpdate(comreply_vo));
	}*/
	 
	
	/* 커뮤니티 댓글 삭제
	@Test
	public void replyDelete() {
		
		log.info(comreplyDao.replyDelete(15));
	}
	 */
	
	/* 커뮤니티 댓글의 글 번호 조회 
	@Test
	public void replyRead() {
		log.info(comreplyDao.replyRead(30));
	}
	*/	
		
}
