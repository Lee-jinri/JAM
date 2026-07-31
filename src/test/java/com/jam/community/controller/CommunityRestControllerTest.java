package com.jam.community.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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

import com.jam.community.dto.CommunityDetailResponseDto;
import com.jam.community.dto.CommunityDto;
import com.jam.community.dto.CommunityEditRequestDto;
import com.jam.community.dto.CommunityWriteRequestDto;
import com.jam.community.service.CommunityService;
import com.jam.config.MyBatisConfig;
import com.jam.global.exception.ForbiddenException;
import com.jam.global.exception.NotFoundException;
import com.jam.global.security.JwtAuthenticationFilter;
import com.jam.member.dto.MemberDto;

/**
 * CommunityRestController @WebMvcTest 슬라이스 테스트.
 * 실제 SecurityConfig(CSRF, JWT 등) 대신 @PreAuthorize만 동작하는
 * 최소 Security 설정(permitAll)을 따로 구성해서 씀.
 * 인증 사용자는 SecurityMockMvcRequestPostProcessors.user()로 MemberDto를 principal에 직접 꽂음.
 * MyBatisConfig, JwtAuthenticationFilter는 이 테스트에 안 쓰여서 스캔 제외.
 */
@WebMvcTest(
		controllers = CommunityRestController.class,
		excludeFilters = {
				@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class),
				@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = MyBatisConfig.class)
		})
