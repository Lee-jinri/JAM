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
import com.jam.file.vo.FileAssetVO;
import com.jam.file.vo.FileCategory;
import com.jam.global.exception.BadRequestException;
import com.jam.global.exception.ConflictException;
import com.jam.global.exception.ForbiddenException;
import com.jam.global.exception.NotFoundException;
import com.jam.global.service.FileReferenceService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Validated
@Slf4j
public class JobServiceImpl implements JobService {

	private final JobDAO jobDao;
	private final FileReferenceService fileRefService;	
	
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

		int applicants = jobDao.appCountByPostId(post_id);
		if (applicants > 0) {
			throw new ConflictException("지원자가 있어 삭제할 수 없습니다. 공고를 마감 처리하세요.");
		}
		
		return jobDao.deletePost(post_id, user_id);
	}
	
	// 공고 마감
	@Override
	public int closePost(Long post_id, String user_id) {
		
		return jobDao.closePost(post_id, user_id);
	}
	
	// 지원서 작성
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
		app.setCompany_user_id(info.getUser_id());
		
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
					fileRefService.insertFiles(app.getFile_assets(), app.getApplication_id());
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

	// 작성한 기업 공고
	@Override
	public List<JobVO> getMyJobPosts(JobVO jobs) {
		return jobDao.getMyJobPosts(jobs);
	}
	
	// 작성한 멤버 모집 공고
	@Override
	public List<JobVO> getMyRecruitPosts(JobVO jobs) {
		return jobDao.getMyRecruitPosts(jobs);
	}

	// 작성한 공고 페이징
	@Override
	public int getMyPostCnt(JobVO job_vo) {
		return jobDao.getMyPostCnt(job_vo);
	}

	// 지원서 상세
	@Override
	public Map<String, Object> getApplication(Long applicationId, String userId) {
		
		ApplicationVO info = findPostInfoByAppId(applicationId); 
		
		if (info == null) {
			log.error("getApplication 실패: 공고 정보 조회 불가. applicationId="+ applicationId);
			throw new NotFoundException("공고 정보를 찾을 수 없습니다.");
		}
		if (info.getUser_id() == null) {
			log.error("getApplication 실패: 공고에 user_id 없음. post_id="+ info.getPost_id());
			throw new IllegalStateException("공고의 작성자 정보가 누락되었습니다.");
		}
		
		if(!info.getUser_id().equals(userId) && !info.getCompany_user_id().equals(userId)) {
			log.error("getApplication 실패: 공고 작성자 또는 지원자와 조회하는 사람의 아이디가 다름. post_id="+ info.getPost_id());
			throw new ForbiddenException("지원서를 볼 권한이 없습니다.");
		}
		
		Map<String, Object> result = new HashMap<>();
		ApplicationVO app = jobDao.getApplication(applicationId);
		
		int category = info.getCategory();
		
		switch(category) {
		case 0: 
			FileAssetVO param = new FileAssetVO();
			param.setPost_id(applicationId);
			param.setPost_type(FileCategory.APPLICATION.name());
			
			List<FileAssetVO> files = fileRefService.getFilesByPost(param);
					
			if (app == null || files == null) {
				log.error("getApplication 실패: 기업공고 지원 데이터 누락. post_id=" + applicationId);
				throw new NotFoundException("지원서를 찾을 수 없습니다.");
			}
			
			result.put("category", "COMPANY");
			result.put("files", files);
			result.put("app", app);
			
			break;
		case 1:
			if (app == null) {
				log.error("getApplication 실패: 멤버공고 지원 데이터 없음. applicationId=" + applicationId);
				throw new NotFoundException("지원서를 찾을 수 없습니다.");
			}
			result.put("category", "USER");
			result.put("app", app);	
			break;
		default:
			log.error("getApplication 실패: 알 수 없는 category. category=" + category + ", applicationId=" + applicationId);
			throw new BadRequestException("알 수 없는 공고 유형입니다.");
		}
		
		return result;
	}

	@Override
	public String findCompanyIdByPostId(Long post_id) {
		return jobDao.findCompanyIdByPostId(post_id);
	}
	
	// 지원자 목록
	@Override
	public List<ApplicationVO> getApplicationsByPostId(Long post_id) {
		return jobDao.getApplicationsByPostId(post_id);
	}

	// 지원자 목록 페이징
	@Override
	public int applicationsListCnt(ApplicationVO application) {
		return jobDao.applicationsListCnt(application);
	}

	private ApplicationVO findPostInfoByAppId(Long applicationId) {
		return jobDao.findPostInfoByAppId(applicationId);
	}

	// 지원내역
	@Override
	public List<Map<String, Object>> getMyApplications(ApplicationVO app) {
		return jobDao.getMyApplications(app);
	}
	
	// 지원내역 페이징
	@Override
	public int getMyApplicationsCnt(ApplicationVO app) {
		return jobDao.getMyApplicationsCnt(app);
	} 

	// 지원 취소
	@Override
	public void withdrawApplication(Long applicationId, String userId) {
		
		Map<String, Object> appJobInfo = jobDao.findAppJobInfo(applicationId); 
		
		if (appJobInfo == null || appJobInfo.isEmpty()) {
			log.error("appJobInfo 실패: 지원서 정보 조회 불가. applicationId="+ applicationId);
			throw new NotFoundException("지원서 정보를 찾을 수 없습니다.");
		}
		
		String applicantUserId = (String) appJobInfo.get("USERID");
		if (applicantUserId == null) {
			log.error("appJobInfo 실패: 지원서 정보에 user_id 없음. applicationId="+ applicationId);
			throw new IllegalStateException("공고의 작성자 정보가 누락되었습니다.");
		}
		
		if(!applicantUserId.equals(userId)) {
			log.error("appJobInfo 실패: 지원자와 조회하는 사람의 아이디가 다름. applicationId="+ applicationId);
			throw new ForbiddenException("지원서를 삭제할 권한이 없습니다.");
		}
		
		jobDao.withdrawApplication(applicationId, userId);

		Object categoryObj = appJobInfo.get("JOBCATEGORY");
		if (categoryObj == null) {
			log.warn("jobCategory null: applicationId= "+ applicationId);
			return;
		}
		
		int category = ((java.math.BigDecimal) categoryObj).intValue();

		if(category == 0) {
			FileAssetVO param = new FileAssetVO();
			
			param.setPost_id(applicationId);
			param.setPost_type(FileCategory.APPLICATION.name());
			
			fileRefService.deleteFiles(param);
		}
	}
	
	@Override
	public List<Map<String, Object>> getMyFavorites(JobVO job) {
		return jobDao.getMyFavorites(job);
	}

	@Override
	public int getMyFavoritesCnt(JobVO job) {
		return jobDao.getMyFavoritesCnt(job);
	}
}
