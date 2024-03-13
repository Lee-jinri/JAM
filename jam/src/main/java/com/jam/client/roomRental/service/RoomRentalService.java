package com.jam.client.roomRental.service;

import java.util.List;

import com.jam.client.roomRental.vo.RoomRentalVO;

public interface RoomRentalService {
	
	public List<RoomRentalVO> getBoards(RoomRentalVO room_vo);

	public int listCnt(RoomRentalVO room_vo);
			
	public void incrementReadCnt(Long room_no);
				
	public RoomRentalVO getBoardDetail(Long room_no);

	public int writeBoard(RoomRentalVO room_vo) throws Exception;
				
	// 수정 페이지
	public RoomRentalVO getBoardById(Long roomRental_no);
				
	public int editBoard(RoomRentalVO room_vo);
				
	public int boardDelete(Long roomRental_no);

		
}
