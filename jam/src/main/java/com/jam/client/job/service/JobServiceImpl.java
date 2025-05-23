package com.jam.client.job.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.jam.client.job.dao.JobDAO;
import com.jam.client.job.vo.JobVO;
import com.jam.client.member.service.MemberService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {

	private final JobDAO jobDao;
	private final MemberService memberService;
	
	//  
	@Override
	public List<JobVO> getBoards(JobVO job_vo) {
		List<JobVO> list = new ArrayList<>();
		
		if(job_vo.getUser_id() == null) list = jobDao.getBoards(job_vo);
		else {
			System.out.println("jobservice : " + job_vo.getUser_id());
			list = jobDao.getBoardsWithFavorite(job_vo);
		}
		
		return list;
	}

	//  페이징
	@Override
	public int listCnt(JobVO job_vo) {
		return jobDao.listCnt(job_vo);
	}

	//  조회수 증가 
	@Override
	public void incrementReadCnt(Long job_no) {
		jobDao.incrementReadCnt(job_no);
	}

	//  상세페이지
	@Override
	public JobVO getBoardDetail(Long job_no) {
		return jobDao.getBoardDetail(job_no);
	}

	//  글 작성
	@Override
	public int writeBoard(JobVO job_vo) throws Exception {
		return jobDao.writeBoard(job_vo);
	}

	//  글 수정 페이지
	@Override
	public JobVO getBoardById(Long job_no) {
		return jobDao.getBoardById(job_no);
	}

	//  글 수정
	@Override
	public int editBoard(JobVO job_vo) {
		return jobDao.editBoard(job_vo);
	}

	//  글 삭제
	@Override
	public int boardDelete(Long job_no, String user_id) {
		return jobDao.boardDelete(job_no, user_id);
	}
	
	@Override
	public List<JobVO> getPosts(JobVO job_vo){
		return jobDao.getPosts(job_vo);
	}

	@Override
	public int getUserPostCnt(JobVO job_vo) {
		return jobDao.getUserPostCnt(job_vo);
	}

	@Override
	public boolean isValidUserName(String user_name) throws Exception {
		int count = memberService.nameCheck(user_name);
		return count != 0 ? true : false;
	}
	
	@Override
	public String getUserId(String user_name) {
		return memberService.getUserId(user_name);
	}

}
