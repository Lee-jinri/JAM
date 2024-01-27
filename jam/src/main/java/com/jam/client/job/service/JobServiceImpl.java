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
	public List<JobVO> getBoards(JobVO job_vo) {
		return jobDao.getBoards(job_vo);
	}

	// 구인구직 페이징
	@Override
	public int listCnt(JobVO job_vo) {
		return jobDao.listCnt(job_vo);
	}

	// 구인구직 조회수 증가 
	@Override
	public void incrementReadCnt(Long job_no) {
		jobDao.incrementReadCnt(job_no);
	}

	// 구인구직 상세페이지
	@Override
	public JobVO getBoardDetail(Long job_no) {
		return jobDao.getBoardDetail(job_no);
	}

	// 구인구직 글 작성
	@Override
	public int writeBoard(JobVO job_vo) throws Exception {
		return jobDao.writeBoard(job_vo);
	}

	// 구인구직 글 수정 페이지
	@Override
	public JobVO getBoardById(Long job_no) {
		return jobDao.getBoardById(job_no);
	}

	// 구인구직 글 수정
	@Override
	public int editBoard(JobVO job_vo) {
		return jobDao.editBoard(job_vo);
	}

	// 구인구직 글 삭제
	@Override
	public int boardDelete(Long job_no) {
		return jobDao.boardDelete(job_no);
	}

}
