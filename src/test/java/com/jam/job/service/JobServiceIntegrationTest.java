package com.jam.job.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import com.jam.file.dto.FileAssetDto;
import com.jam.file.dto.FileCategory;
import com.jam.global.exception.BadRequestException;
import com.jam.global.exception.ConflictException;
import com.jam.global.exception.ForbiddenException;
import com.jam.job.dto.ApplicationDto;
import com.jam.job.dto.JobDto;
import com.jam.job.mapper.JobMapper;
import com.jam.member.entity.Member;
import com.jam.member.repository.MemberRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * JobService에 대한 @SpringBootTest 통합 테스트. JobMapper.xml에 있는 실제 SQL을 검증한다.
 * job 도메인은 JPA 엔티티가 없는 MyBatis 전용 도메인이라 JOB/APPLICATION row는 mapper/native query로 직접 시드한다.
 *
 * getBoard/listCnt/getMyApplications 등의 키워드 검색은 CONTAINS(Oracle Text)를 쓰는데, 이 인덱스는 COMMIT 시점에
 * 동기화된다(SYNC ON COMMIT으로 확인됨) - 즉 @Transactional 롤백 안에서 새로 insert한 row는 절대 검색에 안 잡힌다.
 * 그래서 키워드 검색 테스트(getBoard B)는 TestTransaction으로 실제 COMMIT까지 시켜서 직접 시드한 데이터로 검증하고,
 * 테스트가 끝나면 직접 DELETE로 정리한다 - 로컬/배포 등 환경에 있는 기존 데이터에 의존하지 않는다.
 */
@SpringBootTest
@Transactional
class JobServiceIntegrationTest {

	@Autowired
	private JobService jobService;

	@Autowired
	private JobMapper jobMapper;

	@Autowired
	private MemberRepository memberRepository;

	@PersistenceContext
	private EntityManager entityManager;

	private Member seedMember(String userId) {
		Member member = new Member();
		member.setUserId(userId);
		member.setUserPw("pw1234");
		// user_name은 VARCHAR2(25)라서 userId 길이에 상관없이 짧고 유일한 값을 생성
		member.setUserName("t" + Integer.toHexString(userId.hashCode()));
		Member saved = memberRepository.save(member);
		entityManager.flush();
		return saved;
	}

	private long seedJobPost(String userId, String title, int category) {
		return seedJobPost(userId, title, category, "guitar", "서울", null);
	}

	private long seedJobPost(String userId, String title, int category, String position, String city, String gu) {
		JobDto job = new JobDto();
		job.setTitle(title);
		job.setContent("통합테스트 시드 데이터");
		job.setCategory(category);
		job.setUser_id(userId);
		job.setCity(city);
		job.setGu(gu);
		job.setPosition(position);
		jobMapper.writePost(job);
		return job.getPost_id();
	}

	private void seedFavorite(String userId, long postId) {
		entityManager.flush();
		entityManager.createNativeQuery(
				"INSERT INTO favorite (favorite_id, user_id, post_id, board_type, created_at) "
						+ "VALUES (seq_favorite.nextval, :userId, :postId, 'JOB', SYSTIMESTAMP)")
				.setParameter("userId", userId)
				.setParameter("postId", postId)
				.executeUpdate();
	}

	// jobMapper.createApplication을 직접 호출해 지원서를 시드한다 (JobService의 지원 검증 로직은 우회).
	private long seedApplication(String applicantId, long postId, String companyUserId, String title) {
		ApplicationDto app = new ApplicationDto();
		app.setUser_id(applicantId);
		app.setPost_id(postId);
		app.setTitle(title);
		app.setContent("통합테스트 시드 데이터");
		app.setCompany_user_id(companyUserId);
		jobMapper.createApplication(app);
		return app.getApplication_id();
	}

	@Nested
	@DisplayName("getBoard")
	class GetBoard {

