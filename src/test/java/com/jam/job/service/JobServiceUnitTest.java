package com.jam.job.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.jam.file.dto.FileAssetDto;
import com.jam.global.exception.BadRequestException;
import com.jam.global.exception.ConflictException;
import com.jam.global.exception.ForbiddenException;
import com.jam.global.exception.NotFoundException;
import com.jam.global.service.FileReferenceService;
import com.jam.job.dto.ApplicationDto;
import com.jam.job.dto.JobDto;
import com.jam.job.mapper.JobMapper;

/**
 * JobService에 대한 Mockito 단위 테스트.
 * 단순 mapper 위임 메서드는 위임 확인용으로만 작성했고, XML에 있는 실제 SQL(검색/즐겨찾기 join 등)은
 * 이 유닛테스트로 검증되지 않는다.
 */
@ExtendWith(MockitoExtension.class)
class JobServiceUnitTest {

	@Mock
	private JobMapper jobMapper;
	@Mock
	private FileReferenceService fileRefService;

	@InjectMocks
	private JobService jobService;

	@Nested
	@DisplayName("getBoard")
	class GetBoard {

		@Test
		@DisplayName("user_id가 null이면 getBoard로 조회한다")
		void getBoard_nullUserId_callsGetBoard() {
			JobDto job = new JobDto();
			List<JobDto> mockResult = List.of(new JobDto());
			given(jobMapper.getBoard(job)).willReturn(mockResult);

			List<JobDto> result = jobService.getBoard(job);

			assertThat(result).isEqualTo(mockResult);
			verify(jobMapper, never()).getBoardWithFavorite(any());
		}

		@Test
		@DisplayName("user_id가 빈 문자열이면 getBoard로 조회한다")
		void getBoard_emptyUserId_callsGetBoard() {
			JobDto job = new JobDto();
			job.setUser_id("");
			given(jobMapper.getBoard(job)).willReturn(List.of());

			jobService.getBoard(job);

			verify(jobMapper).getBoard(job);
			verify(jobMapper, never()).getBoardWithFavorite(any());
		}

		@Test
		@DisplayName("user_id가 있으면 getBoardWithFavorite로 조회한다")
		void getBoard_withUserId_callsGetBoardWithFavorite() {
			JobDto job = new JobDto();
			job.setUser_id("user1");
			List<JobDto> mockResult = List.of(new JobDto());
			given(jobMapper.getBoardWithFavorite(job)).willReturn(mockResult);

			List<JobDto> result = jobService.getBoard(job);

			assertThat(result).isEqualTo(mockResult);
			verify(jobMapper, never()).getBoard(any());
		}
	}

	@Nested
	@DisplayName("getPost")
	class GetPost {

		@Test
		@DisplayName("항상 조회수를 증가시킨다")
		void getPost_alwaysIncrementsReadCnt() {
			jobService.getPost(1L, null);

			verify(jobMapper).incrementReadCnt(1L);
		}

		@Test
		@DisplayName("currentUserId가 없으면 getPost로 조회한다")
		void getPost_noUserId_callsGetPost() {
			JobDto mockResult = new JobDto();
			given(jobMapper.getPost(1L)).willReturn(mockResult);

			JobDto result = jobService.getPost(1L, null);

			assertThat(result).isEqualTo(mockResult);
			verify(jobMapper, never()).getPostWithFavorite(any(), any());
		}

		@Test
		@DisplayName("currentUserId가 있으면 getPostWithFavorite로 조회한다")
		void getPost_withUserId_callsGetPostWithFavorite() {
			JobDto mockResult = new JobDto();
			given(jobMapper.getPostWithFavorite(1L, "user1")).willReturn(mockResult);

			JobDto result = jobService.getPost(1L, "user1");

			assertThat(result).isEqualTo(mockResult);
			verify(jobMapper, never()).getPost(any());
		}
	}

	@Nested
	@DisplayName("deletePost")
	class DeletePost {

