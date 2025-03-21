package com.jam.client.roomRental.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.jam.client.roomRental.vo.RoomRentalVO;

public interface RoomRentalDAO {
	
	// 합주,연습실 리스트
	public List<RoomRentalVO> getBoards(RoomRentalVO room_vo);
	
	public List<RoomRentalVO> getBoardsWithFavorite(RoomRentalVO room_vo);

	// 합주,연습실 페이징
	public int listCnt(RoomRentalVO room_vo);

	// 합주,연습실 조회수 증가
	public void incrementReadCnt(Long room_no);

	// 합주,연습실 글 상세페이지
	public RoomRentalVO getBoardDetail(Long room_no);

	// 합주,연습실 글 입력
	public int writeBoard(RoomRentalVO room_vo);

	// 합주,연습실 글 수정 페이지
	public RoomRentalVO getBoardById(Long roomRental_no);

	// 합주,연습실 글 수정
	public int editBoard(RoomRentalVO room_vo);

	// 합주,연습실 글 삭제
	public int boardDelete(Long roomRental_no);

	// 댓글 개수 증감
	public void updateReplyCnt(@Param("roomRental_no") Long roomRental_no, @Param("amount") int amount);


	public List<RoomRentalVO> getRoomPosts(RoomRentalVO room_vo);

	public int getUserPostCnt(RoomRentalVO room_vo);

}		