package com.jam.client.roomRental.dao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.jam.client.roomRental.vo.RoomRentalVO;

import lombok.Setter;
import lombok.extern.log4j.Log4j;

@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/main/webapp/WEB-INF/spring/root-context.xml")
@Log4j
public class RoomRentalMapperTest {
	
	@Setter(onMethod_=@Autowired)
	private RoomRentalDAO roomDao;
	
	/* 리스트 조회
	@Test
	public void testRoomList() {
		RoomRentalVO room_vo = new RoomRentalVO();
	
		log.info("글 조회");
		log.info(roomDao.roomList(room_vo));
	}  */
	
	/* detail 조회
	@Test
	public void testRoomDatail() {
		RoomRentalVO room_vo = new RoomRentalVO();
		
		room_vo.setRoomRental_no(1);
		log.info("글 상세");
		log.info(roomDao.roomDetail(room_vo));
	}
	 */
	
	/* insert 
	@Test
	public void testRoomInsert() {
		RoomRentalVO room_vo = new RoomRentalVO();
		log.info("글 입력");
		room_vo.setRoomRental_title("글 입력 제목");
		room_vo.setRoomRental_content("글 입력 내용");
		room_vo.setUser_id("abcd123");
		room_vo.setUser_name("김철수");
		room_vo.setRoomRental_hits(0);
		room_vo.setRoomRental_reply_cnt(0);
		log.info(roomDao.roomInsert(room_vo));
		
	}*/
	
	/* update 
	@Test
	public void testRoomUpdate() {
		RoomRentalVO room_vo = new RoomRentalVO();
		log.info("글 수정");
		room_vo.setRoomRental_no(3);
		room_vo.setRoomRental_title("글 수정");
		room_vo.setRoomRental_content("글 수정 내용");
		log.info(roomDao.roomUpdate(room_vo));
	}*/
	
	/* delete
	@Test
	public void testRoomDelete() {
		RoomRentalVO room_vo = new RoomRentalVO();
		room_vo.setRoomRental_no(7);
		log.info("글 삭제 " + roomDao.roomDelete(room_vo));
	}*/
	
}