		@Test
		@DisplayName("지원자가 있으면 ConflictException을 던지고 삭제하지 않는다")
		void deletePost_hasApplicants_throwsConflict() {
			given(jobMapper.appCountByPostId(1L)).willReturn(2);

			assertThatThrownBy(() -> jobService.deletePost(1L, "user1"))
					.isInstanceOf(ConflictException.class);

			verify(jobMapper, never()).deletePost(any(), any());
		}

		@Test
		@DisplayName("지원자가 없으면 삭제한다")
		void deletePost_noApplicants_deletes() {
			given(jobMapper.appCountByPostId(1L)).willReturn(0);
			given(jobMapper.deletePost(1L, "user1")).willReturn(1);

			int result = jobService.deletePost(1L, "user1");

			assertThat(result).isEqualTo(1);
			verify(jobMapper).deletePost(1L, "user1");
		}
	}

	@Nested
	@DisplayName("createApplication")
	class CreateApplication {

		private ApplicationDto app;

		@BeforeEach
		void setUp() {
			app = new ApplicationDto();
			app.setPost_id(1L);
			app.setUser_id("applicant1");
			app.setTitle("지원합니다");
		}

		@Test
		@DisplayName("존재하지 않는 공고면 NotFoundException을 던진다")
		void createApplication_postNotExists_throwsNotFound() {
			given(jobMapper.existsJobPost(1L)).willReturn(0);

			assertThatThrownBy(() -> jobService.createApplication(app))
					.isInstanceOf(NotFoundException.class);

			verify(jobMapper, never()).createApplication(any());
		}

		@Test
		@DisplayName("공고 정보를 찾을 수 없으면 NotFoundException을 던진다")
		void createApplication_infoNull_throwsNotFound() {
			given(jobMapper.existsJobPost(1L)).willReturn(1);
			given(jobMapper.findPostInfo(1L)).willReturn(null);

			assertThatThrownBy(() -> jobService.createApplication(app))
					.isInstanceOf(NotFoundException.class);

			verify(jobMapper, never()).createApplication(any());
		}

		@Test
		@DisplayName("공고에 작성자 정보가 없으면 IllegalStateException을 던진다")
		void createApplication_infoUserIdNull_throwsIllegalState() {
			given(jobMapper.existsJobPost(1L)).willReturn(1);
			given(jobMapper.findPostInfo(1L)).willReturn(new JobDto());

			assertThatThrownBy(() -> jobService.createApplication(app))
					.isInstanceOf(IllegalStateException.class);
		}

		@Test
		@DisplayName("본인이 등록한 공고에는 지원할 수 없다")
		void createApplication_selfApply_throwsBadRequest() {
			JobDto info = new JobDto();
			info.setUser_id("applicant1");
			info.setCategory(1);
			given(jobMapper.existsJobPost(1L)).willReturn(1);
			given(jobMapper.findPostInfo(1L)).willReturn(info);

			assertThatThrownBy(() -> jobService.createApplication(app))
					.isInstanceOf(BadRequestException.class);

			verify(jobMapper, never()).createApplication(any());
		}

		@Test
		@DisplayName("기업 공고(category 0)인데 첨부파일이 없으면 BadRequestException을 던진다")
		void createApplication_category0_noFiles_throwsBadRequest() {
			JobDto info = new JobDto();
			info.setUser_id("company1");
			info.setCategory(0);
			given(jobMapper.existsJobPost(1L)).willReturn(1);
			given(jobMapper.findPostInfo(1L)).willReturn(info);

			assertThatThrownBy(() -> jobService.createApplication(app))
					.isInstanceOf(BadRequestException.class);

			verify(jobMapper, never()).createApplication(any());
		}

		@Test
		@DisplayName("기업 공고(category 0) 성공 시 company_user_id를 세팅하고 파일을 연결한다")
		void createApplication_category0_success() {
			JobDto info = new JobDto();
			info.setUser_id("company1");
			info.setCategory(0);
			app.setFile_assets(List.of(new FileAssetDto()));
			given(jobMapper.existsJobPost(1L)).willReturn(1);
			given(jobMapper.findPostInfo(1L)).willReturn(info);
			doAnswer(inv -> {
				ApplicationDto arg = inv.getArgument(0);
				arg.setApplication_id(100L);
				return null;
			}).when(jobMapper).createApplication(app);

			jobService.createApplication(app);

			assertThat(app.getCompany_user_id()).isEqualTo("company1");
			verify(fileRefService).insertFiles(app.getFile_assets(), 100L);
		}

