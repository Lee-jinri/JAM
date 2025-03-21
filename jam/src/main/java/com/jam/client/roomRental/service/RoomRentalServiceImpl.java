package com.jam.client.roomRental.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jam.client.fleaMarket.vo.FleaMarketVO;
import com.jam.client.member.service.MemberService;
import com.jam.client.roomRental.dao.RoomRentalDAO;
import com.jam.client.roomRental.vo.RoomRentalVO;

import lombok.Setter;
@Service
public class RoomRentalServiceImpl implements RoomRentalService {

	@Autowired
	private RoomRentalDAO roomDao;
	
	@Autowired
	private MemberService memberService;
	
	@Override
	public List<RoomRentalVO> getBoards(RoomRentalVO room_vo) {
		List<RoomRentalVO> list = new ArrayList<>(); 
		
		if(room_vo.getUser_id() == null) list = roomDao.getBoards(room_vo);
		else list = roomDao.getBoardsWithFavorite(room_vo);
		
		return list;
	}

	@Override
	public int listCnt(RoomRentalVO room_vo) {
		return roomDao.listCnt(room_vo);
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

	@Override
	public List<RoomRentalVO> getRoomPosts(RoomRentalVO room_vo){
		return roomDao.getRoomPosts(room_vo);
	}

	@Override
	public int getUserPostCnt(RoomRentalVO room_vo) {
		return roomDao.getUserPostCnt(room_vo);
	}

	@Override
	public String getUserId(String user_name) {
		return memberService.getUserId(user_name);
	}

	@Override
	public boolean isValidUserName(String user_name) throws Exception {
		int count = memberService.nameCheck(user_name);
		return count != 0? true : false;
	}

}
