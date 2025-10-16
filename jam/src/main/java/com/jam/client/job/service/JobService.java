package com.jam.client.job.service;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import com.jam.client.job.vo.ApplicationVO;
import com.jam.client.job.vo.JobVO;

public interface JobService {

	// 구인구직 전체 글 조회
	List<JobVO> getBoard(JobVO job_vo);

	// 구인구직 페이징
	int listCnt(JobVO job_vo);
	
	// 구인구직 조회수 증가
	void incrementReadCnt(Long post_id);
		
	// 구인구직 상세페이지 조회
	JobVO getPost(Long post_id);

	// 구인구직 글 작성
	int writePost(JobVO job_vo) throws Exception;
		
	// 구인구직 수정할 글 정보 불러오기
	JobVO getPostById(Long post_id);
		
	// 구인구직 글 수정
	int editPost(JobVO job_vo);
		
	// 구인구직 글 삭제
	int deletePost(Long post_id, String user_id);

	// 작성한 기업 공고/ 멤버 모집 공고/ 페이징
	List<JobVO> getMyRecruitPosts(JobVO jobs);
	List<JobVO> getMyJobPosts(JobVO jobs);
	int getMyPostCnt(JobVO job_vo);

	// 공고 마감
	int closePost(Long post_id, String user_id);
	
	// 지원
	void createApplication(@Valid ApplicationVO app);

	// 지원서 상세
	Map<String, Object> getApplication(Long applicationId, String userId);
	int applicationsListCnt(ApplicationVO application);
	
	// 지원 내역
	List<Map<String, Object>> getMyApplications(ApplicationVO app);
	int getMyApplicationsCnt(ApplicationVO app);

}