@Import(CommunityRestControllerTest.MethodSecurityTestConfig.class)
class CommunityRestControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private CommunityService comService;

	private MemberDto loginUser;

	@BeforeEach
	void setUp() {
		// @MockBean 기본 reset이 @Nested 클래스에서는 안 걸려서 수동으로 초기화
		reset(comService);

		loginUser = new MemberDto();
		loginUser.setUser_id("user1");
		loginUser.setUser_name("tester");
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
	@DisplayName("GET /api/community/board")
	class GetBoards {

		@Test
		@DisplayName("keyword 없이 호출하면 전체 목록과 pageMaker를 반환한다")
		void getBoards_noKeyword() throws Exception {
			given(comService.getBoard(any(CommunityDto.class))).willReturn(List.of());
			given(comService.listCnt(any(CommunityDto.class))).willReturn(0);

			mockMvc.perform(get("/api/community/board"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.communityList").isArray())
					.andExpect(jsonPath("$.pageMaker").exists());
		}

		@Test
		@DisplayName("keyword가 있으면 sanitize된 값이 서비스로 전달된다")
		void getBoards_withKeyword_sanitized() throws Exception {
			given(comService.getBoard(any(CommunityDto.class))).willReturn(List.of());
			given(comService.listCnt(any(CommunityDto.class))).willReturn(0);

			mockMvc.perform(get("/api/community/board").param("keyword", "  기타  "))
					.andExpect(status().isOk());

			ArgumentCaptor<CommunityDto> captor = ArgumentCaptor.forClass(CommunityDto.class);
			verify(comService).getBoard(captor.capture());
			assertThat(captor.getValue().getKeyword()).isEqualTo("기타");
		}

		@Test
		@DisplayName("keyword가 공백만 있으면 keyword가 세팅되지 않는다")
		void getBoards_blankKeyword_notSet() throws Exception {
			given(comService.getBoard(any(CommunityDto.class))).willReturn(List.of());
			given(comService.listCnt(any(CommunityDto.class))).willReturn(0);

			mockMvc.perform(get("/api/community/board").param("keyword", "   "))
					.andExpect(status().isOk());

			ArgumentCaptor<CommunityDto> captor = ArgumentCaptor.forClass(CommunityDto.class);
			verify(comService).getBoard(captor.capture());
			assertThat(captor.getValue().getKeyword()).isEmpty();
		}

		@Test
		@DisplayName("로그인 사용자면 user_id가 세팅된다")
		void getBoards_loggedIn_setsUserId() throws Exception {
			given(comService.getBoard(any(CommunityDto.class))).willReturn(List.of());
			given(comService.listCnt(any(CommunityDto.class))).willReturn(0);

			mockMvc.perform(get("/api/community/board").with(user(loginUser)))
					.andExpect(status().isOk());

			ArgumentCaptor<CommunityDto> captor = ArgumentCaptor.forClass(CommunityDto.class);
			verify(comService).getBoard(captor.capture());
			assertThat(captor.getValue().getUser_id()).isEqualTo("user1");
		}

		@Test
		@DisplayName("비로그인이면 user_id가 세팅되지 않는다")
		void getBoards_anonymous_noUserId() throws Exception {
			given(comService.getBoard(any(CommunityDto.class))).willReturn(List.of());
			given(comService.listCnt(any(CommunityDto.class))).willReturn(0);

			mockMvc.perform(get("/api/community/board"))
					.andExpect(status().isOk());

			ArgumentCaptor<CommunityDto> captor = ArgumentCaptor.forClass(CommunityDto.class);
			verify(comService).getBoard(captor.capture());
			assertThat(captor.getValue().getUser_id()).isNull();
		}

		@Test
		@DisplayName("total 파라미터가 0 이하이면 listCnt를 다시 호출해서 total을 구한다")
		void getBoards_totalZero_callsListCnt() throws Exception {
			given(comService.getBoard(any(CommunityDto.class))).willReturn(List.of());
			given(comService.listCnt(any(CommunityDto.class))).willReturn(42);

			mockMvc.perform(get("/api/community/board"))
					.andExpect(status().isOk());

			verify(comService).listCnt(any(CommunityDto.class));
		}

		@Test
		@DisplayName("total 파라미터가 명시적으로 넘어오면 listCnt를 다시 호출하지 않는다")
		void getBoards_totalGiven_skipsListCnt() throws Exception {
			given(comService.getBoard(any(CommunityDto.class))).willReturn(List.of());

			mockMvc.perform(get("/api/community/board").param("total", "100"))
					.andExpect(status().isOk());

			verify(comService, never()).listCnt(any());
		}

		@Test
		@DisplayName("pageNum 파라미터 생략 시 기본값 1이 적용된다")
		void getBoards_defaultPageNum() throws Exception {
			given(comService.getBoard(any(CommunityDto.class))).willReturn(List.of());
			given(comService.listCnt(any(CommunityDto.class))).willReturn(0);

			mockMvc.perform(get("/api/community/board"))
					.andExpect(status().isOk());

			ArgumentCaptor<CommunityDto> captor = ArgumentCaptor.forClass(CommunityDto.class);
			verify(comService).getBoard(captor.capture());
			assertThat(captor.getValue().getPageNum()).isEqualTo(1);
		}
	}

	@Nested
	@DisplayName("GET /api/community/board/popular")
	class GetPopularBoard {

		@Test
		@DisplayName("서비스 결과가 popularList로 반환된다")
		void getPopularBoard_returnsResult() throws Exception {
			CommunityDto post = new CommunityDto();
			post.setTitle("인기글");
			given(comService.getPopularBoard()).willReturn(List.of(post));

			mockMvc.perform(get("/api/community/board/popular"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.popularList[0].title").value("인기글"));
		}

		@Test
		@DisplayName("결과가 없으면 빈 리스트를 반환한다")
		void getPopularBoard_empty() throws Exception {
			given(comService.getPopularBoard()).willReturn(List.of());

			mockMvc.perform(get("/api/community/board/popular"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.popularList").isArray())
					.andExpect(jsonPath("$.popularList").isEmpty());
		}
	}

	@Nested
	@DisplayName("GET /api/community/post/{postId}")
	class GetBoardDetail {

		private CommunityDetailResponseDto detail;

		@BeforeEach
		void setUpDetail() {
			detail = CommunityDetailResponseDto.builder()
					.postId(1L)
					.title("제목")
					.content("내용")
					.viewCount(3)
					.createdAt(LocalDateTime.now())
					.userId("author1")
					.userName("작성자")
					.build();
		}

		@Test
		@DisplayName("정상 조회 시 detail을 반환하고 조회수를 증가시킨다")
		void getBoardDetail_success() throws Exception {
			given(comService.getPost(1L)).willReturn(detail);

			mockMvc.perform(get("/api/community/post/1"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.detail.postId").value(1))
					.andExpect(jsonPath("$.detail.title").value("제목"));

			verify(comService).incrementReadCnt(1L);
		}

		@Test
		@DisplayName("로그인 사용자가 작성자 본인이면 isAuthor=true")
		void getBoardDetail_isAuthor_true() throws Exception {
			given(comService.getPost(1L)).willReturn(detail);
			loginUser.setUser_id("author1");

			mockMvc.perform(get("/api/community/post/1").with(user(loginUser)))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.isAuthor").value(true));
		}

		@Test
		@DisplayName("로그인 사용자가 작성자가 아니면 isAuthor=false")
		void getBoardDetail_isAuthor_false() throws Exception {
			given(comService.getPost(1L)).willReturn(detail);
			// loginUser.user_id = "user1" (작성자 author1과 다름)

			mockMvc.perform(get("/api/community/post/1").with(user(loginUser)))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.isAuthor").value(false));
		}

		@Test
		@DisplayName("비로그인이면 isAuthor=false")
		void getBoardDetail_anonymous_isAuthorFalse() throws Exception {
			given(comService.getPost(1L)).willReturn(detail);

			mockMvc.perform(get("/api/community/post/1"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.isAuthor").value(false));
		}

		@Test
		@DisplayName("존재하지 않는 글이면 예외가 GlobalExceptionHandler로 전파된다")
		void getBoardDetail_notFound() throws Exception {
			given(comService.getPost(999L)).willThrow(new NotFoundException("존재하지 않는 게시글입니다."));

			mockMvc.perform(get("/api/community/post/999"))
					.andExpect(status().isNotFound())
					.andExpect(jsonPath("$.detail").value("존재하지 않는 게시글입니다."));
		}
	}

	@Nested
	@DisplayName("GET /api/community/posts/{post_id}/edit-data")
	class GetEditData {

		@Test
		@DisplayName("비인증이면 접근 거부된다")
		void getEditData_anonymous_denied() throws Exception {
			mockMvc.perform(get("/api/community/posts/1/edit-data"))
					.andExpect(status().isForbidden());
		}

		@Test
		@DisplayName("인증된 사용자면 getPostForEdit 결과를 반환한다")
		void getEditData_success() throws Exception {
			Map<String, Object> data = Map.of("post", Map.of("title", "제목"), "files", List.of());
			given(comService.getPostForEdit(1L)).willReturn(data);

			mockMvc.perform(get("/api/community/posts/1/edit-data").with(user(loginUser)))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.post.title").value("제목"));
		}
	}

	@Nested
	@DisplayName("POST /api/community/post")
	class WritePost {

		@Test
		@DisplayName("비인증이면 접근 거부된다")
		void writePost_anonymous_denied() throws Exception {
			mockMvc.perform(post("/api/community/post")
							.contentType(MediaType.APPLICATION_JSON)
							.content("{\"title\":\"제목\",\"content\":\"내용\"}"))
					.andExpect(status().isForbidden());
		}

		@Test
		@DisplayName("요청 바디가 JSON null이면 500 (Spring이 @RequestBody null을 HttpMessageNotReadableException으로 먼저 가로채서, 컨트롤러의 requestDto==null 체크는 이 경로로 도달 불가)")
		void writePost_nullBody() throws Exception {
			mockMvc.perform(post("/api/community/post")
							.with(user(loginUser))
							.contentType(MediaType.APPLICATION_JSON)
							.content("null"))
					.andExpect(status().isInternalServerError());
		}

		@Test
		@DisplayName("title이 없으면 400")
		void writePost_noTitle() throws Exception {
			mockMvc.perform(post("/api/community/post")
							.with(user(loginUser))
							.contentType(MediaType.APPLICATION_JSON)
							.content("{\"content\":\"내용\"}"))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.detail").value("제목을 입력하세요."));
		}

		@Test
		@DisplayName("content가 없으면 400")
		void writePost_noContent() throws Exception {
			mockMvc.perform(post("/api/community/post")
							.with(user(loginUser))
							.contentType(MediaType.APPLICATION_JSON)
							.content("{\"title\":\"제목\"}"))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.detail").value("내용을 입력하세요."));
		}

		@Test
		@DisplayName("title/content가 sanitize된 값으로 서비스에 전달된다")
		void writePost_sanitizesInput() throws Exception {
			given(comService.writePost(eq("user1"), any(CommunityWriteRequestDto.class))).willReturn(1L);

			mockMvc.perform(post("/api/community/post")
							.with(user(loginUser))
							.contentType(MediaType.APPLICATION_JSON)
							.content("{\"title\":\"<b>제목</b>\",\"content\":\"<script>alert(1)</script>내용\"}"))
					.andExpect(status().isOk());

			ArgumentCaptor<CommunityWriteRequestDto> captor = ArgumentCaptor.forClass(CommunityWriteRequestDto.class);
			verify(comService).writePost(eq("user1"), captor.capture());
			assertThat(captor.getValue().getTitle()).doesNotContain("<b>");
			assertThat(captor.getValue().getContent()).doesNotContain("<script>");
		}

		@Test
		@DisplayName("정상 작성 시 postId를 응답한다")
		void writePost_success() throws Exception {
			given(comService.writePost(eq("user1"), any(CommunityWriteRequestDto.class))).willReturn(10L);

			mockMvc.perform(post("/api/community/post")
							.with(user(loginUser))
							.contentType(MediaType.APPLICATION_JSON)
							.content("{\"title\":\"제목\",\"content\":\"내용\"}"))
					.andExpect(status().isOk())
					.andExpect(content().string("10"));
		}
	}

	@Nested
	@DisplayName("PUT /api/community/post/{postId}")
	class EditBoard {

		@Test
		@DisplayName("비인증이면 접근 거부된다")
		void editBoard_anonymous_denied() throws Exception {
			mockMvc.perform(put("/api/community/post/1")
							.contentType(MediaType.APPLICATION_JSON)
							.content("{\"title\":\"제목\",\"content\":\"내용\"}"))
					.andExpect(status().isForbidden());
		}

		@Test
		@DisplayName("요청 바디가 JSON null이면 500 (Spring이 @RequestBody null을 HttpMessageNotReadableException으로 먼저 가로채서, 컨트롤러의 requestDto==null 체크는 이 경로로 도달 불가)")
		void editBoard_nullBody() throws Exception {
			mockMvc.perform(put("/api/community/post/1")
							.with(user(loginUser))
							.contentType(MediaType.APPLICATION_JSON)
							.content("null"))
					.andExpect(status().isInternalServerError());
		}

		@Test
		@DisplayName("title이 없으면 400")
		void editBoard_noTitle() throws Exception {
			mockMvc.perform(put("/api/community/post/1")
							.with(user(loginUser))
							.contentType(MediaType.APPLICATION_JSON)
							.content("{\"content\":\"내용\"}"))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.detail").value("제목을 입력하세요."));
		}

		@Test
		@DisplayName("content가 없으면 400")
		void editBoard_noContent() throws Exception {
			mockMvc.perform(put("/api/community/post/1")
							.with(user(loginUser))
							.contentType(MediaType.APPLICATION_JSON)
							.content("{\"title\":\"제목\"}"))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.detail").value("내용을 입력하세요."));
		}

		@Test
		@DisplayName("title/content가 sanitize된 값으로 서비스에 전달된다")
		void editBoard_sanitizesInput() throws Exception {
			mockMvc.perform(put("/api/community/post/1")
							.with(user(loginUser))
							.contentType(MediaType.APPLICATION_JSON)
							.content("{\"title\":\"<b>제목</b>\",\"content\":\"<script>alert(1)</script>내용\"}"))
					.andExpect(status().isOk());

			ArgumentCaptor<CommunityEditRequestDto> captor = ArgumentCaptor.forClass(CommunityEditRequestDto.class);
			verify(comService).editPost(captor.capture(), eq(1L), eq("user1"));
			assertThat(captor.getValue().getTitle()).doesNotContain("<b>");
			assertThat(captor.getValue().getContent()).doesNotContain("<script>");
		}

		@Test
		@DisplayName("정상 수정 시 postId를 응답한다")
		void editBoard_success() throws Exception {
			mockMvc.perform(put("/api/community/post/1")
							.with(user(loginUser))
							.contentType(MediaType.APPLICATION_JSON)
							.content("{\"title\":\"제목\",\"content\":\"내용\"}"))
					.andExpect(status().isOk())
					.andExpect(content().string("1"));

			verify(comService).editPost(any(CommunityEditRequestDto.class), eq(1L), eq("user1"));
		}

		@Test
		@DisplayName("본인 글이 아니면 403이 전파된다")
		void editBoard_notOwner_forbidden() throws Exception {
			doThrow(new ForbiddenException("본인이 작성한 글만 수정할 수 있습니다."))
					.when(comService).editPost(any(CommunityEditRequestDto.class), eq(1L), eq("user1"));

			mockMvc.perform(put("/api/community/post/1")
							.with(user(loginUser))
							.contentType(MediaType.APPLICATION_JSON)
							.content("{\"title\":\"제목\",\"content\":\"내용\"}"))
					.andExpect(status().isForbidden())
					.andExpect(jsonPath("$.detail").value("본인이 작성한 글만 수정할 수 있습니다."));
		}
	}

	@Nested
	@DisplayName("DELETE /api/community/post/{postId}")
	class PostDelete {

		@Test
		@DisplayName("비인증이면 접근 거부된다")
		void postDelete_anonymous_denied() throws Exception {
			mockMvc.perform(delete("/api/community/post/1"))
					.andExpect(status().isForbidden());
		}

		@Test
		@DisplayName("정상 삭제 시 200을 응답한다")
		void postDelete_success() throws Exception {
			mockMvc.perform(delete("/api/community/post/1").with(user(loginUser)))
					.andExpect(status().isOk());

			verify(comService).deletePost(1L, "user1");
		}

		@Test
		@DisplayName("본인 글이 아니면 403이 전파된다")
		void postDelete_notOwner_forbidden() throws Exception {
			doThrow(new ForbiddenException("본인이 작성한 글만 삭제할 수 있습니다."))
					.when(comService).deletePost(1L, "user1");

			mockMvc.perform(delete("/api/community/post/1").with(user(loginUser)))
					.andExpect(status().isForbidden())
					.andExpect(jsonPath("$.detail").value("본인이 작성한 글만 삭제할 수 있습니다."));
		}
	}

	@Nested
	@DisplayName("GET /api/community/my/posts")
	class GetMyPosts {

		@Test
		@DisplayName("비인증이면 접근 거부된다")
		void getMyPosts_anonymous_denied() throws Exception {
			mockMvc.perform(get("/api/community/my/posts"))
					.andExpect(status().isForbidden());
		}

		@Test
		@DisplayName("pageNum/amount 기본값(1/10)이 적용된다")
		void getMyPosts_defaults() throws Exception {
			given(comService.getMyPosts(eq("user1"), any(), eq(1), eq(10))).willReturn(Map.of());

			mockMvc.perform(get("/api/community/my/posts").with(user(loginUser)))
					.andExpect(status().isOk());

			verify(comService).getMyPosts(eq("user1"), any(), eq(1), eq(10));
		}

		@Test
		@DisplayName("keyword가 sanitize되어 전달된다")
		void getMyPosts_sanitizesKeyword() throws Exception {
			given(comService.getMyPosts(eq("user1"), eq("기타"), anyInt(), anyInt())).willReturn(Map.of());

			mockMvc.perform(get("/api/community/my/posts").with(user(loginUser)).param("keyword", "  기타  "))
					.andExpect(status().isOk());

			verify(comService).getMyPosts(eq("user1"), eq("기타"), anyInt(), anyInt());
		}

		@Test
		@DisplayName("서비스 결과가 그대로 응답 바디로 나간다")
		void getMyPosts_returnsServiceResult() throws Exception {
			given(comService.getMyPosts(eq("user1"), any(), anyInt(), anyInt()))
					.willReturn(Map.of("posts", List.of(), "pageMaker", Map.of("total", 0)));

			mockMvc.perform(get("/api/community/my/posts").with(user(loginUser)))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.posts").isArray());
		}
	}

	@Nested
	@DisplayName("DELETE /api/community/my/posts")
	class DeletePosts {

		@Test
		@DisplayName("비인증이면 접근 거부된다")
		void deletePosts_anonymous_denied() throws Exception {
			mockMvc.perform(delete("/api/community/my/posts")
							.contentType(MediaType.APPLICATION_JSON)
							.content("[1,2]"))
					.andExpect(status().isForbidden());
		}

		@Test
		@DisplayName("정상 삭제 시 200을 응답한다")
		void deletePosts_success() throws Exception {
			mockMvc.perform(delete("/api/community/my/posts")
							.with(user(loginUser))
							.contentType(MediaType.APPLICATION_JSON)
							.content("[1,2,3]"))
					.andExpect(status().isOk());

			verify(comService).deleteMyPosts(eq("user1"), eq(List.of(1L, 2L, 3L)));
		}

		@Test
		@DisplayName("빈 배열이어도 컨트롤러 검증 없이 그대로 서비스로 위임된다")
		void deletePosts_emptyList_delegates() throws Exception {
			mockMvc.perform(delete("/api/community/my/posts")
							.with(user(loginUser))
							.contentType(MediaType.APPLICATION_JSON)
							.content("[]"))
					.andExpect(status().isOk());

			verify(comService).deleteMyPosts(eq("user1"), eq(List.of()));
		}

		@Test
		@DisplayName("본인 글이 아닌 게 섞여 있으면 403이 전파된다")
		void deletePosts_notOwner_forbidden() throws Exception {
			doThrow(new ForbiddenException("본인이 작성한 글만 삭제할 수 있습니다."))
					.when(comService).deleteMyPosts(eq("user1"), any());

			mockMvc.perform(delete("/api/community/my/posts")
							.with(user(loginUser))
							.contentType(MediaType.APPLICATION_JSON)
							.content("[1,2]"))
					.andExpect(status().isForbidden());
		}
	}

	@Nested
	@DisplayName("GET /api/community/my/favorites")
	class GetFavorites {

		@Test
		@DisplayName("비인증이면 접근 거부된다")
		void getFavorites_anonymous_denied() throws Exception {
			mockMvc.perform(get("/api/community/my/favorites"))
					.andExpect(status().isForbidden());
		}

		@Test
		@DisplayName("정상 조회 시 favorites/pageMaker를 반환한다")
		void getFavorites_success() throws Exception {
			given(comService.getFavorites(any(CommunityDto.class))).willReturn(List.of());
			given(comService.favoritesListCnt(any(CommunityDto.class))).willReturn(0);

			mockMvc.perform(get("/api/community/my/favorites").with(user(loginUser)))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.favorites").isArray())
					.andExpect(jsonPath("$.pageMaker").exists());
		}

		@Test
		@DisplayName("pageNum 기본값 1이 적용된다")
		void getFavorites_defaultPageNum() throws Exception {
			given(comService.getFavorites(any(CommunityDto.class))).willReturn(List.of());
			given(comService.favoritesListCnt(any(CommunityDto.class))).willReturn(0);

			mockMvc.perform(get("/api/community/my/favorites").with(user(loginUser)))
					.andExpect(status().isOk());

			ArgumentCaptor<CommunityDto> captor = ArgumentCaptor.forClass(CommunityDto.class);
			verify(comService).getFavorites(captor.capture());
			assertThat(captor.getValue().getPageNum()).isEqualTo(1);
		}
	}
}