		@Test
		@DisplayName("A: 검색어 없이 조회하면 해당 category 전체 리스트를 반환한다")
		void getBoard_noKeyword() {
			JobDto job = new JobDto();
			job.setPageNum(1);
			job.setCategory(1);

			List<JobDto> result = jobService.getBoard(job);

			assertThat(result).isNotEmpty();
		}

		@Test
		@DisplayName("B: 키워드로 검색하면 제목에 해당 키워드가 포함된 글만 반환한다"
				+ " (CONTAINS 인덱스는 COMMIT 시점에 동기화되므로, 트랜잭션을 실제로 커밋해서 직접 시드한 글로 검증하고 테스트 후 되돌린다)")
		void getBoard_withKeyword() {
			Member member = seedMember("jobKeywordTestUser");
			String uniqueKeyword = "키워드검색테스트유니크단어";
			long postId = seedJobPost(member.getUserId(), uniqueKeyword + " 공고", 1);

			TestTransaction.flagForCommit();
			TestTransaction.end(); // 실제 COMMIT (Oracle Text 인덱스 동기화 트리거)

			try {
				JobDto job = new JobDto();
				job.setPageNum(1);
				job.setCategory(1);
				job.setKeyword(uniqueKeyword);

				List<JobDto> result = jobService.getBoard(job);

				assertThat(result).isNotEmpty();
				assertThat(result).allMatch(post -> post.getTitle().contains(uniqueKeyword));
			} finally {
				// @Transactional 롤백은 이미 커밋된 row는 못 지우므로 직접 정리
				TestTransaction.start();
				entityManager.createNativeQuery("DELETE FROM job WHERE post_id = :postId")
						.setParameter("postId", postId)
						.executeUpdate();
				entityManager.createNativeQuery("DELETE FROM member WHERE user_id = :userId")
						.setParameter("userId", member.getUserId())
						.executeUpdate();
				TestTransaction.flagForCommit();
				TestTransaction.end();
				TestTransaction.start();
			}
		}

		@Test
		@DisplayName("C: 일치하는 검색 결과가 없으면 빈 리스트를 반환한다")
		void getBoard_emptyResult() {
			JobDto job = new JobDto();
			job.setPageNum(1);
			job.setCategory(1);
			job.setKeyword("절대없을것같은검색어12345");

			List<JobDto> result = jobService.getBoard(job);

			assertThat(result).isEmpty();
		}

		@Test
		@DisplayName("D: 즐겨찾기한 글은 isFavorite=true, 안 한 글은 isFavorite=false로 정확히 반영된다")
		void getBoard_withUser_favoriteReflected() {
			Member member = seedMember("jobFavTestUser");
			long favoritedPostId = seedJobPost(member.getUserId(), "즐겨찾기한 멤버모집 글", 1);
			long notFavoritedPostId = seedJobPost(member.getUserId(), "즐겨찾기 안 한 멤버모집 글", 1);
			seedFavorite(member.getUserId(), favoritedPostId);

			JobDto job = new JobDto();
			job.setPageNum(1);
			job.setCategory(1);
			job.setUser_id(member.getUserId());

			List<JobDto> result = jobService.getBoard(job);

			JobDto favoritedResult = result.stream()
					.filter(post -> post.getPost_id().equals(favoritedPostId))
					.findFirst()
					.orElseThrow(() -> new AssertionError("즐겨찾기한 글이 조회 결과에 없습니다."));
			JobDto notFavoritedResult = result.stream()
					.filter(post -> post.getPost_id().equals(notFavoritedPostId))
					.findFirst()
					.orElseThrow(() -> new AssertionError("즐겨찾기 안 한 글이 조회 결과에 없습니다."));

			assertThat(favoritedResult.isFavorite()).isTrue();
			assertThat(notFavoritedResult.isFavorite()).isFalse();
		}

