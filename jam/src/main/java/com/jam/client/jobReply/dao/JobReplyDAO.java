package com.jam.client.jobReply.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.jam.client.jobReply.vo.JobReplyVO;

public interface JobReplyDAO {

	// 구인 댓글 리스트
	public List<JobReplyVO> replyList(Long job_no);

	// 구인 댓글 입력
	public int replyInsert(JobReplyVO jrvo);

	// 구인 댓글 수정
	public int replyUpdate(JobReplyVO jrvo);

	// 구인 댓글 삭제
	public int replyDelete(@Param("jobReply_no") Long jobReply_no, @Param("user_id") String user_id);

	// 구인 댓글의 글 번호 조회
	public JobReplyVO replyRead(Long jobReply_no);

	
}
