package com.jam.client.roomRental.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.jam.client.roomRental.vo.RoomRentalVO;

public interface RoomRentalDAO {
	
	// 합주,연습실 리스트
	public List<RoomRentalVO> roomList(RoomRentalVO room_vo);

	// 합주,연습실 페이징
	public int roomListCnt(RoomRentalVO room_vo);

	// 합주,연습실 조회수 증가
	public void roomReadCnt(RoomRentalVO room_vo);

	// 합주,연습실 글 상세페이지
	public RoomRentalVO roomDetail(RoomRentalVO room_vo);

	// 합주,연습실 글 입력
	public int roomInsert(RoomRentalVO room_vo);

	// 합주,연습실 글 수정 페이지
	public RoomRentalVO roomUpdateForm(RoomRentalVO room_vo);

	// 합주,연습실 글 수정
	public int roomUpdate(RoomRentalVO room_vo);

	// 합주,연습실 글 삭제
	public int roomDelete(RoomRentalVO room_vo);

	// 댓글 개수 증감
	public void updateReplyCnt(@Param("roomRental_no") int roomRental_no, @Param("amount") int amount);

}		