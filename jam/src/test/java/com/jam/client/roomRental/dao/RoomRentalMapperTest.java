package com.jam.client.roomRental.dao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.jam.client.roomRental.vo.RoomRentalVO;
import com.jam.config.RootConfig;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = RootConfig.class)
@Slf4j
public class RoomRentalMapperTest {
	
	@Setter(onMethod_=@Autowired)
	private RoomRentalDAO roomDao;
	
	/* 전체 글 조회  
	@Test
	public void testRoomList() {
		RoomRentalVO room_vo = new RoomRentalVO();
	
		log.info("글 조회");
		log.info(roomDao.getBoards(room_vo));
	} */
	
	
	/* 상세 페이지 조회
	@Test
	public void testRoomDatail() {
		
		log.info("글 상세");
		
		Long room_no = 21L;
		log.info(roomDao.getBoardDetail(room_no));
	}
	*/
	
	/* 조회수 증가 
	@Test
	public void testIncrementReadCnt() {
		roomDao.incrementReadCnt(21L);
	}*/
	
	/* 글 작성 
	@Test
	public void testRoomInsert() {
		RoomRentalVO room_vo = new RoomRentalVO();
		log.info("합주실 글 작성");
		room_vo.setRoomRental_title("작성 테스트");
		room_vo.setRoomRental_content("작성 테스트 ");
		room_vo.setUser_id("abcd123");
		room_vo.setUser_name("김철수");
		room_vo.setRoomRental_price(1000);
		
		log.info(roomDao.writeBoard(room_vo));
		
	}*/
	
	/* 수정할 글 정보 
	@Test
	public void testGetBoardById() {
		log.info(roomDao.getBoardById(23L));
	}*/
	
	
	/* 수정 
	@Test
	public void testRoomUpdate() {
		RoomRentalVO room_vo = new RoomRentalVO();
		
		room_vo.setRoomRental_no(23L);
		room_vo.setRoomRental_title("글 수정");
		room_vo.setRoomRental_content("글 수정 내용");
		room_vo.setRoomRental_status(1);
		room_vo.setRoomRental_price(2000);
		
		log.info(roomDao.editBoard(room_vo));
	}*/
	
	/* 삭제
	@Test
	public void testRoomDelete() {
		log.info(roomDao.boardDelete(23L));
	}*/
	
}
