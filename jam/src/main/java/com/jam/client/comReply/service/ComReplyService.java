package com.jam.client.comReply.service;

import java.util.List;

import com.jam.client.comReply.vo.ComReplyVO;

public interface ComReplyService {

	// 커뮤니티 댓글 리스트
	public List<ComReplyVO> comReplyList(Long com_no);
	
	// 커뮤니티 댓글 입력
	public int replyInsert(ComReplyVO crvo);

	// 커뮤니티 댓글 수정
	public int replyUpdate(ComReplyVO crvo);
	
	// 커뮤니티 댓글 삭제
	public int replyDelete(Long comReply_no, String user_id);


}
