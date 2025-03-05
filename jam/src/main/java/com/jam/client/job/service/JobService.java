package com.jam.client.job.service;

import java.util.List;

import org.springframework.web.bind.annotation.RequestParam;

import com.jam.client.job.vo.JobVO;

public interface JobService {

	// 구인구직 전체 글 조회
	List<JobVO> getBoards(JobVO job_vo);

	// 구인구직 페이징
	int listCnt(JobVO job_vo);
	
	// 구인구직 조회수 증가
	void incrementReadCnt(Long job_no);
		
	// 구인구직 상세페이지 조회
	JobVO getBoardDetail(Long job_no);

	// 구인구직 글 작성
	int writeBoard(JobVO job_vo) throws Exception;
		
	// 구인구직 수정할 글 정보 불러오기
	JobVO getBoardById(Long job_no);
		
	// 구인구직 글 수정
	int editBoard(JobVO job_vo);
		
	// 구인구직 글 삭제
	int boardDelete(Long job_no);

	List<JobVO> getPosts(JobVO job_vo);

	int getUserPostCnt(JobVO job_vo);

	boolean isValidUserName(String user_name) throws Exception;
	
	String getUserId(String user_name);

}
