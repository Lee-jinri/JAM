package com.jam.client.roomRental.service;

import java.util.List;

import com.jam.client.roomRental.vo.RoomRentalVO;

public interface RoomRentalService {
	
	// 레슨 리스트
	public List<RoomRentalVO> roomList(RoomRentalVO room_vo);

	// 레슨 페이징
	public int roomListCnt(RoomRentalVO room_vo);
			
	// 레슨 조회수
	public void roomReadCnt(RoomRentalVO room_vo);
				
	// 레슨 detail
	public RoomRentalVO boardDetail(RoomRentalVO room_vo);

	// 레슨 insert
	public int roomInsert(RoomRentalVO room_vo) throws Exception;
				
	// 레슨 update Form
	public RoomRentalVO roomUpdateForm(RoomRentalVO room_vo);
				
	// 레슨 update
	public int roomUpdate(RoomRentalVO room_vo);
				
	// 레슨 delete
	public int roomDelete(RoomRentalVO room_vo);	
}