		@Test
		@DisplayName("E: 사용자 아이디가 없으면 모든 게시물의 즐겨찾기 여부는 false이다")
		void getBoard_noUser_allFalse() {
			JobDto job = new JobDto();
			job.setPageNum(1);
			job.setCategory(1);

			List<JobDto> result = jobService.getBoard(job);

			assertThat(result).isNotEmpty();
			assertThat(result).allMatch(post -> !post.isFavorite());
		}

		@Test
		@DisplayName("F: positions로 필터링하면 해당 포지션의 글만 반환한다")
		void getBoard_filtersByPositions() {
			Member member = seedMember("jobPositionFilterUser");
			long drumPostId = seedJobPost(member.getUserId(), "드럼 포지션 글", 1, "drum", "서울", null);
			seedJobPost(member.getUserId(), "기타 포지션 글", 1, "guitar", "서울", null);

			JobDto job = new JobDto();
			job.setPageNum(1);
			job.setCategory(1);
			job.setPositions(List.of("drum"));

			List<JobDto> result = jobService.getBoard(job);

			assertThat(result).extracting(JobDto::getPost_id).contains(drumPostId);
			assertThat(result).allMatch(post -> "drum".equals(post.getPosition()));
		}

		@Test
		@DisplayName("G: gu로 필터링하면 해당 지역의 글만 반환한다")
		void getBoard_filtersByGu() {
			Member member = seedMember("jobLocalFilterUser");
			long mapoPostId = seedJobPost(member.getUserId(), "마포구 공고", 1, "guitar", "서울", "마포구");
			seedJobPost(member.getUserId(), "강남구 공고", 1, "guitar", "서울", "강남구");

			JobDto job = new JobDto();
			job.setPageNum(1);
			job.setCategory(1);
			job.setGu("마포구");

			List<JobDto> result = jobService.getBoard(job);

			assertThat(result).extracting(JobDto::getPost_id).contains(mapoPostId);
			assertThat(result).allMatch(post -> "마포구".equals(post.getGu()));
		}
	}

	@Nested
	@DisplayName("listCnt")
	class ListCnt {

		@Test
		@DisplayName("검색조건 없이 새로 등록한 글 수만큼 카운트가 증가한다")
		void listCnt_increasesByNumberOfNewPosts() {
			JobDto job = new JobDto();
			job.setCategory(1);
			int before = jobService.listCnt(job);

			Member member = seedMember("jobCntTestUser");
			seedJobPost(member.getUserId(), "카운트 테스트 글 1", 1);
			seedJobPost(member.getUserId(), "카운트 테스트 글 2", 1);
			seedJobPost(member.getUserId(), "카운트 테스트 글 3", 1);

			int after = jobService.listCnt(job);

			assertThat(after - before).isEqualTo(3);
		}
	}

	@Nested
	@DisplayName("getPost")
	class GetPost {

		@Test
		@DisplayName("호출할 때마다 조회수가 증가하고, currentUserId가 없으면 즐겨찾기 여부는 항상 false다")
		void getPost_noUser_incrementsViewCount() {
			Member member = seedMember("jobPostViewUser");
			long postId = seedJobPost(member.getUserId(), "조회 테스트 글", 1);

			JobDto first = jobService.getPost(postId, null);
			JobDto second = jobService.getPost(postId, null);

			assertThat(first.isFavorite()).isFalse();
			assertThat(second.getView_count()).isEqualTo(first.getView_count() + 1);
		}

		@Test
		@DisplayName("currentUserId가 있으면 즐겨찾기 여부가 정확히 반영된다")
		void getPost_withUser_favoriteReflected() {
			Member owner = seedMember("jobPostOwner");
			Member favoriter = seedMember("jobPostFavoriter");
			long postId = seedJobPost(owner.getUserId(), "즐겨찾기 상세조회 테스트 글", 1);
			seedFavorite(favoriter.getUserId(), postId);

			JobDto favoriterView = jobService.getPost(postId, favoriter.getUserId());
			JobDto strangerView = jobService.getPost(postId, "stranger");

			assertThat(favoriterView.isFavorite()).isTrue();
			assertThat(strangerView.isFavorite()).isFalse();
			assertThat(favoriterView.getUser_id()).isEqualTo(owner.getUserId());
		}
	}

