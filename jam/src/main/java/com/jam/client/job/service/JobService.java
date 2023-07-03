package com.jam.client.job.service;

import java.util.List;

import com.jam.client.job.vo.JobVO;

public interface JobService {

	// 구인구직 리스트
	List<JobVO> jobList(JobVO job_vo);

	// 구인구직 페이징
	int jobListCnt(JobVO job_vo);
	
	// 구인구직 조회수
	public void jobReadCnt(JobVO job_vo);
		
	// 구인구직 detail
	public JobVO boardDetail(JobVO job_vo);

	// 구인구직 insert
	public int jobInsert(JobVO job_vo) throws Exception;
		
	// 구인구직 update Form
	public JobVO jobUpdateForm(JobVO job_vo);
		
	// 구인구직 update
	public int jobUpdate(JobVO job_vo);
		
	// 구인구직 delete
	public int jobDelete(JobVO job_vo);

}
