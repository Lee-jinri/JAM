package com.jam.client.fleaReply.dao;

import java.util.List;

import com.jam.client.fleaReply.vo.FleaReplyVO;

public interface FleaReplyDAO {

	// 중고악기 댓글 리스트
	public List<FleaReplyVO> replyList(Long flea_no);

	// 중고악기 댓글 입력
	public int replyInsert(FleaReplyVO frvo);

	// 중고악기 댓글 수정
	public int replyUpdate(FleaReplyVO frvo);
	
	// 중고악기 댓글 삭제
	public int replyDelete(Long fleaReply_no);
	
	// 중고악기 댓글의 글 번호 조회
	public FleaReplyVO replyRead(Long fleaReply_no);

}
