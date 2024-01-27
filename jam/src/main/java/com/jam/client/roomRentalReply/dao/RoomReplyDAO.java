package com.jam.client.roomRentalReply.dao;

import java.util.List;

import com.jam.client.roomRentalReply.vo.RoomReplyVO;

public interface RoomReplyDAO {

	// 댓글 리스트
	List<RoomReplyVO> replyList(Long roomRental_no);

	// 댓글 입력
	int replyInsert(RoomReplyVO rrvo);

	// 댓글 수정
	int replyUpdate(RoomReplyVO rrvo);

	// 댓글 삭제
	int replyDelete(Long roomReply_no);

	// 댓글 조회
	RoomReplyVO replyRead(Long roomReply_no);

}