		@Test
		@DisplayName("기업 공고(category 0)에서 application_id가 생성되지 않으면 IllegalStateException을 던지고 파일을 연결하지 않는다")
		void createApplication_category0_applicationIdNotGenerated_throwsIllegalState() {
			JobDto info = new JobDto();
			info.setUser_id("company1");
			info.setCategory(0);
			app.setFile_assets(List.of(new FileAssetDto()));
			given(jobMapper.existsJobPost(1L)).willReturn(1);
			given(jobMapper.findPostInfo(1L)).willReturn(info);
			// createApplication mock 호출은 application_id를 세팅하지 않음(기본 동작)

			assertThatThrownBy(() -> jobService.createApplication(app))
					.isInstanceOf(IllegalStateException.class);

			verify(fileRefService, never()).insertFiles(any(), any());
		}

		@Test
		@DisplayName("기업 공고(category 0)에서 파일 연결 중 예외가 나면 그대로 전파된다")
		void createApplication_category0_insertFilesThrows_propagates() {
			JobDto info = new JobDto();
			info.setUser_id("company1");
			info.setCategory(0);
			app.setFile_assets(List.of(new FileAssetDto()));
			given(jobMapper.existsJobPost(1L)).willReturn(1);
			given(jobMapper.findPostInfo(1L)).willReturn(info);
			doAnswer(inv -> {
				ApplicationDto arg = inv.getArgument(0);
				arg.setApplication_id(100L);
				return null;
			}).when(jobMapper).createApplication(app);
			doThrow(new RuntimeException("파일 연결 실패")).when(fileRefService).insertFiles(any(), eq(100L));

			assertThatThrownBy(() -> jobService.createApplication(app))
					.isInstanceOf(RuntimeException.class)
					.hasMessage("파일 연결 실패");

			verify(jobMapper).createApplication(app);
		}

		@Test
		@DisplayName("멤버 모집(category 1)은 첨부파일 없이도 저장되고 파일 연결을 호출하지 않는다")
		void createApplication_category1_success_noFileCheck() {
			JobDto info = new JobDto();
			info.setUser_id("recruiter1");
			info.setCategory(1);
			given(jobMapper.existsJobPost(1L)).willReturn(1);
			given(jobMapper.findPostInfo(1L)).willReturn(info);

			jobService.createApplication(app);

			verify(jobMapper).createApplication(app);
			verify(fileRefService, never()).insertFiles(any(), any());
		}

		@Test
		@DisplayName("알 수 없는 category면 BadRequestException을 던진다")
		void createApplication_unknownCategory_throwsBadRequest() {
			JobDto info = new JobDto();
			info.setUser_id("someone");
			info.setCategory(2);
			given(jobMapper.existsJobPost(1L)).willReturn(1);
			given(jobMapper.findPostInfo(1L)).willReturn(info);

			assertThatThrownBy(() -> jobService.createApplication(app))
					.isInstanceOf(BadRequestException.class);

			verify(jobMapper, never()).createApplication(any());
		}
	}

	@Nested
	@DisplayName("getMyPosts / getMyPostCnt")
	class GetMyPosts {

		@Test
		@DisplayName("ROLE_COMPANY면 getMyJobPosts로 조회한다")
		void getMyPosts_roleCompany_callsGetMyJobPosts() {
			JobDto job = new JobDto();
			List<JobDto> mockResult = List.of(new JobDto());
			given(jobMapper.getMyJobPosts(job)).willReturn(mockResult);

			List<JobDto> result = jobService.getMyPosts(job, Set.of("ROLE_COMPANY"));

			assertThat(result).isEqualTo(mockResult);
			verify(jobMapper, never()).getMyRecruitPosts(any());
		}

