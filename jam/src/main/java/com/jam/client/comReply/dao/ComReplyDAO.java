package com.jam.client.comReply.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.jam.client.comReply.vo.ComReplyVO;

public interface ComReplyDAO {

	// 커뮤니티 댓글 리스트
	public List<ComReplyVO> replyList(Integer com_no);

	//커뮤니티 댓글 입력
	public int replyInsert(ComReplyVO crvo);

	// 커뮤니티 댓글 수정
	public int replyUpdate(ComReplyVO crvo);

	// 커뮤니티 댓글 삭제
	public int replyDelete(Integer comReply_no);

	// 커뮤니티 댓글의 글 번호 조회
	public ComReplyVO replyRead(Integer comReply_no);

	
}
