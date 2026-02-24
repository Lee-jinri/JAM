package com.jam.job.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.jam.file.dto.FileAssetDto;
import com.jam.file.dto.FileCategory;
import com.jam.global.exception.BadRequestException;
import com.jam.global.exception.ConflictException;
import com.jam.global.exception.ForbiddenException;
import com.jam.global.exception.NotFoundException;
import com.jam.global.service.FileReferenceService;
import com.jam.job.dto.ApplicationDto;
import com.jam.job.dto.JobDto;
import com.jam.job.mapper.JobMapper;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Validated
@Slf4j
public class JobService {

	private final JobMapper jobMapper;
	private final FileReferenceService fileRefService;	
	
	public List<JobDto> getBoard(JobDto job) {
		List<JobDto> list = new ArrayList<>();
		if(job.getUser_id() == null || job.getUser_id().isEmpty()) 
			list = jobMapper.getBoard(job);
		else 
			list = jobMapper.getBoardWithFavorite(job);
		
		return list;
	}

	//  페이징
	public int listCnt(JobDto job) {
		return jobMapper.listCnt(job);
	}

	//  상세페이지
	@Transactional
	public JobDto getPost(Long post_id, String currentUserId) {
		// 조회수 증가
		jobMapper.incrementReadCnt(post_id);
		
		if(currentUserId == null) return jobMapper.getPost(post_id);
		else return jobMapper.getPostWithFavorite(post_id, currentUserId);
	}

	//  글 작성
	public int writePost(JobDto job) throws Exception {
		return jobMapper.writePost(job);
	}

	//  글 수정 페이지
	public JobDto getPostById(Long post_id) {
		return jobMapper.getPostById(post_id);
	}

	//  글 수정
	public int editPost(JobDto job) {
		return jobMapper.editPost(job);
	}

	//  글 삭제
	public int deletePost(Long post_id, String user_id) {
		int applicants = jobMapper.appCountByPostId(post_id);
		if (applicants > 0) {
			throw new ConflictException("지원자가 있어 삭제할 수 없습니다. 공고를 마감 처리하세요.");
		}
		
		return jobMapper.deletePost(post_id, user_id);
	}
	
	// 공고 마감
	public int closePost(Long post_id, String user_id) {
		return jobMapper.closePost(post_id, user_id);
	}
	
	// 지원서 작성
	@Transactional(rollbackFor = Exception.class)
	public void createApplication(@Valid ApplicationDto app) {

		int count = jobMapper.existsJobPost(app.getPost_id());
	    if (count <= 0) {
	    	log.error("createApplication 실패: 존재하지 않는 공고. post_id="+ app.getPost_id());
	        throw new IllegalArgumentException("존재하지 않는 공고입니다.");
	    }
	    
		JobDto info = findPostInfo(app.getPost_id()); 
		
		if (info == null) {
			log.error("createApplication 실패: 공고 정보 조회 불가. post_id="+ app.getPost_id());
			//throw new NotFoundException("공고 정보를 찾을 수 없습니다.");
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
			    
				jobMapper.createApplication(app);
				
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
				jobMapper.createApplication(app);
				break;
			default:
				log.error("createApplication 실패: 알 수 없는 category. category=" + category +
							"post_id=" +  app.getPost_id());
				throw new BadRequestException("지원할 수 없는 공고 유형입니다.");
		}
	}

	private JobDto findPostInfo(Long post_id) {
		return jobMapper.findPostInfo(post_id);
	}

	// 작성한 기업 공고
	public List<JobDto> getMyJobPosts(JobDto jobs) {
		return jobMapper.getMyJobPosts(jobs);
	}
	
	// 작성한 멤버 모집 공고
	public List<JobDto> getMyRecruitPosts(JobDto jobs) {
		return jobMapper.getMyRecruitPosts(jobs);
	}

	// 작성한 공고 페이징
	public int getMyPostCnt(JobDto job) {
		return jobMapper.getMyPostCnt(job);
	}

	// 지원서 상세
	public Map<String, Object> getApplication(Long applicationId, String userId) {
		
		ApplicationDto info = findPostInfoByAppId(applicationId); 
		
		if (info == null) {
			log.error("getApplication 실패: 공고 정보 조회 불가. applicationId="+ applicationId);
			//throw new NotFoundException("공고 정보를 찾을 수 없습니다.");
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
		ApplicationDto app = jobMapper.getApplication(applicationId);
		
		int category = info.getCategory();
		
		switch(category) {
		case 0: 
			FileAssetDto param = new FileAssetDto();
			param.setPost_id(applicationId);
			param.setPost_type(FileCategory.APPLICATION.name());
			
			List<FileAssetDto> files = fileRefService.getFilesByPost(param);
					
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

	public String findCompanyIdByPostId(Long post_id) {
		return jobMapper.findCompanyIdByPostId(post_id);
	}
	
	// 지원자 목록
	public List<ApplicationDto> getApplicationsByPostId(Long post_id) {
		return jobMapper.getApplicationsByPostId(post_id);
	}

	// 지원자 목록 페이징
	public int applicationsListCnt(ApplicationDto application) {
		return jobMapper.applicationsListCnt(application);
	}

	private ApplicationDto findPostInfoByAppId(Long applicationId) {
		return jobMapper.findPostInfoByAppId(applicationId);
	}

	// 지원내역
	public List<Map<String, Object>> getMyApplications(ApplicationDto app) {
		return jobMapper.getMyApplications(app);
	}
	
	// 지원내역 페이징
	public int getMyApplicationsCnt(ApplicationDto app) {
		return jobMapper.getMyApplicationsCnt(app);
	} 

	// 지원 취소
	public void withdrawApplication(Long applicationId, String userId) {
		
		Map<String, Object> appJobInfo = jobMapper.findAppJobInfo(applicationId); 
		
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
		
		jobMapper.withdrawApplication(applicationId, userId);

		Object categoryObj = appJobInfo.get("JOBCATEGORY");
		if (categoryObj == null) {
			log.warn("jobCategory null: applicationId= "+ applicationId);
			return;
		}
		
		int category = ((java.math.BigDecimal) categoryObj).intValue();

		if(category == 0) {
			FileAssetDto param = new FileAssetDto();
			
			param.setPost_id(applicationId);
			param.setPost_type(FileCategory.APPLICATION.name());
			
			fileRefService.deleteFiles(param);
		}
	}
	
	public List<Map<String, Object>> getMyFavorites(JobDto job) {
		return jobMapper.getMyFavorites(job);
	}

	public int getMyFavoritesCnt(JobDto job) {
		return jobMapper.getMyFavoritesCnt(job);
	}
}