		@Test
		@DisplayName("ROLE_USER면 getMyRecruitPosts로 조회한다")
		void getMyPosts_roleUser_callsGetMyRecruitPosts() {
			JobDto job = new JobDto();
			List<JobDto> mockResult = List.of(new JobDto());
			given(jobMapper.getMyRecruitPosts(job)).willReturn(mockResult);

			List<JobDto> result = jobService.getMyPosts(job, Set.of("ROLE_USER"));

			assertThat(result).isEqualTo(mockResult);
			verify(jobMapper, never()).getMyJobPosts(any());
		}

		@Test
		@DisplayName("둘 다 아니면 빈 리스트를 반환하고 mapper를 호출하지 않는다")
		void getMyPosts_noRole_returnsEmpty() {
			JobDto job = new JobDto();

			List<JobDto> result = jobService.getMyPosts(job, Set.of("ROLE_ADMIN"));

			assertThat(result).isEmpty();
			verify(jobMapper, never()).getMyJobPosts(any());
			verify(jobMapper, never()).getMyRecruitPosts(any());
		}

		@Test
		@DisplayName("ROLE_COMPANY면 category=0으로 설정 후 카운트를 조회한다")
		void getMyPostCnt_roleCompany_setsCategory0() {
			JobDto job = new JobDto();
			given(jobMapper.getMyPostCnt(job)).willReturn(5);

			int result = jobService.getMyPostCnt(job, Set.of("ROLE_COMPANY"));

			assertThat(result).isEqualTo(5);
			assertThat(job.getCategory()).isEqualTo(0);
		}

		@Test
		@DisplayName("ROLE_USER면 category=1로 설정 후 카운트를 조회한다")
		void getMyPostCnt_roleUser_setsCategory1() {
			JobDto job = new JobDto();
			given(jobMapper.getMyPostCnt(job)).willReturn(3);

			int result = jobService.getMyPostCnt(job, Set.of("ROLE_USER"));

			assertThat(result).isEqualTo(3);
			assertThat(job.getCategory()).isEqualTo(1);
		}

		@Test
		@DisplayName("둘 다 아니면 0을 반환하고 mapper를 호출하지 않는다")
		void getMyPostCnt_noRole_returnsZero() {
			JobDto job = new JobDto();

			int result = jobService.getMyPostCnt(job, Set.of("ROLE_ADMIN"));

			assertThat(result).isEqualTo(0);
			verify(jobMapper, never()).getMyPostCnt(any());
		}
	}

	@Nested
	@DisplayName("getApplication")
	class GetApplication {

		@Test
		@DisplayName("지원서 정보를 찾을 수 없으면 NotFoundException을 던진다")
		void getApplication_infoNull_throwsNotFound() {
			given(jobMapper.findPostInfoByAppId(1L)).willReturn(null);

			assertThatThrownBy(() -> jobService.getApplication(1L, "user1"))
					.isInstanceOf(NotFoundException.class);
		}

		@Test
		@DisplayName("공고에 작성자 정보가 없으면 IllegalStateException을 던진다")
		void getApplication_infoUserIdNull_throwsIllegalState() {
			given(jobMapper.findPostInfoByAppId(1L)).willReturn(new ApplicationDto());

			assertThatThrownBy(() -> jobService.getApplication(1L, "user1"))
					.isInstanceOf(IllegalStateException.class);
		}

		@Test
		@DisplayName("공고 작성자도 지원자 본인도 아니면 ForbiddenException을 던진다")
		void getApplication_notOwnerNorApplicant_throwsForbidden() {
			ApplicationDto info = new ApplicationDto();
			info.setUser_id("applicant1");
			info.setCompany_user_id("company1");
			given(jobMapper.findPostInfoByAppId(1L)).willReturn(info);

			assertThatThrownBy(() -> jobService.getApplication(1L, "intruder"))
					.isInstanceOf(ForbiddenException.class);
		}

