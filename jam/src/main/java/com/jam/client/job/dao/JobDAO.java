package com.jam.client.job.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.jam.client.job.vo.JobVO;

public interface JobDAO {

	// 구인구직 리스트
	public List<JobVO> jobList(JobVO job_vo);

	// 구인구직 페이징
	public int jobListCnt(JobVO job_vo);

	// 구인구직 조회수 증가
	public void jobReadCnt(JobVO job_vo);

	// 구인구직 글 상세페이지
	public JobVO jobDetail(JobVO job_vo);

	// 구인구직 글 입력
	public int jobInsert(JobVO job_vo);

	// 구인구직 글 수정 페이지
	public JobVO jobUpdateForm(JobVO job_vo);

	// 구인구직 글 수정
	public int jobUpdate(JobVO job_vo);

	// 구인구직 글 삭제
	public int jobDelete(JobVO job_vo);

	// 구인구직 댓글 개수 증감
	public void updateReplyCnt(@Param("job_no") int job_no, @Param("amount") int amount);

}
