package com.jam.client.job.dao;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.apache.ibatis.annotations.Param;

import com.jam.client.job.vo.ApplicationVO;
import com.jam.client.job.vo.JobVO;

public interface JobDAO {

	// 전체 공고 조회
	public List<JobVO> getBoard(JobVO job_vo);
	public List<JobVO> getBoardWithFavorite(JobVO job_vo);
	public int listCnt(JobVO job_vo);

	// 조회수 증가
	public void incrementReadCnt(Long post_id);

	// 공고 상세페이지
	public JobVO getPost(Long post_id);

	// 공고 입력
	public int writePost(JobVO job_vo);

	// 공고 수정 페이지
	public JobVO getPostById(Long post_id);

	// 공고 수정
	public int editPost(JobVO job_vo);

	// 공고 삭제
	public int deletePost(@Param("post_id") Long post_id, @Param("user_id") String user_id);
	// 지원자 수 count
	public int appCountByPostId(Long post_id);
	
	// 작성한 기업 공고/ 멤버 모집 공고/ 페이징
	public List<JobVO> getMyJobPosts(JobVO jobs);
	public List<JobVO> getMyRecruitPosts(JobVO jobs);
	public int getMyPostCnt(JobVO job_vo);

	// 공고 마감
	public int closePost(@Param("post_id") Long post_id, @Param("user_id") String user_id);
	
	// 지원
	public void createApplication(@Valid ApplicationVO app);
	// 존재하는 공고인지 확인
	public int existsJobPost(@Param("post_id") Long postId);
	// 공고 조회
	public JobVO findPostInfo(Long post_id);
		
	// 지원서 상세
	public ApplicationVO getApplication(Long applicationId);
	public int applicationsListCnt(ApplicationVO application);
	public ApplicationVO findPostInfoByAppId(Long applicationId);
	
	// 지원 내역
	public List<Map<String, Object>> getMyApplications(ApplicationVO app);
	public int getMyApplicationsCnt(ApplicationVO app);
	
	// 지원 취소
	public void withdrawApplication(@Param("application_id") Long applicationId, @Param("user_id") String userId);
	public Map<String, Object> findAppJobInfo(@Param("application_id") Long applicationId);
	
}