		@Test
		@DisplayName("기업 공고(category 0)인데 지원서나 파일을 찾을 수 없으면 NotFoundException을 던진다")
		void getApplication_category0_appOrFilesNull_throwsNotFound() {
			ApplicationDto info = new ApplicationDto();
			info.setUser_id("applicant1");
			info.setCompany_user_id("company1");
			info.setCategory(0);
			given(jobMapper.findPostInfoByAppId(1L)).willReturn(info);
			given(jobMapper.getApplication(1L)).willReturn(null);
			given(fileRefService.getFilesByPost(any(FileAssetDto.class))).willReturn(List.of());

			assertThatThrownBy(() -> jobService.getApplication(1L, "applicant1"))
					.isInstanceOf(NotFoundException.class);
		}

		@Test
		@DisplayName("기업 공고(category 0) 성공 시 category=COMPANY와 files/app을 반환한다")
		void getApplication_category0_success() {
			ApplicationDto info = new ApplicationDto();
			info.setUser_id("applicant1");
			info.setCompany_user_id("company1");
			info.setCategory(0);
			ApplicationDto appDetail = new ApplicationDto();
			List<FileAssetDto> files = List.of(new FileAssetDto());
			given(jobMapper.findPostInfoByAppId(1L)).willReturn(info);
			given(jobMapper.getApplication(1L)).willReturn(appDetail);
			given(fileRefService.getFilesByPost(any(FileAssetDto.class))).willReturn(files);

			Map<String, Object> result = jobService.getApplication(1L, "company1");

			assertThat(result.get("category")).isEqualTo("COMPANY");
			assertThat(result.get("app")).isEqualTo(appDetail);
			assertThat(result.get("files")).isEqualTo(files);

			ArgumentCaptor<FileAssetDto> captor = ArgumentCaptor.forClass(FileAssetDto.class);
			verify(fileRefService).getFilesByPost(captor.capture());
			assertThat(captor.getValue().getPost_id()).isEqualTo(1L);
			assertThat(captor.getValue().getPost_type()).isEqualTo("APPLICATION");
		}

		@Test
		@DisplayName("멤버 모집(category 1)인데 지원서를 찾을 수 없으면 NotFoundException을 던진다")
		void getApplication_category1_appNull_throwsNotFound() {
			ApplicationDto info = new ApplicationDto();
			info.setUser_id("applicant1");
			info.setCompany_user_id("recruiter1");
			info.setCategory(1);
			given(jobMapper.findPostInfoByAppId(1L)).willReturn(info);
			given(jobMapper.getApplication(1L)).willReturn(null);

			assertThatThrownBy(() -> jobService.getApplication(1L, "applicant1"))
					.isInstanceOf(NotFoundException.class);
		}

		@Test
		@DisplayName("멤버 모집(category 1) 성공 시 category=USER와 app을 반환한다")
		void getApplication_category1_success() {
			ApplicationDto info = new ApplicationDto();
			info.setUser_id("applicant1");
			info.setCompany_user_id("recruiter1");
			info.setCategory(1);
			ApplicationDto appDetail = new ApplicationDto();
			given(jobMapper.findPostInfoByAppId(1L)).willReturn(info);
			given(jobMapper.getApplication(1L)).willReturn(appDetail);

			Map<String, Object> result = jobService.getApplication(1L, "applicant1");

			assertThat(result.get("category")).isEqualTo("USER");
			assertThat(result.get("app")).isEqualTo(appDetail);
			verify(fileRefService, never()).getFilesByPost(any());
		}

		@Test
		@DisplayName("알 수 없는 category면 BadRequestException을 던진다")
		void getApplication_unknownCategory_throwsBadRequest() {
			ApplicationDto info = new ApplicationDto();
			info.setUser_id("applicant1");
			info.setCompany_user_id("company1");
			info.setCategory(9);
			given(jobMapper.findPostInfoByAppId(1L)).willReturn(info);
			given(jobMapper.getApplication(1L)).willReturn(new ApplicationDto());

			assertThatThrownBy(() -> jobService.getApplication(1L, "applicant1"))
					.isInstanceOf(BadRequestException.class);
		}
	}

	@Nested
	@DisplayName("withdrawApplication")
	class WithdrawApplication {

