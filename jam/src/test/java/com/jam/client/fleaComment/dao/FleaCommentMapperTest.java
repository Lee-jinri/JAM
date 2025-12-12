package com.jam.client.fleaComment.dao;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.jam.client.fleaComment.vo.FleaCommentVO;
import com.jam.config.RootConfig;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = RootConfig.class)
@Slf4j
public class FleaCommentMapperTest {

	@Setter(onMethod_=@Autowired)
	private FleaCommentDAO dao;
	
	/* 중고악기 댓글 리스트
	@Test
	public void testCommentList(){
		
		log.info("중고악기 댓글 조회");
		log.info(dao.commentList(7L));
	}*/

	/* 중고악기 댓글 입력
	@Test
	public void commentInsert() {
		FleaCommentVO vo = new FleaCommentVO();
		vo.setPost_id(7L);
		vo.setContent("댓글 입력");
		vo.setUser_id("abcd123");
		vo.setUser_name("김철수");
		
		log.info(dao.commentInsert(vo));
	}*/

	/* 중고악기 댓글 수정
	@Test
	public void commentUpdate(){
		FleaCommentVO vo = new FleaCommentVO();
		vo.setComment_id(10L);
		vo.setContent("댓글 수정");
		
		log.info(dao.commentUpdate(vo));
	}*/
		
	/* 중고악기 댓글 삭제
	@Test
	public void commentDelete() {
		log.info(dao.commentDelete(10L));
	}*/
		
	/* 중고악기 댓글의 글 번호 조회
	@Test
	public void replyRead() {
		log.info(dao.commentRead(14L));
	}*/
}
