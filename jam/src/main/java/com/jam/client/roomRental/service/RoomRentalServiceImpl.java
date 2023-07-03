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
	public void roomReadCnt(RoomRentalVO room_vo) {
		roomDao.roomReadCnt(room_vo);
	}

	@Override
	public RoomRentalVO boardDetail(RoomRentalVO room_vo) {
		return roomDao.roomDetail(room_vo);
	}

	@Override
	public int roomInsert(RoomRentalVO room_vo) throws Exception {
		return roomDao.roomInsert(room_vo);
	}

	@Override
	public RoomRentalVO roomUpdateForm(RoomRentalVO room_vo) {
		return roomDao.roomUpdateForm(room_vo);
	}

	@Override
	public int roomUpdate(RoomRentalVO room_vo) {
		return roomDao.roomUpdate(room_vo);
	}

	@Override
	public int roomDelete(RoomRentalVO room_vo) {
		return roomDao.roomDelete(room_vo);
	}

}
