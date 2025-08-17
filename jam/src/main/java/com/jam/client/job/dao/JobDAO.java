package com.jam.client.job.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.jam.client.job.vo.JobVO;

public interface JobDAO {

	// 리스트
	public List<JobVO> getBoard(JobVO job_vo);
	public List<JobVO> getBoardWithFavorite(JobVO job_vo);

	// 페이징
	public int listCnt(JobVO job_vo);

	// 조회수 증가
	public void incrementReadCnt(Long post_id);

	// 상세페이지
	public JobVO getPost(Long post_id);

	// 글 입력
	public int writePost(JobVO job_vo);

	// 글 수정 페이지
	public JobVO getPostById(Long post_id);

	// 글 수정
	public int editPost(JobVO job_vo);

	// 글 삭제
	public int deletePost(@Param("post_id") Long post_id, @Param("user_id") String user_id);

	public List<JobVO> getPosts(JobVO job_vo);

	public int getUserPostCnt(JobVO job_vo);

	
}
