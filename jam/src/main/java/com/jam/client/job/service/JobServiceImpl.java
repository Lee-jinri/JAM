package com.jam.client.job.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jam.client.job.dao.JobDAO;
import com.jam.client.job.vo.JobVO;

import lombok.Setter;

@Service
public class JobServiceImpl implements JobService {

	@Setter(onMethod_=@Autowired)
	private JobDAO jobDao;
	
	// 구인구직 
	@Override
	public List<JobVO> jobList(JobVO job_vo) {
		return jobDao.jobList(job_vo);
	}

	// 구인구직 페이징
	@Override
	public int jobListCnt(JobVO job_vo) {
		return jobDao.jobListCnt(job_vo);
	}

	// 구인구직 조회수 증가 
	@Override
	public void jobReadCnt(JobVO job_vo) {
		jobDao.jobReadCnt(job_vo);
	}

	// 구인구직 상세페이지
	@Override
	public JobVO boardDetail(JobVO job_vo) {
		return jobDao.jobDetail(job_vo);
	}

	// 구인구직 글 작성
	@Override
	public int jobInsert(JobVO job_vo) throws Exception {
		return jobDao.jobInsert(job_vo);
	}

	// 구인구직 글 수정 페이지
	@Override
	public JobVO jobUpdateForm(JobVO job_vo) {
		return jobDao.jobUpdateForm(job_vo);
	}

	// 구인구직 글 수정
	@Override
	public int jobUpdate(JobVO job_vo) {
		return jobDao.jobUpdate(job_vo);
	}

	// 구인구직 글 삭제
	@Override
	public int jobDelete(JobVO job_vo) {
		return jobDao.jobDelete(job_vo);
	}

}