	@Nested
	@DisplayName("writePost / getPostById / editPost")
	class WritePostAndEdit {

		@Test
		@DisplayName("writePost는 시퀀스로 post_id를 생성해 돌려주고, getPostById로 그대로 조회된다")
		void writePost_generatesPostId_andRoundTrips() {
			Member member = seedMember("jobWriteUser");
			JobDto job = new JobDto();
			job.setTitle("글쓰기 테스트");
			job.setContent("내용");
			job.setCategory(0);
			job.setUser_id(member.getUserId());
			job.setCity("서울");
			job.setPosition("drum");
			job.setPay(3000000);

			jobService.writePost(job);

			assertThat(job.getPost_id()).isNotNull();

			JobDto found = jobService.getPostById(job.getPost_id());
			assertThat(found.getTitle()).isEqualTo("글쓰기 테스트");
		}

		@Test
		@DisplayName("작성자 본인이 수정하면 반영되고 1을 반환한다")
		void editPost_owner_updatesAndReturnsOne() {
			Member member = seedMember("jobEditOwner");
			long postId = seedJobPost(member.getUserId(), "수정 전 제목", 1);

			JobDto edit = new JobDto();
			edit.setPost_id(postId);
			edit.setUser_id(member.getUserId());
			edit.setTitle("수정 후 제목");
			edit.setContent("수정 후 내용");
			edit.setStatus(0);
			edit.setCategory(1);
			edit.setCity("부산");
			edit.setPosition("bass");

			int updated = jobService.editPost(edit);

			assertThat(updated).isEqualTo(1);
			assertThat(jobService.getPostById(postId).getTitle()).isEqualTo("수정 후 제목");
		}

		@Test
		@DisplayName("작성자 본인이 아니면 0을 반환하고 아무 것도 바뀌지 않는다")
		void editPost_notOwner_returnsZero_doesNotChange() {
			Member member = seedMember("jobEditVictim");
			long postId = seedJobPost(member.getUserId(), "수정 방어 테스트 글", 1);

			JobDto edit = new JobDto();
			edit.setPost_id(postId);
			edit.setUser_id("intruder");
			edit.setTitle("해킹 제목");
			edit.setContent("해킹 내용");
			edit.setStatus(0);
			edit.setCategory(1);
			edit.setCity("부산");
			edit.setPosition("bass");

			int updated = jobService.editPost(edit);

			assertThat(updated).isEqualTo(0);
			assertThat(jobService.getPostById(postId).getTitle()).isEqualTo("수정 방어 테스트 글");
		}
	}

	@Nested
	@DisplayName("deletePost / closePost")
	class DeleteAndClose {

		@Test
		@DisplayName("지원자가 있으면 ConflictException을 던지고 글이 삭제되지 않는다")
		void deletePost_hasApplicants_throwsConflict() {
			Member owner = seedMember("jobDeleteOwnerWithApp");
			Member applicant = seedMember("jobDeleteApplicant");
			long postId = seedJobPost(owner.getUserId(), "지원자 있는 글", 1);
			seedApplication(applicant.getUserId(), postId, owner.getUserId(), "지원합니다");

			assertThatThrownBy(() -> jobService.deletePost(postId, owner.getUserId()))
					.isInstanceOf(ConflictException.class);

			assertThat(jobService.getPostById(postId)).isNotNull();
		}

		@Test
		@DisplayName("지원자가 없으면 정상적으로 삭제된다")
		void deletePost_noApplicants_deletes() {
			Member owner = seedMember("jobDeleteOwnerNoApp");
			long postId = seedJobPost(owner.getUserId(), "지원자 없는 글", 1);

			jobService.deletePost(postId, owner.getUserId());

			assertThat(jobService.getPostById(postId)).isNull();
		}

