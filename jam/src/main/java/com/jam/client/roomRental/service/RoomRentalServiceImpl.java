package com.jam.client.roomRental.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jam.client.roomRental.dao.RoomRentalDAO;
import com.jam.client.roomRental.vo.RoomRentalVO;

import lombok.Setter;
@Service
public class RoomRentalServiceImpl implements RoomRentalService {

	@Setter(onMethod_=@Autowired)
	private RoomRentalDAO roomDao;
	
	
	@Override
	public List<RoomRentalVO> roomList(RoomRentalVO room_vo) {
		return roomDao.roomList(room_vo);
	}

	@Override
	public int roomListCnt(RoomRentalVO room_vo) {
		return roomDao.roomListCnt(room_vo);
	}

	@Override
	public void incrementReadCnt(Long room_no) {
		roomDao.incrementReadCnt(room_no);
	}

	@Override
	public RoomRentalVO getBoardDetail(Long room_no) {
		return roomDao.getBoardDetail(room_no);
	}

	@Override
	public int writeBoard(RoomRentalVO room_vo) throws Exception {
		return roomDao.writeBoard(room_vo);
	}

	@Override
	public RoomRentalVO getBoardById(Long roomRental_no) {
		return roomDao.getBoardById(roomRental_no);
	}

	@Override
	public int editBoard(RoomRentalVO room_vo) {
		return roomDao.editBoard(room_vo);
	}

	@Override
	public int boardDelete(Long roomRental_no) {
		return roomDao.boardDelete(roomRental_no);
	}

	

}
