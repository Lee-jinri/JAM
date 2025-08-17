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
	public List<JobVO> getBoard(JobVO job) {
		List<JobVO> list = new ArrayList<>();
		
		if(job.getUser_id() == null || job.getUser_id().isEmpty()) list = jobDao.getBoard(job);
		else {
			System.out.println("jobservice : " + job.getUser_id());
			list = jobDao.getBoardWithFavorite(job);
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
	public void incrementReadCnt(Long post_id) {
		jobDao.incrementReadCnt(post_id);
	}

	//  상세페이지
	@Override
	public JobVO getPost(Long post_id) {
		return jobDao.getPost(post_id);
	}

	//  글 작성
	@Override
	public int writePost(JobVO job_vo) throws Exception {
		return jobDao.writePost(job_vo);
	}

	//  글 수정 페이지
	@Override
	public JobVO getPostById(Long post_id) {
		return jobDao.getPostById(post_id);
	}

	//  글 수정
	@Override
	public int editPost(JobVO job_vo) {
		return jobDao.editPost(job_vo);
	}

	//  글 삭제
	@Override
	public int deletePost(Long post_id, String user_id) {
		return jobDao.deletePost(post_id, user_id);
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