		@Test
		@DisplayName("공고를 마감하면 status가 1로 바뀐다")
		void closePost_setsStatusToOne() {
			Member owner = seedMember("jobCloseOwner");
			long postId = seedJobPost(owner.getUserId(), "마감 테스트 글", 1);

			jobService.closePost(postId, owner.getUserId());

			assertThat(jobService.getPostById(postId).getStatus()).isEqualTo(1);
		}
	}

	@Nested
	@DisplayName("getMyPosts / getMyPostCnt")
	class GetMyPosts {

		@Test
		@DisplayName("ROLE_COMPANY면 내가 쓴 category=0 글만 조회되고 지원자 수(applyCount)가 정확히 집계된다")
		void getMyPosts_roleCompany_returnsOwnPostsWithApplyCount() {
			Member company = seedMember("jobMyPostsCompany");
			Member other = seedMember("jobMyPostsOtherCompany");
			Member applicant1 = seedMember("jobMyPostsApplicant1");
			Member applicant2 = seedMember("jobMyPostsApplicant2");

			long postWithApps = seedJobPost(company.getUserId(), "지원자 2명인 기업 공고", 0);
			long postNoApps = seedJobPost(company.getUserId(), "지원자 없는 기업 공고", 0);
			seedJobPost(other.getUserId(), "타인의 기업 공고", 0);

			seedApplication(applicant1.getUserId(), postWithApps, company.getUserId(), "지원1");
			seedApplication(applicant2.getUserId(), postWithApps, company.getUserId(), "지원2");

			JobDto param = new JobDto();
			param.setPageNum(1);
			param.setUser_id(company.getUserId());

			List<JobDto> result = jobService.getMyPosts(param, Set.of("ROLE_COMPANY"));
			int cnt = jobService.getMyPostCnt(param, Set.of("ROLE_COMPANY"));

			assertThat(result).extracting(JobDto::getPost_id)
					.containsExactlyInAnyOrder(postWithApps, postNoApps);
			assertThat(cnt).isEqualTo(2);

			JobDto withApps = result.stream().filter(p -> p.getPost_id().equals(postWithApps)).findFirst().orElseThrow();
			assertThat(withApps.getApplyCount()).isEqualTo(2);
		}

		@Test
		@DisplayName("아무 역할도 없으면 빈 리스트와 0을 반환한다")
		void getMyPosts_noRole_returnsEmpty() {
			JobDto param = new JobDto();
			param.setUser_id("anyone");

			assertThat(jobService.getMyPosts(param, Set.of("ROLE_ADMIN"))).isEmpty();
			assertThat(jobService.getMyPostCnt(param, Set.of("ROLE_ADMIN"))).isZero();
		}
	}

	@Nested
	@DisplayName("createApplication / getApplication / withdrawApplication")
	class ApplicationFlow {

		@Test
		@DisplayName("멤버 모집(category 1) 공고에 지원하면 application_id가 생성된다")
		void createApplication_category1_success() {
			Member recruiter = seedMember("jobAppRecruiter");
			Member applicant = seedMember("jobAppApplicant1");
			long postId = seedJobPost(recruiter.getUserId(), "멤버 모집 지원 테스트", 1);

			ApplicationDto app = new ApplicationDto();
			app.setPost_id(postId);
			app.setUser_id(applicant.getUserId());
			app.setTitle("지원합니다");
			app.setContent("잘 부탁드립니다");

			jobService.createApplication(app);

			assertThat(app.getApplication_id()).isNotNull();
			assertThat(app.getCompany_user_id()).isEqualTo(recruiter.getUserId());
		}

