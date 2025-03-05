package com.jam.client.jobReply.dao;

import java.util.List;

import com.jam.client.jobReply.vo.JobReplyVO;

public interface JobReplyDAO {

	// 구인구직 댓글 리스트
	public List<JobReplyVO> replyList(Long job_no);

	// 구인구직 댓글 입력
	public int replyInsert(JobReplyVO jrvo);

	// 구인구직 댓글 수정
	public int replyUpdate(JobReplyVO jrvo);

	// 구인구직 댓글 삭제
	public int replyDelete(Long jobReply_no);

	// 구인구직 댓글의 글 번호 조회
	public JobReplyVO replyRead(Long jobReply_no);

	
}
