package com.jam.client.job.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.jam.client.job.vo.JobVO;

public interface JobDAO {

	// 구인구직 리스트
	public List<JobVO> getBoards(JobVO job_vo);

	// 구인구직 페이징
	public int listCnt(JobVO job_vo);

	// 구인구직 조회수 증가
	public void incrementReadCnt(Long job_no);

	// 구인구직 글 상세페이지
	public JobVO getBoardDetail(Long job_no);

	// 구인구직 글 입력
	public int writeBoard(JobVO job_vo);

	// 구인구직 글 수정 페이지
	public JobVO getBoardById(Long job_no);

	// 구인구직 글 수정
	public int editBoard(JobVO job_vo);

	// 구인구직 글 삭제
	public int boardDelete(Long job_no);

	// 구인구직 댓글 개수 증감
	public void updateReplyCnt(@Param("job_no") Long job_no, @Param("amount") int amount);

	public List<JobVO> getPosts(JobVO job_vo);

	public int getUserPostCnt(JobVO job_vo);

}
