package com.jam.job.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.jam.job.dto.ApplicationDto;
import com.jam.job.dto.JobDto;

import jakarta.validation.Valid;

public interface JobMapper {
	// 전체 공고 조회
	public List<JobDto> getBoard(JobDto job_vo);
	public List<JobDto> getBoardWithFavorite(JobDto job_vo);
	public int listCnt(JobDto job_vo);

	// 조회수 증가
	public void incrementReadCnt(Long post_id);

	// 공고 상세페이지
	public JobDto getPost(Long post_id);
	public JobDto getPostWithFavorite(Long post_id, @Param("user_id") String currentUserId);

	// 공고 입력
	public int writePost(JobDto job_vo);

	// 공고 수정 페이지
	public JobDto getPostById(Long post_id);

	// 공고 수정
	public int editPost(JobDto job_vo);

	// 공고 삭제
	public int deletePost(@Param("post_id") Long post_id, @Param("user_id") String user_id);
	// 지원자 수 count
	public int appCountByPostId(Long post_id);
	
	// 작성한 기업 공고/ 멤버 모집 공고/ 페이징
	public List<JobDto> getMyJobPosts(JobDto jobs);
	public List<JobDto> getMyRecruitPosts(JobDto jobs);
	public int getMyPostCnt(JobDto job_vo);

	// 공고 마감
	public int closePost(@Param("post_id") Long post_id, @Param("user_id") String user_id);
	
	// 지원
	public void createApplication(@Valid ApplicationDto app);
	// 존재하는 공고인지 확인
	public int existsJobPost(@Param("post_id") Long postId);
	// 공고 조회
	public JobDto findPostInfo(Long post_id);
		
	// 지원서 상세
	public ApplicationDto getApplication(Long applicationId);
	public int applicationsListCnt(ApplicationDto application);

	public String findCompanyIdByPostId(@Param("post_id") Long post_id);
	
	// 지원자 목록
	public List<ApplicationDto> getApplicationsByPostId(@Param("post_id") Long post_id);
	public ApplicationDto findPostInfoByAppId(Long applicationId);
	
	// 지원 내역
	public List<Map<String, Object>> getMyApplications(ApplicationDto app);
	public int getMyApplicationsCnt(ApplicationDto app);
	
	// 지원 취소
	public void withdrawApplication(@Param("application_id") Long applicationId, @Param("user_id") String userId);
	public Map<String, Object> findAppJobInfo(@Param("application_id") Long applicationId);
	
	// 스크랩
	public List<Map<String, Object>> getMyFavorites(JobDto job);
	public int getMyFavoritesCnt(JobDto job);
}
