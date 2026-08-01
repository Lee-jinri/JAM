package com.jam.job.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import com.jam.config.MyBatisConfig;
import com.jam.global.exception.BadRequestException;
import com.jam.global.exception.ConflictException;
import com.jam.global.exception.ForbiddenException;
import com.jam.global.exception.NotFoundException;
import com.jam.global.security.JwtAuthenticationFilter;
import com.jam.job.dto.JobDto;
import com.jam.job.service.JobService;
import com.jam.member.dto.MemberDto;

/**
 * JobRestController @WebMvcTest 슬라이스 테스트. community/fleaMarket과 같은 패턴:
 * 실제 SecurityConfig 대신 @PreAuthorize만 동작하는 최소 Security 설정(permitAll)을 따로 구성해서 씀.
 * MemberDto가 UserDetails를 구현하고, roles가 비어있으면 getAuthorities()가 기본으로 ROLE_USER를 반환하므로
 * ROLE_COMPANY/그 외 역할 테스트는 roles를 명시적으로 세팅한 principal을 따로 만들어 사용한다.
 */
@WebMvcTest(
		controllers = JobRestController.class,
		excludeFilters = {
				@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class),
				@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = MyBatisConfig.class)
		})
@Import(JobRestControllerTest.MethodSecurityTestConfig.class)
class JobRestControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private JobService jobService;

	private MemberDto loginUser; // roles 미설정 -> 기본 ROLE_USER
	private MemberDto companyUser;
	private MemberDto noRoleUser;

	@BeforeEach
	void setUp() {
		reset(jobService);

		loginUser = new MemberDto();
		loginUser.setUser_id("user1");
		loginUser.setUser_name("tester");

		companyUser = new MemberDto();
		companyUser.setUser_id("company1");
		companyUser.setUser_name("companyTester");
		companyUser.setRoles(List.of("ROLE_COMPANY"));

		noRoleUser = new MemberDto();
		noRoleUser.setUser_id("norole1");
		noRoleUser.setUser_name("noRoleTester");
		noRoleUser.setRoles(List.of("ROLE_ADMIN"));

		// writePost는 실제 매퍼의 <selectKey>가 post_id를 채워주는데, 목에서는 그 동작이 없으니 대신 채워줌
		doAnswer(inv -> {
			JobDto dto = inv.getArgument(0);
			dto.setPost_id(10L);
			return 1;
		}).when(jobService).writePost(any(JobDto.class));
	}

	@TestConfiguration
	@EnableMethodSecurity
	static class MethodSecurityTestConfig {
		@Bean
		SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
			http.csrf(csrf -> csrf.disable())
					.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
			return http.build();
		}
	}

	@Nested
	@DisplayName("GET /api/jobs/board")
	class GetBoard {

		@Test
		@DisplayName("정상 조회 시 jobList와 pageMaker를 반환한다")
		void getBoard_success() throws Exception {
			given(jobService.getBoard(any(JobDto.class))).willReturn(List.of());
			given(jobService.listCnt(any(JobDto.class))).willReturn(0);

			mockMvc.perform(get("/api/jobs/board"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.jobList").isArray())
					.andExpect(jsonPath("$.pageMaker").exists());
		}

		@Test
		@DisplayName("keyword가 있으면 sanitize된 값이 서비스로 전달된다")
		void getBoard_withKeyword_sanitized() throws Exception {
			given(jobService.getBoard(any(JobDto.class))).willReturn(List.of());
			given(jobService.listCnt(any(JobDto.class))).willReturn(0);

			mockMvc.perform(get("/api/jobs/board").param("keyword", "  기타  "))
					.andExpect(status().isOk());

			ArgumentCaptor<JobDto> captor = ArgumentCaptor.forClass(JobDto.class);
			verify(jobService).getBoard(captor.capture());
			assertThat(captor.getValue().getKeyword()).isEqualTo("기타");
		}

		@Test
		@DisplayName("로그인 사용자면 user_id가 세팅된다")
		void getBoard_loggedIn_setsUserId() throws Exception {
			given(jobService.getBoard(any(JobDto.class))).willReturn(List.of());
			given(jobService.listCnt(any(JobDto.class))).willReturn(0);

			mockMvc.perform(get("/api/jobs/board").with(user(loginUser)))
					.andExpect(status().isOk());

			ArgumentCaptor<JobDto> captor = ArgumentCaptor.forClass(JobDto.class);
			verify(jobService).getBoard(captor.capture());
			assertThat(captor.getValue().getUser_id()).isEqualTo("user1");
		}

		@Test
		@DisplayName("비로그인이면 user_id가 세팅되지 않는다")
		void getBoard_anonymous_noUserId() throws Exception {
			given(jobService.getBoard(any(JobDto.class))).willReturn(List.of());
			given(jobService.listCnt(any(JobDto.class))).willReturn(0);

			mockMvc.perform(get("/api/jobs/board"))
					.andExpect(status().isOk());

			ArgumentCaptor<JobDto> captor = ArgumentCaptor.forClass(JobDto.class);
			verify(jobService).getBoard(captor.capture());
			assertThat(captor.getValue().getUser_id()).isNull();
		}

		@Test
		@DisplayName("positions 파라미터가 없으면 빈 리스트로 세팅된다")
		void getBoard_noPositions_defaultsToEmptyList() throws Exception {
			given(jobService.getBoard(any(JobDto.class))).willReturn(List.of());
			given(jobService.listCnt(any(JobDto.class))).willReturn(0);

			mockMvc.perform(get("/api/jobs/board"))
					.andExpect(status().isOk());

			ArgumentCaptor<JobDto> captor = ArgumentCaptor.forClass(JobDto.class);
			verify(jobService).getBoard(captor.capture());
			assertThat(captor.getValue().getPositions()).isEmpty();
		}

		@Test
		@DisplayName("positions 파라미터가 있으면 그대로 서비스에 전달된다")
		void getBoard_withPositions_passedThrough() throws Exception {
			given(jobService.getBoard(any(JobDto.class))).willReturn(List.of());
			given(jobService.listCnt(any(JobDto.class))).willReturn(0);

			mockMvc.perform(get("/api/jobs/board").param("positions", "guitar", "drum"))
					.andExpect(status().isOk());

			ArgumentCaptor<JobDto> captor = ArgumentCaptor.forClass(JobDto.class);
			verify(jobService).getBoard(captor.capture());
			assertThat(captor.getValue().getPositions()).containsExactly("guitar", "drum");
		}
	}

	@Nested
	@DisplayName("GET /api/jobs/post/{post_id}")
	class GetPost {

		private JobDto post;

		@BeforeEach
		void setUpPost() {
			post = new JobDto();
			post.setPost_id(1L);
			post.setUser_id("author1");
			post.setPosition("guitar");
		}

		@Test
		@DisplayName("post_id가 0 이하면 400")
		void getPost_invalidId_badRequest() throws Exception {
			mockMvc.perform(get("/api/jobs/post/0"))
					.andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("정상 조회 시 포지션이 한글로 번역되어 반환된다")
		void getPost_success_translatesPosition() throws Exception {
			given(jobService.getPost(1L, null)).willReturn(post);

			mockMvc.perform(get("/api/jobs/post/1"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.post.position").value("기타"));
		}

		@Test
		@DisplayName("로그인 사용자가 작성자 본인이면 isAuthor=true")
		void getPost_isAuthor_true() throws Exception {
			given(jobService.getPost(1L, "author1")).willReturn(post);
			loginUser.setUser_id("author1");

			mockMvc.perform(get("/api/jobs/post/1").with(user(loginUser)))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.isAuthor").value(true));
		}

		@Test
		@DisplayName("로그인 사용자가 작성자가 아니면 isAuthor=false")
		void getPost_isAuthor_false() throws Exception {
			given(jobService.getPost(1L, "user1")).willReturn(post);

			mockMvc.perform(get("/api/jobs/post/1").with(user(loginUser)))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.isAuthor").value(false));
		}

		@Test
		@DisplayName("비로그인이면 isAuthor=false")
		void getPost_anonymous_isAuthorFalse() throws Exception {
			given(jobService.getPost(1L, null)).willReturn(post);

			mockMvc.perform(get("/api/jobs/post/1"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.isAuthor").value(false));
		}
	}

	@Nested
	@DisplayName("POST /api/jobs/post")
	class WritePost {

		private static final String VALID_BODY =
				"{\"title\":\"제목\",\"content\":\"내용\",\"position\":\"guitar\",\"pay\":3000000,\"city\":\"서울\"}";

		@Test
		@DisplayName("비인증이면 접근 거부된다")
		void writePost_anonymous_denied() throws Exception {
			mockMvc.perform(post("/api/jobs/post")
							.contentType(MediaType.APPLICATION_JSON)
							.content(VALID_BODY))
					.andExpect(status().isForbidden());
		}

		@Test
		@DisplayName("title이 없으면 400")
		void writePost_noTitle() throws Exception {
			mockMvc.perform(post("/api/jobs/post")
							.with(user(companyUser))
							.contentType(MediaType.APPLICATION_JSON)
							.content("{\"content\":\"내용\",\"position\":\"guitar\",\"pay\":3000000,\"city\":\"서울\"}"))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.detail").value("제목은 필수 입력 항목입니다."));
		}

		@Test
		@DisplayName("content가 없으면 400")
		void writePost_noContent() throws Exception {
			mockMvc.perform(post("/api/jobs/post")
							.with(user(companyUser))
							.contentType(MediaType.APPLICATION_JSON)
							.content("{\"title\":\"제목\",\"position\":\"guitar\",\"pay\":3000000,\"city\":\"서울\"}"))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.detail").value("내용은 필수 입력 항목입니다."));
		}

		@Test
		@DisplayName("position이 없으면 400")
		void writePost_noPosition() throws Exception {
			mockMvc.perform(post("/api/jobs/post")
							.with(user(companyUser))
							.contentType(MediaType.APPLICATION_JSON)
							.content("{\"title\":\"제목\",\"content\":\"내용\",\"pay\":3000000,\"city\":\"서울\"}"))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.detail").value("포지션을 선택해주세요."));
		}

		@Test
		@DisplayName("기업 공고(ROLE_COMPANY)인데 급여가 없으면 400")
		void writePost_companyNoPay() throws Exception {
			mockMvc.perform(post("/api/jobs/post")
							.with(user(companyUser))
							.contentType(MediaType.APPLICATION_JSON)
							.content("{\"title\":\"제목\",\"content\":\"내용\",\"position\":\"guitar\",\"city\":\"서울\"}"))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.detail").value("급여를 입력하세요."));
		}

		@Test
		@DisplayName("지역이 없으면 400")
		void writePost_noCity() throws Exception {
			mockMvc.perform(post("/api/jobs/post")
							.with(user(companyUser))
							.contentType(MediaType.APPLICATION_JSON)
							.content("{\"title\":\"제목\",\"content\":\"내용\",\"position\":\"guitar\",\"pay\":3000000}"))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.detail").value("지역을 선택하세요."));
		}

		@Test
		@DisplayName("ROLE_COMPANY/ROLE_USER 둘 다 아니면 403")
		void writePost_unknownRole_forbidden() throws Exception {
			mockMvc.perform(post("/api/jobs/post")
							.with(user(noRoleUser))
							.contentType(MediaType.APPLICATION_JSON)
							.content(VALID_BODY))
					.andExpect(status().isForbidden())
					.andExpect(jsonPath("$.detail").value("해당 메뉴에 접근할 권한이 없습니다."));
		}

		@Test
		@DisplayName("ROLE_COMPANY로 작성하면 category=0으로 저장되고 postId를 응답한다")
		void writePost_company_success() throws Exception {
			mockMvc.perform(post("/api/jobs/post")
							.with(user(companyUser))
							.contentType(MediaType.APPLICATION_JSON)
							.content(VALID_BODY))
					.andExpect(status().isOk());

			ArgumentCaptor<JobDto> captor = ArgumentCaptor.forClass(JobDto.class);
			verify(jobService).writePost(captor.capture());
			assertThat(captor.getValue().getCategory()).isEqualTo(0);
			assertThat(captor.getValue().getUser_id()).isEqualTo("company1");
		}

		@Test
		@DisplayName("ROLE_USER로 작성하면 category=1로 저장된다 (급여 없어도 통과)")
		void writePost_user_success_noPayRequired() throws Exception {
			mockMvc.perform(post("/api/jobs/post")
							.with(user(loginUser))
							.contentType(MediaType.APPLICATION_JSON)
							.content("{\"title\":\"제목\",\"content\":\"내용\",\"position\":\"guitar\",\"city\":\"서울\"}"))
					.andExpect(status().isOk());

			ArgumentCaptor<JobDto> captor = ArgumentCaptor.forClass(JobDto.class);
			verify(jobService).writePost(captor.capture());
			assertThat(captor.getValue().getCategory()).isEqualTo(1);
		}

		@Test
		@DisplayName("title/content가 sanitize된 값으로 서비스에 전달된다")
		void writePost_sanitizesInput() throws Exception {
			mockMvc.perform(post("/api/jobs/post")
							.with(user(companyUser))
							.contentType(MediaType.APPLICATION_JSON)
							.content("{\"title\":\"<b>제목</b>\",\"content\":\"<script>alert(1)</script>내용\","
									+ "\"position\":\"guitar\",\"pay\":3000000,\"city\":\"서울\"}"))
					.andExpect(status().isOk());

			ArgumentCaptor<JobDto> captor = ArgumentCaptor.forClass(JobDto.class);
			verify(jobService).writePost(captor.capture());
			assertThat(captor.getValue().getTitle()).doesNotContain("<b>");
			assertThat(captor.getValue().getContent()).doesNotContain("<script>");
		}
	}

	@Nested
	@DisplayName("PUT /api/jobs/post/{postId}")
	class EditPost {

		private static final String VALID_BODY =
				"{\"title\":\"제목\",\"content\":\"내용\",\"position\":\"guitar\",\"pay\":3000000,\"city\":\"서울\"}";

		@Test
		@DisplayName("비인증이면 접근 거부된다")
		void editPost_anonymous_denied() throws Exception {
			mockMvc.perform(put("/api/jobs/post/1")
							.contentType(MediaType.APPLICATION_JSON)
							.content(VALID_BODY))
					.andExpect(status().isForbidden());
		}

		@Test
		@DisplayName("title이 없으면 400")
		void editPost_noTitle() throws Exception {
			mockMvc.perform(put("/api/jobs/post/1")
							.with(user(companyUser))
							.contentType(MediaType.APPLICATION_JSON)
							.content("{\"content\":\"내용\",\"position\":\"guitar\",\"pay\":3000000,\"city\":\"서울\"}"))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.detail").value("제목은 필수 입력 항목입니다."));
		}

		@Test
		@DisplayName("정상 수정 시 postId를 응답한다")
		void editPost_success() throws Exception {
			given(jobService.editPost(any(JobDto.class))).willReturn(1);

			mockMvc.perform(put("/api/jobs/post/1")
							.with(user(companyUser))
							.contentType(MediaType.APPLICATION_JSON)
							.content(VALID_BODY))
					.andExpect(status().isOk())
					.andExpect(content().string("1"));
		}

		@Test
		@DisplayName("본인 글이 아니면(수정건수 0) 403이 반환된다")
		void editPost_notOwner_forbidden() throws Exception {
			given(jobService.editPost(any(JobDto.class))).willReturn(0);

			mockMvc.perform(put("/api/jobs/post/1")
							.with(user(companyUser))
							.contentType(MediaType.APPLICATION_JSON)
							.content(VALID_BODY))
					.andExpect(status().isForbidden())
					.andExpect(jsonPath("$.detail").value("수정 권한이 없습니다."));
		}
	}

	@Nested
	@DisplayName("DELETE /api/jobs/post/{postId}")
	class DeletePost {

		@Test
		@DisplayName("비인증이면 접근 거부된다")
		void deletePost_anonymous_denied() throws Exception {
			mockMvc.perform(delete("/api/jobs/post/1"))
					.andExpect(status().isForbidden());
		}

		@Test
		@DisplayName("정상 삭제 시 200을 응답한다")
		void deletePost_success() throws Exception {
			mockMvc.perform(delete("/api/jobs/post/1").with(user(loginUser)))
					.andExpect(status().isOk());

			verify(jobService).deletePost(1L, "user1");
		}

		@Test
		@DisplayName("지원자가 있으면(ConflictException) 409가 전파된다")
		void deletePost_hasApplicants_conflict() throws Exception {
			doThrow(new ConflictException("지원자가 있어 삭제할 수 없습니다. 공고를 마감 처리하세요."))
					.when(jobService).deletePost(1L, "user1");

			mockMvc.perform(delete("/api/jobs/post/1").with(user(loginUser)))
					.andExpect(status().isConflict())
					.andExpect(jsonPath("$.detail").value("지원자가 있어 삭제할 수 없습니다. 공고를 마감 처리하세요."));
		}
	}

	@Nested
	@DisplayName("PATCH /api/jobs/post/{postId}")
	class CloseJob {

		@Test
		@DisplayName("비인증이면 접근 거부된다")
		void closeJob_anonymous_denied() throws Exception {
			mockMvc.perform(patch("/api/jobs/post/1"))
					.andExpect(status().isForbidden());
		}

		@Test
		@DisplayName("정상 마감 시 200을 응답한다")
		void closeJob_success() throws Exception {
			mockMvc.perform(patch("/api/jobs/post/1").with(user(loginUser)))
					.andExpect(status().isOk());

			verify(jobService).closePost(1L, "user1");
		}
	}

	@Nested
	@DisplayName("POST /api/jobs/applications")
	class CreateApplication {

		private static final String VALID_BODY = "{\"post_id\":1,\"title\":\"지원합니다\",\"content\":\"내용\"}";

		@Test
		@DisplayName("비인증이면 접근 거부된다")
		void createApplication_anonymous_denied() throws Exception {
			mockMvc.perform(post("/api/jobs/applications")
							.contentType(MediaType.APPLICATION_JSON)
							.content(VALID_BODY))
					.andExpect(status().isForbidden());
		}

		@Test
		@DisplayName("정상 지원 시 200을 응답하고 로그인한 user_id로 저장된다")
		void createApplication_success() throws Exception {
			mockMvc.perform(post("/api/jobs/applications")
							.with(user(loginUser))
							.contentType(MediaType.APPLICATION_JSON)
							.content(VALID_BODY))
					.andExpect(status().isOk());

			ArgumentCaptor<com.jam.job.dto.ApplicationDto> captor =
					ArgumentCaptor.forClass(com.jam.job.dto.ApplicationDto.class);
			verify(jobService).createApplication(captor.capture());
			assertThat(captor.getValue().getUser_id()).isEqualTo("user1");
		}

		@Test
		@DisplayName("본인이 등록한 공고면(BadRequestException) 400이 전파된다")
		void createApplication_selfApply_badRequest() throws Exception {
			doThrow(new BadRequestException("본인이 등록한 공고에는 지원할 수 없습니다."))
					.when(jobService).createApplication(any());

			mockMvc.perform(post("/api/jobs/applications")
							.with(user(loginUser))
							.contentType(MediaType.APPLICATION_JSON)
							.content(VALID_BODY))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.detail").value("본인이 등록한 공고에는 지원할 수 없습니다."));
		}

		@Test
		@DisplayName("존재하지 않는 공고면(NotFoundException) 404가 전파된다")
		void createApplication_postNotFound() throws Exception {
			doThrow(new NotFoundException("존재하지 않는 공고입니다."))
					.when(jobService).createApplication(any());

			mockMvc.perform(post("/api/jobs/applications")
							.with(user(loginUser))
							.contentType(MediaType.APPLICATION_JSON)
							.content(VALID_BODY))
					.andExpect(status().isNotFound());
		}
	}

	@Nested
	@DisplayName("GET /api/jobs/my/posts")
	class GetPostings {

		@Test
		@DisplayName("비인증이면 접근 거부된다")
		void getPostings_anonymous_denied() throws Exception {
			mockMvc.perform(get("/api/jobs/my/posts"))
					.andExpect(status().isForbidden());
		}

		@Test
		@DisplayName("정상 조회 시 postings/pageMaker/category를 반환한다")
		void getPostings_success() throws Exception {
			given(jobService.getMyPosts(any(JobDto.class), eq(Set.of("ROLE_USER")))).willReturn(List.of());
			given(jobService.getMyPostCnt(any(JobDto.class), eq(Set.of("ROLE_USER")))).willReturn(0);

			mockMvc.perform(get("/api/jobs/my/posts").with(user(loginUser)))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.postings").isArray())
					.andExpect(jsonPath("$.pageMaker").exists());
		}
	}

	@Nested
	@DisplayName("GET /api/jobs/applications/{applicationId}")
	class GetApplication {

		@Test
		@DisplayName("비인증이면 접근 거부된다")
		void getApplication_anonymous_denied() throws Exception {
			mockMvc.perform(get("/api/jobs/applications/1"))
					.andExpect(status().isForbidden());
		}

		@Test
		@DisplayName("정상 조회 시 서비스 결과를 그대로 반환한다")
		void getApplication_success() throws Exception {
			given(jobService.getApplication(1L, "user1")).willReturn(Map.of("category", "USER"));

			mockMvc.perform(get("/api/jobs/applications/1").with(user(loginUser)))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.category").value("USER"));
		}

		@Test
		@DisplayName("권한이 없으면(ForbiddenException) 403이 전파된다")
		void getApplication_forbidden() throws Exception {
			doThrow(new ForbiddenException("지원서를 볼 권한이 없습니다."))
					.when(jobService).getApplication(1L, "user1");

			mockMvc.perform(get("/api/jobs/applications/1").with(user(loginUser)))
					.andExpect(status().isForbidden())
					.andExpect(jsonPath("$.detail").value("지원서를 볼 권한이 없습니다."));
		}
	}

	@Nested
	@DisplayName("GET /api/jobs/my/applications")
	class GetMyApplications {

		@Test
		@DisplayName("비인증이면 접근 거부된다")
		void getMyApplications_anonymous_denied() throws Exception {
			mockMvc.perform(get("/api/jobs/my/applications"))
					.andExpect(status().isForbidden());
		}

		@Test
		@DisplayName("응답의 각 항목 키가 소문자로 변환된다")
		void getMyApplications_keysLowercased() throws Exception {
			given(jobService.getMyApplications(any())).willReturn(List.of(Map.of("APPLICATION_ID", 1L)));
			given(jobService.getMyApplicationsCnt(any())).willReturn(1);

			mockMvc.perform(get("/api/jobs/my/applications").with(user(loginUser)))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.apps[0].application_id").value(1));
		}
	}

	@Nested
	@DisplayName("DELETE /api/jobs/applications/{applicationId}/withdraw")
	class WithdrawApplication {

		@Test
		@DisplayName("비인증이면 접근 거부된다")
		void withdrawApplication_anonymous_denied() throws Exception {
			mockMvc.perform(delete("/api/jobs/applications/1/withdraw"))
					.andExpect(status().isForbidden());
		}

		@Test
		@DisplayName("정상 취소 시 200을 응답한다")
		void withdrawApplication_success() throws Exception {
			mockMvc.perform(delete("/api/jobs/applications/1/withdraw").with(user(loginUser)))
					.andExpect(status().isOk());

			verify(jobService).withdrawApplication(1L, "user1");
		}
	}

	@Nested
	@DisplayName("GET /api/jobs/candidates")
	class GetCandidates {

		@Test
		@DisplayName("비인증이면 접근 거부된다")
		void getCandidates_anonymous_denied() throws Exception {
			mockMvc.perform(get("/api/jobs/candidates").param("post_id", "1"))
					.andExpect(status().isForbidden());
		}

		@Test
		@DisplayName("공고 작성자가 아니면 403")
		void getCandidates_notOwner_forbidden() throws Exception {
			given(jobService.findCompanyIdByPostId(1L)).willReturn("someoneElse");

			mockMvc.perform(get("/api/jobs/candidates").param("post_id", "1").with(user(loginUser)))
					.andExpect(status().isForbidden())
					.andExpect(jsonPath("$.detail").value("해당 정보를 조회할 권한이 없습니다."));
		}

		@Test
		@DisplayName("공고 작성자면 지원자 목록을 반환한다")
		void getCandidates_owner_success() throws Exception {
			given(jobService.findCompanyIdByPostId(1L)).willReturn("user1");
			given(jobService.getApplicationsByPostId(1L)).willReturn(List.of());
			given(jobService.applicationsListCnt(any())).willReturn(0);

			mockMvc.perform(get("/api/jobs/candidates").param("post_id", "1").with(user(loginUser)))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.apps").isArray());
		}
	}

	@Nested
	@DisplayName("GET /api/jobs/my/favorites")
	class GetMyFavorites {

		@Test
		@DisplayName("비인증이면 접근 거부된다")
		void getMyFavorites_anonymous_denied() throws Exception {
			mockMvc.perform(get("/api/jobs/my/favorites"))
					.andExpect(status().isForbidden());
		}

		@Test
		@DisplayName("응답의 각 항목 키가 소문자로 변환된다")
		void getMyFavorites_keysLowercased() throws Exception {
			given(jobService.getMyFavorites(any())).willReturn(List.of(Map.of("POST_ID", 1L)));
			given(jobService.getMyFavoritesCnt(any())).willReturn(1);

			mockMvc.perform(get("/api/jobs/my/favorites").with(user(loginUser)))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.favorites[0].post_id").value(1));
		}
	}
}