		@Test
		@DisplayName("기업 공고(category 0)는 이력서 파일과 함께 지원해야 하며, 성공하면 파일도 연결된다")
		void createApplication_category0_withFile_success() {
			Member company = seedMember("jobAppCompany");
			Member applicant = seedMember("jobAppApplicant2");
			long postId = seedJobPost(company.getUserId(), "기업 공고 지원 테스트", 0);

			FileAssetDto file = new FileAssetDto();
			file.setFile_key("test-key/resume.pdf");
			file.setFile_name("resume.pdf");
			file.setFile_type("application/pdf");
			file.setFile_size(1000L);
			file.setFile_category(FileCategory.APPLICATION);

			ApplicationDto app = new ApplicationDto();
			app.setPost_id(postId);
			app.setUser_id(applicant.getUserId());
			app.setTitle("이력서 첨부 지원");
			app.setContent("잘 부탁드립니다");
			app.setFile_assets(List.of(file));

			jobService.createApplication(app);

			assertThat(app.getApplication_id()).isNotNull();

			FileAssetDto queryParam = new FileAssetDto();
			queryParam.setPost_id(app.getApplication_id());
			queryParam.setPost_type(FileCategory.APPLICATION.name());
			Map<String, Object> detail = jobService.getApplication(app.getApplication_id(), applicant.getUserId());

			@SuppressWarnings("unchecked")
			List<FileAssetDto> files = (List<FileAssetDto>) detail.get("files");
			assertThat(files).hasSize(1);
			assertThat(files.get(0).getFile_name()).isEqualTo("resume.pdf");
		}

		@Test
		@DisplayName("본인이 등록한 공고에는 지원할 수 없다")
		void createApplication_selfApply_throwsBadRequest() {
			Member owner = seedMember("jobAppSelfOwner");
			long postId = seedJobPost(owner.getUserId(), "본인 공고", 1);

			ApplicationDto app = new ApplicationDto();
			app.setPost_id(postId);
			app.setUser_id(owner.getUserId());
			app.setTitle("셀프 지원");

			assertThatThrownBy(() -> jobService.createApplication(app))
					.isInstanceOf(BadRequestException.class);
		}

		@Test
		@DisplayName("공고 작성자나 지원자 본인이 아니면 지원서를 볼 수 없다")
		void getApplication_notOwnerNorApplicant_throwsForbidden() {
			Member recruiter = seedMember("jobAppViewRecruiter");
			Member applicant = seedMember("jobAppViewApplicant");
			long postId = seedJobPost(recruiter.getUserId(), "지원서 열람 테스트", 1);
			long applicationId = seedApplication(applicant.getUserId(), postId, recruiter.getUserId(), "지원합니다");

			assertThatThrownBy(() -> jobService.getApplication(applicationId, "stranger"))
					.isInstanceOf(ForbiddenException.class);
		}

		@Test
		@DisplayName("지원을 취소하면 지원자 목록에서 사라진다")
		void withdrawApplication_removesFromList() {
			Member recruiter = seedMember("jobWithdrawRecruiter");
			Member applicant = seedMember("jobWithdrawApplicant");
			long postId = seedJobPost(recruiter.getUserId(), "지원 취소 테스트", 1);
			long applicationId = seedApplication(applicant.getUserId(), postId, recruiter.getUserId(), "지원합니다");

			jobService.withdrawApplication(applicationId, applicant.getUserId());

			List<ApplicationDto> remaining = jobService.getApplicationsByPostId(postId);
			assertThat(remaining).extracting(ApplicationDto::getApplication_id)
					.doesNotContain(applicationId);
		}