		@Test
		@DisplayName("지원 정보를 찾을 수 없으면(null) NotFoundException을 던진다")
		void withdrawApplication_infoNull_throwsNotFound() {
			given(jobMapper.findAppJobInfo(1L)).willReturn(null);

			assertThatThrownBy(() -> jobService.withdrawApplication(1L, "user1"))
					.isInstanceOf(NotFoundException.class);

			verify(jobMapper, never()).withdrawApplication(any(), any());
		}

		@Test
		@DisplayName("지원 정보가 빈 맵이면 NotFoundException을 던진다")
		void withdrawApplication_infoEmpty_throwsNotFound() {
			given(jobMapper.findAppJobInfo(1L)).willReturn(new HashMap<>());

			assertThatThrownBy(() -> jobService.withdrawApplication(1L, "user1"))
					.isInstanceOf(NotFoundException.class);
		}

		@Test
		@DisplayName("USERID가 없으면 IllegalStateException을 던진다")
		void withdrawApplication_userIdNull_throwsIllegalState() {
			Map<String, Object> info = new HashMap<>();
			info.put("JOBCATEGORY", BigDecimal.ZERO);
			given(jobMapper.findAppJobInfo(1L)).willReturn(info);

			assertThatThrownBy(() -> jobService.withdrawApplication(1L, "user1"))
					.isInstanceOf(IllegalStateException.class);
		}

		@Test
		@DisplayName("지원자 본인이 아니면 ForbiddenException을 던진다")
		void withdrawApplication_notApplicant_throwsForbidden() {
			Map<String, Object> info = new HashMap<>();
			info.put("USERID", "applicant1");
			given(jobMapper.findAppJobInfo(1L)).willReturn(info);

			assertThatThrownBy(() -> jobService.withdrawApplication(1L, "intruder"))
					.isInstanceOf(ForbiddenException.class);

			verify(jobMapper, never()).withdrawApplication(any(), any());
		}

		@Test
		@DisplayName("JOBCATEGORY가 없으면 삭제 후 파일 연결 삭제 없이 종료한다")
		void withdrawApplication_categoryNull_deletesWithoutFiles() {
			Map<String, Object> info = new HashMap<>();
			info.put("USERID", "user1");
			given(jobMapper.findAppJobInfo(1L)).willReturn(info);

			jobService.withdrawApplication(1L, "user1");

			verify(jobMapper).withdrawApplication(1L, "user1");
			verify(fileRefService, never()).deleteFiles(any());
		}

		@Test
		@DisplayName("category가 0(기업)이면 첨부파일도 함께 삭제한다")
		void withdrawApplication_category0_deletesFiles() {
			Map<String, Object> info = new HashMap<>();
			info.put("USERID", "user1");
			info.put("JOBCATEGORY", BigDecimal.ZERO);
			given(jobMapper.findAppJobInfo(1L)).willReturn(info);

			jobService.withdrawApplication(1L, "user1");

			verify(jobMapper).withdrawApplication(1L, "user1");

			ArgumentCaptor<FileAssetDto> captor = ArgumentCaptor.forClass(FileAssetDto.class);
			verify(fileRefService).deleteFiles(captor.capture());
			assertThat(captor.getValue().getPost_id()).isEqualTo(1L);
			assertThat(captor.getValue().getPost_type()).isEqualTo("APPLICATION");
		}

		@Test
		@DisplayName("category가 1(멤버)이면 파일 연결 삭제를 호출하지 않는다")
		void withdrawApplication_category1_doesNotDeleteFiles() {
			Map<String, Object> info = new HashMap<>();
			info.put("USERID", "user1");
			info.put("JOBCATEGORY", BigDecimal.ONE);
			given(jobMapper.findAppJobInfo(1L)).willReturn(info);

			jobService.withdrawApplication(1L, "user1");

			verify(fileRefService, never()).deleteFiles(any());
		}
	}

	@Nested
	@DisplayName("단순 mapper 위임 메서드 (mapper 위임 확인용, XML SQL 검증 X)")
	class MapperDelegation {

		@Test
		@DisplayName("listCnt - mapper 결과 그대로 반환")
		void listCnt_delegatesToMapper() {
			JobDto job = new JobDto();
			given(jobMapper.listCnt(job)).willReturn(7);

			assertThat(jobService.listCnt(job)).isEqualTo(7);
		}

