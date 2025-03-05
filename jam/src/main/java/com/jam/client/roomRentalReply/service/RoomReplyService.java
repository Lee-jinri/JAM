package com.jam.client.roomRentalReply.service;

import java.util.List;

import com.jam.client.roomRentalReply.vo.RoomReplyVO;

public interface RoomReplyService {
	
	// 댓글 리스트
	List<RoomReplyVO> roomReplyList(Long roomRental_no);

	// 댓글 입력
	int replyInsert(RoomReplyVO rrvo);

	// 댓글 수정
	int replyUpdate(RoomReplyVO rrvo);

	// 댓글 삭제
	int replyDelete(Long roomReply_no);

}