		@Test
		@DisplayName("지원자 본인이 아니면 지원을 취소할 수 없다")
		void withdrawApplication_notApplicant_throwsForbidden() {
			Member recruiter = seedMember("jobWithdrawRecruiter2");
			Member applicant = seedMember("jobWithdrawApplicant2");
			long postId = seedJobPost(recruiter.getUserId(), "지원 취소 방어 테스트", 1);
			long applicationId = seedApplication(applicant.getUserId(), postId, recruiter.getUserId(), "지원합니다");

			assertThatThrownBy(() -> jobService.withdrawApplication(applicationId, "intruder"))
					.isInstanceOf(ForbiddenException.class);

			assertThat(jobService.getApplicationsByPostId(postId))
					.extracting(ApplicationDto::getApplication_id)
					.contains(applicationId);
		}
	}

	@Nested
	@DisplayName("getMyApplications / getMyApplicationsCnt")
	class GetMyApplications {

		@Test
		@DisplayName("job_status로 필터링하면 해당 상태의 공고에 낸 지원만 반환한다")
		void getMyApplications_filtersByJobStatus() {
			Member recruiter = seedMember("jobAppStatusRecruiter");
			Member applicant = seedMember("jobAppStatusApplicant");
			long openPostId = seedJobPost(recruiter.getUserId(), "모집중인 공고", 1);
			long closedPostId = seedJobPost(recruiter.getUserId(), "마감된 공고", 1);
			jobService.closePost(closedPostId, recruiter.getUserId());

			long openAppId = seedApplication(applicant.getUserId(), openPostId, recruiter.getUserId(), "모집중 공고 지원");
			long closedAppId = seedApplication(applicant.getUserId(), closedPostId, recruiter.getUserId(), "마감 공고 지원");

			ApplicationDto openParam = new ApplicationDto();
			openParam.setUser_id(applicant.getUserId());
			openParam.setJob_status(0);

			ApplicationDto closedParam = new ApplicationDto();
			closedParam.setUser_id(applicant.getUserId());
			closedParam.setJob_status(1);

			List<Map<String, Object>> openResult = jobService.getMyApplications(openParam);
			List<Map<String, Object>> closedResult = jobService.getMyApplications(closedParam);

			assertThat(openResult).extracting(m -> ((Number) m.get("APPLICATION_ID")).longValue())
					.containsExactly(openAppId);
			assertThat(closedResult).extracting(m -> ((Number) m.get("APPLICATION_ID")).longValue())
					.containsExactly(closedAppId);
			assertThat(jobService.getMyApplicationsCnt(openParam)).isEqualTo(1);
			assertThat(jobService.getMyApplicationsCnt(closedParam)).isEqualTo(1);
		}

		@Test
		@DisplayName("period(최근 N일)로 필터링하면 그 기간보다 오래된 지원은 제외된다")
		void getMyApplications_filtersByPeriod() {
			Member recruiter = seedMember("jobAppPeriodRecruiter");
			Member applicant = seedMember("jobAppPeriodApplicant");
			long postId = seedJobPost(recruiter.getUserId(), "기간 필터 테스트 공고", 1);
			long oldAppId = seedApplication(applicant.getUserId(), postId, recruiter.getUserId(), "오래된 지원");

			entityManager.createNativeQuery("UPDATE application SET created_at = SYSDATE - 40 WHERE application_id = :id")
					.setParameter("id", oldAppId)
					.executeUpdate();

			ApplicationDto last30Days = new ApplicationDto();
			last30Days.setUser_id(applicant.getUserId());
			last30Days.setPeriod("30");

			ApplicationDto allTime = new ApplicationDto();
			allTime.setUser_id(applicant.getUserId());
			allTime.setPeriod("all");

			assertThat(jobService.getMyApplications(last30Days))
					.extracting(m -> ((Number) m.get("APPLICATION_ID")).longValue()).doesNotContain(oldAppId);
			assertThat(jobService.getMyApplicationsCnt(last30Days)).isEqualTo(0);

			assertThat(jobService.getMyApplications(allTime))
					.extracting(m -> ((Number) m.get("APPLICATION_ID")).longValue()).contains(oldAppId);
			assertThat(jobService.getMyApplicationsCnt(allTime)).isEqualTo(1);
		}
	}