		@Test
		@DisplayName("writePost - mapper 결과 그대로 반환")
		void writePost_delegatesToMapper() {
			JobDto job = new JobDto();
			given(jobMapper.writePost(job)).willReturn(1);

			assertThat(jobService.writePost(job)).isEqualTo(1);
		}

		@Test
		@DisplayName("getPostById - mapper 결과 그대로 반환")
		void getPostById_delegatesToMapper() {
			JobDto mockResult = new JobDto();
			given(jobMapper.getPostById(1L)).willReturn(mockResult);

			assertThat(jobService.getPostById(1L)).isEqualTo(mockResult);
		}

		@Test
		@DisplayName("editPost - mapper 결과 그대로 반환")
		void editPost_delegatesToMapper() {
			JobDto job = new JobDto();
			given(jobMapper.editPost(job)).willReturn(1);

			assertThat(jobService.editPost(job)).isEqualTo(1);
		}

		@Test
		@DisplayName("closePost - mapper 결과 그대로 반환")
		void closePost_delegatesToMapper() {
			given(jobMapper.closePost(1L, "user1")).willReturn(1);

			assertThat(jobService.closePost(1L, "user1")).isEqualTo(1);
		}

		@Test
		@DisplayName("findCompanyIdByPostId - mapper 결과 그대로 반환")
		void findCompanyIdByPostId_delegatesToMapper() {
			given(jobMapper.findCompanyIdByPostId(1L)).willReturn("company1");

			assertThat(jobService.findCompanyIdByPostId(1L)).isEqualTo("company1");
		}

		@Test
		@DisplayName("getApplicationsByPostId - mapper 결과 그대로 반환")
		void getApplicationsByPostId_delegatesToMapper() {
			List<ApplicationDto> mockResult = List.of(new ApplicationDto());
			given(jobMapper.getApplicationsByPostId(1L)).willReturn(mockResult);

			assertThat(jobService.getApplicationsByPostId(1L)).isEqualTo(mockResult);
		}

		@Test
		@DisplayName("applicationsListCnt - mapper 결과 그대로 반환")
		void applicationsListCnt_delegatesToMapper() {
			ApplicationDto param = new ApplicationDto();
			given(jobMapper.applicationsListCnt(param)).willReturn(4);

			assertThat(jobService.applicationsListCnt(param)).isEqualTo(4);
		}

		@Test
		@DisplayName("getMyApplications - mapper 결과 그대로 반환")
		void getMyApplications_delegatesToMapper() {
			ApplicationDto param = new ApplicationDto();
			List<Map<String, Object>> mockResult = List.of(Map.of("k", "v"));
			given(jobMapper.getMyApplications(param)).willReturn(mockResult);

			assertThat(jobService.getMyApplications(param)).isEqualTo(mockResult);
		}

		@Test
		@DisplayName("getMyApplicationsCnt - mapper 결과 그대로 반환")
		void getMyApplicationsCnt_delegatesToMapper() {
			ApplicationDto param = new ApplicationDto();
			given(jobMapper.getMyApplicationsCnt(param)).willReturn(2);

			assertThat(jobService.getMyApplicationsCnt(param)).isEqualTo(2);
		}

		@Test
		@DisplayName("getMyFavorites - mapper 결과 그대로 반환")
		void getMyFavorites_delegatesToMapper() {
			JobDto job = new JobDto();
			List<Map<String, Object>> mockResult = List.of(Map.of("k", "v"));
			given(jobMapper.getMyFavorites(job)).willReturn(mockResult);

			assertThat(jobService.getMyFavorites(job)).isEqualTo(mockResult);
		}

		@Test
		@DisplayName("getMyFavoritesCnt - mapper 결과 그대로 반환")
		void getMyFavoritesCnt_delegatesToMapper() {
			JobDto job = new JobDto();
			given(jobMapper.getMyFavoritesCnt(job)).willReturn(6);

			assertThat(jobService.getMyFavoritesCnt(job)).isEqualTo(6);
		}
	}
}
