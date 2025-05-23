package com.jam.client.roomRentalReply.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jam.client.roomRental.dao.RoomRentalDAO;
import com.jam.client.roomRentalReply.dao.RoomReplyDAO;
import com.jam.client.roomRentalReply.vo.RoomReplyVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoomReplyServiceImpl implements RoomReplyService {

	private final static int reply_Add = 1;
	private final static int reply_Del = -1;
	
	private final RoomReplyDAO roomreplyDao;
	private final RoomRentalDAO roomDao;

	@Override
	public List<RoomReplyVO> roomReplyList(Long roomRental_no) {
		List<RoomReplyVO> list = roomreplyDao.replyList(roomRental_no);
		return list;
	}

	@Transactional
	@Override
	public int replyInsert(RoomReplyVO rrvo) {
		roomDao.updateReplyCnt(rrvo.getRoomRental_no(), reply_Add);
		return roomreplyDao.replyInsert(rrvo);
	}

	@Override
	public int replyUpdate(RoomReplyVO rrvo) {
		return roomreplyDao.replyUpdate(rrvo);
	}

	@Transactional
	@Override
	public int replyDelete(Long roomReply_no) {
		
		RoomReplyVO vo = roomreplyDao.replyRead(roomReply_no);
		roomDao.updateReplyCnt(vo.getRoomRental_no(), reply_Del);
		
		return roomreplyDao.replyDelete(roomReply_no);
	}

}
