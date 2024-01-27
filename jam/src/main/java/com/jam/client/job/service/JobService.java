package com.jam.client.job.service;

import java.util.List;

import com.jam.client.job.vo.JobVO;

public interface JobService {

	// 구인구직 전체 글 조회
	public List<JobVO> getBoards(JobVO job_vo);

	// 구인구직 페이징
	public int listCnt(JobVO job_vo);
	
	// 구인구직 조회수 증가
	public void incrementReadCnt(Long job_no);
		
	// 구인구직 상세페이지 조회
	public JobVO getBoardDetail(Long job_no);

	// 구인구직 글 작성
	public int writeBoard(JobVO job_vo) throws Exception;
		
	// 구인구직 수정할 글 정보 불러오기
	public JobVO getBoardById(Long job_no);
		
	// 구인구직 글 수정
	public int editBoard(JobVO job_vo);
		
	// 구인구직 글 삭제
	public int boardDelete(Long job_no);

}
