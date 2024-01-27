package com.jam.client.fleaReply.service;

import java.util.List;

import com.jam.client.fleaReply.vo.FleaReplyVO;

public interface FleaReplyService {

	// 중고거래 댓글 리스트
	public List<FleaReplyVO> fleaReplyList(Long flea_no);

	// 중고거래 댓글 입력
	public int replyInsert(FleaReplyVO frvo);

	// 댓글 수정
	public int replyUpdate(FleaReplyVO frvo);

	// 댓글 삭제
	public int replyDelete(Long fleaReply_no);

}
