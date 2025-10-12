package com.jam.client.job.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.jam.client.job.dao.JobDAO;
import com.jam.client.job.vo.ApplicationVO;
import com.jam.client.job.vo.JobVO;
import com.jam.client.member.service.MemberService;
import com.jam.file.service.FileService;
import com.jam.file.vo.FileAssetVO;
import com.jam.global.exception.BadRequestException;
import com.jam.global.exception.ConflictException;
import com.jam.global.exception.ForbiddenException;
import com.jam.global.exception.NotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;

@Service
@RequiredArgsConstructor
@Validated
@Log4j
public class JobServiceImpl implements JobService {

	private final JobDAO jobDao;
	private final MemberService memberService;
	private final FileService fileService;
	
	@Override
	public List<JobVO> getBoard(JobVO job) {
		List<JobVO> list = new ArrayList<>();
		
		if(job.getUser_id() == null || job.getUser_id().isEmpty()) 
			list = jobDao.getBoard(job);
		else 
			list = jobDao.getBoardWithFavorite(job);
		
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
	public List<JobVO> getPosts(JobVO job_vo){
		return jobDao.getPosts(job_vo);
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

	@Override
	public List<JobVO> getMyJobPosts(JobVO jobs) {
		return jobDao.getMyJobPosts(jobs);
	}
	
	@Override
	public List<JobVO> getMyRecruitPosts(JobVO jobs) {
		return jobDao.getMyRecruitPosts(jobs);
	}

	@Override
	public int getMyPostCnt(JobVO job_vo) {
		return jobDao.getMyPostCnt(job_vo);
	}
	
	@Override
	public String findCompanyIdByPostId(Long post_id) {
		return jobDao.findCompanyIdByPostId(post_id);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void createApplication(@Valid ApplicationVO app) {

		int count = jobDao.existsJobPost(app.getPost_id());
	    if (count <= 0) {
	    	log.error("createApplication 실패: 존재하지 않는 공고. post_id="+ app.getPost_id());
	        throw new IllegalArgumentException("존재하지 않는 공고입니다.");
	    }
	    
		JobVO info = findPostInfo(app.getPost_id()); 
		
		if (info == null) {
			log.error("createApplication 실패: 공고 정보 조회 불가. post_id="+ app.getPost_id());
			throw new NotFoundException("공고 정보를 찾을 수 없습니다.");
		}
		if (info.getUser_id() == null) {
			log.error("createApplication 실패: 공고에 user_id 없음. post_id="+ app.getPost_id());
			throw new IllegalStateException("공고의 작성자 정보가 누락되었습니다.");
		}
		app.setCompany_id(info.getUser_id());
		
		if (app.getUser_id() != null && app.getUser_id().equals(info.getUser_id())) {
			log.error("createApplication 실패: 본인이 등록한 공고에 지원. post_id="+ app.getPost_id());
			throw new BadRequestException("본인이 등록한 공고에는 지원할 수 없습니다.");
		}
		
		int category = info.getCategory();
		
		switch(category) {
			case 0: 
				if(app.getFile_assets() == null) {
					log.error("createApplication 실패: 기업 공고 지원 파일 없음. user_id=" + app.getUser_id()
								+ "post_id=" + app.getPost_id());
			    	throw new BadRequestException("기업 공고 지원은 이력서 파일이 1개 이상 필요합니다.");
			    }
			    
				jobDao.createApplication(app);
				
				if (app.getApplication_id() == null) {
					log.error("createApplication 실패: application_id 생성 안 됨. user_id=" + app.getUser_id() +
							"post_id=" + app.getPost_id());
					throw new IllegalStateException("application_id not generated.");
				}
				
				try {
					fileService.insertFiles(app.getFile_assets(), app.getApplication_id());
				} catch (Exception e) {
					log.error("createApplication 실패: 파일 연결 중 오류. application_id=" + app.getApplication_id() +
								"error=" +  e.getMessage());
					throw e; 
				}
				break;
			case 1:
				jobDao.createApplication(app);
				break;
			default:
				log.error("createApplication 실패: 알 수 없는 category. category=" + category +
							"post_id=" +  app.getPost_id());
				throw new BadRequestException("지원할 수 없는 공고 유형입니다.");
		}
	}

	private JobVO findPostInfo(Long post_id) {
		return jobDao.findPostInfo(post_id);
	}

	

}
