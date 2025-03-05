package com.jam.client.roomRental.service;

import java.util.List;

import com.jam.client.roomRental.vo.RoomRentalVO;

public interface RoomRentalService {
	
	List<RoomRentalVO> getBoards(RoomRentalVO room_vo);

	int listCnt(RoomRentalVO room_vo);
			
	void incrementReadCnt(Long room_no);
				
	RoomRentalVO getBoardDetail(Long room_no);

	int writeBoard(RoomRentalVO room_vo) throws Exception;
				
	// 수정 페이지
	RoomRentalVO getBoardById(Long roomRental_no);
				
	int editBoard(RoomRentalVO room_vo);
				
	int boardDelete(Long roomRental_no);

	List<RoomRentalVO> getRoomPosts(RoomRentalVO room_vo);

	int getUserPostCnt(RoomRentalVO room_vo);

	String getUserId(String user_name) ;

	boolean isValidUserName(String user_name) throws Exception;

		
}