	@Nested
	@DisplayName("getMyFavorites / getMyFavoritesCnt")
	class GetMyFavorites {

		@Test
		@DisplayName("내가 즐겨찾기(스크랩)한 공고만 반환한다")
		void getMyFavorites_returnsOnlyFavoritedPosts() {
			Member member = seedMember("jobFavoritesUser");
			long favoritedPostId = seedJobPost(member.getUserId(), "스크랩 테스트 글", 1);
			seedJobPost(member.getUserId(), "스크랩 안 한 글", 1);
			seedFavorite(member.getUserId(), favoritedPostId);

			JobDto param = new JobDto();
			param.setPageNum(1);
			param.setCategory(1);
			param.setUser_id(member.getUserId());

			List<Map<String, Object>> result = jobService.getMyFavorites(param);
			int cnt = jobService.getMyFavoritesCnt(param);

			assertThat(result).hasSize(1);
			assertThat(cnt).isEqualTo(1);
		}

		@Test
		@DisplayName("status는 JobDto 기본값(0)이라 명시하지 않으면 마감(status=1)된 스크랩 글은 안 보인다")
		void getMyFavorites_defaultStatus_excludesClosedJobs() {
			Member member = seedMember("jobFavStatusUser");
			long openPostId = seedJobPost(member.getUserId(), "모집중 스크랩 글", 1);
			long closedPostId = seedJobPost(member.getUserId(), "마감 스크랩 글", 1);
			jobService.closePost(closedPostId, member.getUserId());
			seedFavorite(member.getUserId(), openPostId);
			seedFavorite(member.getUserId(), closedPostId);

			JobDto defaultParam = new JobDto();
			defaultParam.setPageNum(1);
			defaultParam.setCategory(1);
			defaultParam.setUser_id(member.getUserId());
			// status를 세팅하지 않음 - int 기본값 0

			JobDto closedParam = new JobDto();
			closedParam.setPageNum(1);
			closedParam.setCategory(1);
			closedParam.setUser_id(member.getUserId());
			closedParam.setStatus(1);

			List<Map<String, Object>> defaultResult = jobService.getMyFavorites(defaultParam);
			List<Map<String, Object>> closedResult = jobService.getMyFavorites(closedParam);

			assertThat(defaultResult).extracting(m -> ((Number) m.get("POST_ID")).longValue())
					.contains(openPostId).doesNotContain(closedPostId);
			assertThat(closedResult).extracting(m -> ((Number) m.get("POST_ID")).longValue())
					.contains(closedPostId).doesNotContain(openPostId);
		}

		@Test
		@DisplayName("period(최근 N일)로 필터링하면 그 기간보다 오래된 스크랩 글은 제외된다")
		void getMyFavorites_filtersByPeriod() {
			Member member = seedMember("jobFavPeriodUser");
			long oldPostId = seedJobPost(member.getUserId(), "오래된 스크랩 글", 1);
			seedFavorite(member.getUserId(), oldPostId);

			entityManager.createNativeQuery("UPDATE job SET created_at = SYSDATE - 40 WHERE post_id = :id")
					.setParameter("id", oldPostId)
					.executeUpdate();

			JobDto last30Days = new JobDto();
			last30Days.setPageNum(1);
			last30Days.setCategory(1);
			last30Days.setUser_id(member.getUserId());
			last30Days.setPeriod("30");

			JobDto allTime = new JobDto();
			allTime.setPageNum(1);
			allTime.setCategory(1);
			allTime.setUser_id(member.getUserId());
			allTime.setPeriod("all");

			assertThat(jobService.getMyFavorites(last30Days))
					.extracting(m -> ((Number) m.get("POST_ID")).longValue()).doesNotContain(oldPostId);
			assertThat(jobService.getMyFavorites(allTime))
					.extracting(m -> ((Number) m.get("POST_ID")).longValue()).contains(oldPostId);
		}
	}
}
