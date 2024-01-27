package com.jam.client.roomRentalReply.dao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.jam.client.community.dao.ComMapperTest;
import com.jam.client.roomRentalReply.dao.RoomReplyDAO;
import com.jam.client.roomRentalReply.vo.RoomReplyVO;
import com.jam.config.RootConfig;

import lombok.Setter;
import lombok.extern.log4j.Log4j;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = RootConfig.class)
@Log4j
public class RoomReplyMapperTest {
	
	@Setter(onMethod_=@Autowired)
	private RoomReplyDAO roomRentalDao;
	
	/*  댓글 리스트
	@Test
	public void testRoomReplyList(){
		
		log.info("댓글 조회");
		log.info(roomRentalDao.replyList(3));
	}*/
	
	/* 댓글 입력
	@Test
	public void replyInsert() {
		RoomReplyVO vo = new RoomReplyVO();
		vo.setRoomRental_no(7);
		vo.setRoomReply_content("댓글 입력");
		vo.setUser_id("abcd123");
		vo.setUser_name("김철수");
		
		log.info(roomRentalDao.replyInsert(vo));
	}*/
	
	/* 댓글 수정 
	@Test
	public void replyUpdate(){
		RoomReplyVO vo = new RoomReplyVO();
		vo.setRoomReply_no(10);
		vo.setRoomReply_content("댓글 수정");
		
		log.info(roomRentalDao.replyUpdate(vo));
	}*/
	
	/* 댓글 삭제
	@Test
	public void replyDelete() {
		log.info(roomRentalDao.replyDelete(3));
	}*/
		
	/* 댓글의 글 번호 조회
	@Test
	public void replyRead() {
		log.info(roomRentalDao.replyRead(4));
	}*/
}
