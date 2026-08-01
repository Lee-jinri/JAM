package com.jam.fleaMarket.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import com.jam.config.MyBatisConfig;
import com.jam.file.dto.ImageFileDto;
import com.jam.fleaMarket.dto.FleaMarketDto;
import com.jam.fleaMarket.service.FleaMarketService;
import com.jam.global.exception.ForbiddenException;
import com.jam.global.security.JwtAuthenticationFilter;
import com.jam.member.dto.MemberDto;

/**
 * FleaMarketRestController @WebMvcTest 슬라이스 테스트. CommunityRestControllerTest와 같은 패턴:
 * 실제 SecurityConfig 대신 @PreAuthorize만 동작하는 최소 Security 설정(permitAll)을 따로 구성해서 씀.
 */
@WebMvcTest(
		controllers = FleaMarketRestController.class,
		excludeFilters = {
				@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class),
				@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = MyBatisConfig.class)
		})
@Import(FleaMarketRestControllerTest.MethodSecurityTestConfig.class)
class FleaMarketRestControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private FleaMarketService fleaService;

	private MemberDto loginUser;

	@BeforeEach
	void setUp() {
		reset(fleaService);

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
	@DisplayName("GET /api/fleaMarket/board")
	class GetBoards {

		@Test
		@DisplayName("정상 조회 시 fleaMarketList와 pageMaker를 반환한다")
		void getBoards_success() throws Exception {
			given(fleaService.getBoard(any(FleaMarketDto.class))).willReturn(List.of());
			given(fleaService.listCnt(any(FleaMarketDto.class))).willReturn(0);

			mockMvc.perform(get("/api/fleaMarket/board"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.fleaMarketList").isArray())
					.andExpect(jsonPath("$.pageMaker").exists());
		}

		@Test
		@DisplayName("keyword가 있으면 sanitize된 값이 서비스로 전달된다")
		void getBoards_withKeyword_sanitized() throws Exception {
			given(fleaService.getBoard(any(FleaMarketDto.class))).willReturn(List.of());
			given(fleaService.listCnt(any(FleaMarketDto.class))).willReturn(0);

			mockMvc.perform(get("/api/fleaMarket/board").param("keyword", "  기타  "))
					.andExpect(status().isOk());

			ArgumentCaptor<FleaMarketDto> captor = ArgumentCaptor.forClass(FleaMarketDto.class);
			verify(fleaService).getBoard(captor.capture());
			assertThat(captor.getValue().getKeyword()).isEqualTo("기타");
		}

		@Test
		@DisplayName("로그인 사용자면 user_id가 세팅된다")
		void getBoards_loggedIn_setsUserId() throws Exception {
			given(fleaService.getBoard(any(FleaMarketDto.class))).willReturn(List.of());
			given(fleaService.listCnt(any(FleaMarketDto.class))).willReturn(0);

			mockMvc.perform(get("/api/fleaMarket/board").with(user(loginUser)))
					.andExpect(status().isOk());

			ArgumentCaptor<FleaMarketDto> captor = ArgumentCaptor.forClass(FleaMarketDto.class);
			verify(fleaService).getBoard(captor.capture());
			assertThat(captor.getValue().getUser_id()).isEqualTo("user1");
		}

		@Test
		@DisplayName("비로그인이면 user_id가 세팅되지 않는다")
		void getBoards_anonymous_noUserId() throws Exception {
			given(fleaService.getBoard(any(FleaMarketDto.class))).willReturn(List.of());
			given(fleaService.listCnt(any(FleaMarketDto.class))).willReturn(0);

			mockMvc.perform(get("/api/fleaMarket/board"))
					.andExpect(status().isOk());

			ArgumentCaptor<FleaMarketDto> captor = ArgumentCaptor.forClass(FleaMarketDto.class);
			verify(fleaService).getBoard(captor.capture());
			assertThat(captor.getValue().getUser_id()).isNull();
		}

		@Test
		@DisplayName("pageNum 파라미터 생략 시 기본값 1이 적용된다")
		void getBoards_defaultPageNum() throws Exception {
			given(fleaService.getBoard(any(FleaMarketDto.class))).willReturn(List.of());
			given(fleaService.listCnt(any(FleaMarketDto.class))).willReturn(0);

			mockMvc.perform(get("/api/fleaMarket/board"))
					.andExpect(status().isOk());

			ArgumentCaptor<FleaMarketDto> captor = ArgumentCaptor.forClass(FleaMarketDto.class);
			verify(fleaService).getBoard(captor.capture());
			assertThat(captor.getValue().getPageNum()).isEqualTo(1);
		}
	}

	@Nested
	@DisplayName("GET /api/fleaMarket/post/{post_id}")
	class GetBoardDetail {

		private FleaMarketDto post;

		@BeforeEach
		void setUpPost() {
			post = new FleaMarketDto();
			post.setPost_id(1L);
			post.setTitle("제목");
			post.setUser_id("author1");
		}

		@Test
		@DisplayName("정상 조회 시 post/images를 반환하고 조회수를 증가시킨다")
		void getBoardDetail_success() throws Exception {
			given(fleaService.getPostDetail(any(FleaMarketDto.class))).willReturn(post);
			given(fleaService.findFleaImagesByPostId(1L)).willReturn(List.of());

			mockMvc.perform(get("/api/fleaMarket/post/1"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.post.title").value("제목"))
					.andExpect(jsonPath("$.images").isArray());

			verify(fleaService).incrementReadCnt(1L);
		}

		@Test
		@DisplayName("로그인 사용자가 작성자 본인이면 isAuthor=true")
		void getBoardDetail_isAuthor_true() throws Exception {
			given(fleaService.getPostDetail(any(FleaMarketDto.class))).willReturn(post);
			given(fleaService.findFleaImagesByPostId(1L)).willReturn(List.of());
			loginUser.setUser_id("author1");

			mockMvc.perform(get("/api/fleaMarket/post/1").with(user(loginUser)))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.isAuthor").value(true));
		}

		@Test
		@DisplayName("로그인 사용자가 작성자가 아니면 isAuthor=false")
		void getBoardDetail_isAuthor_false() throws Exception {
			given(fleaService.getPostDetail(any(FleaMarketDto.class))).willReturn(post);
			given(fleaService.findFleaImagesByPostId(1L)).willReturn(List.of());
			// loginUser.user_id = "user1" (작성자 author1과 다름)

			mockMvc.perform(get("/api/fleaMarket/post/1").with(user(loginUser)))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.isAuthor").value(false));
		}

		@Test
		@DisplayName("비로그인이면 isAuthor=false")
		void getBoardDetail_anonymous_isAuthorFalse() throws Exception {
			given(fleaService.getPostDetail(any(FleaMarketDto.class))).willReturn(post);
			given(fleaService.findFleaImagesByPostId(1L)).willReturn(List.of());

			mockMvc.perform(get("/api/fleaMarket/post/1"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.isAuthor").value(false));
		}
	}

	@Nested
	@DisplayName("POST /api/fleaMarket/post")
	class WriteBoard {

		private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder validRequest() {
			return multipart("/api/fleaMarket/post")
					.file(new MockMultipartFile("images", "a.jpg", "image/jpeg", "x".getBytes()))
					.param("title", "제목")
					.param("content", "내용")
					.param("price", "10000")
					.param("category_id", "1");
		}

		@Test
		@DisplayName("비인증이면 접근 거부된다")
		void writeBoard_anonymous_denied() throws Exception {
			mockMvc.perform(validRequest())
					.andExpect(status().isForbidden());
		}

		@Test
		@DisplayName("images 파트가 아예 없으면 500 (Spring이 List<MultipartFile> required 파라미터를 MissingServletRequestPartException으로 먼저 가로채서,"
				+ " 컨트롤러의 images==null||isEmpty() 체크는 이 경로로 도달 불가 - List<MultipartFile>은 파일이 1개 이상 있으면 비어있지 않은 리스트를,"
				+ " 하나도 없으면 null을 반환하는 구조라 '존재하지만 비어있는 리스트'는 애초에 나올 수 없음."
				+ " 참고로 이 예외는 GlobalExceptionHandler에 매핑되어 있지 않아 400이 아니라 500으로 응답됨)")
		void writeBoard_noImagesPart() throws Exception {
			mockMvc.perform(multipart("/api/fleaMarket/post")
							.param("title", "제목")
							.param("content", "내용")
							.param("price", "10000")
							.param("category_id", "1")
							.with(user(loginUser)))
					.andExpect(status().isInternalServerError());
		}

		@Test
		@DisplayName("title이 비어있으면 400")
		void writeBoard_blankTitle() throws Exception {
			mockMvc.perform(multipart("/api/fleaMarket/post")
							.file(new MockMultipartFile("images", "a.jpg", "image/jpeg", "x".getBytes()))
							.param("title", "")
							.param("content", "내용")
							.param("price", "10000")
							.param("category_id", "1")
							.with(user(loginUser)))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.detail").value("제목을 입력하세요."));
		}

		@Test
		@DisplayName("content가 비어있으면 400")
		void writeBoard_blankContent() throws Exception {
			mockMvc.perform(multipart("/api/fleaMarket/post")
							.file(new MockMultipartFile("images", "a.jpg", "image/jpeg", "x".getBytes()))
							.param("title", "제목")
							.param("content", "")
							.param("price", "10000")
							.param("category_id", "1")
							.with(user(loginUser)))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.detail").value("설명을 입력하세요."));
		}

		@Test
		@DisplayName("이미지가 6장이면 400")
		void writeBoard_tooManyImages() throws Exception {
			var request = multipart("/api/fleaMarket/post");
			for (int i = 0; i < 6; i++) {
				request = request.file(new MockMultipartFile("images", "img" + i + ".jpg", "image/jpeg", "x".getBytes()));
			}

			mockMvc.perform(request
							.param("title", "제목")
							.param("content", "내용")
							.param("price", "10000")
							.param("category_id", "1")
							.with(user(loginUser)))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.detail").value("이미지는 최대 5장까지 등록할 수 있습니다."));
		}

		@Test
		@DisplayName("가격이 0이면 400")
		void writeBoard_zeroPrice() throws Exception {
			mockMvc.perform(multipart("/api/fleaMarket/post")
							.file(new MockMultipartFile("images", "a.jpg", "image/jpeg", "x".getBytes()))
							.param("title", "제목")
							.param("content", "내용")
							.param("price", "0")
							.param("category_id", "1")
							.with(user(loginUser)))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.detail").value("가격은 0원보다 커야 합니다."));
		}

		@Test
		@DisplayName("title/content가 sanitize된 값으로 서비스에 전달되고, 정상 작성 시 postId를 응답한다")
		void writeBoard_success() throws Exception {
			given(fleaService.writePost(any(FleaMarketDto.class), anyList())).willReturn(10L);

			mockMvc.perform(multipart("/api/fleaMarket/post")
							.file(new MockMultipartFile("images", "a.jpg", "image/jpeg", "x".getBytes()))
							.param("title", "<b>제목</b>")
							.param("content", "<script>alert(1)</script>내용")
							.param("price", "10000")
							.param("category_id", "1")
							.with(user(loginUser)))
					.andExpect(status().isOk())
					.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string("10"));

			ArgumentCaptor<FleaMarketDto> captor = ArgumentCaptor.forClass(FleaMarketDto.class);
			verify(fleaService).writePost(captor.capture(), anyList());
			assertThat(captor.getValue().getTitle()).doesNotContain("<b>");
			assertThat(captor.getValue().getContent()).doesNotContain("<script>");
			assertThat(captor.getValue().getUser_id()).isEqualTo("user1");
		}
	}

	@Nested
	@DisplayName("GET /api/fleaMarket/posts/{post_id}/edit-data")
	class GetEditData {

		@Test
		@DisplayName("비인증이면 접근 거부된다")
		void getEditData_anonymous_denied() throws Exception {
			mockMvc.perform(get("/api/fleaMarket/posts/1/edit-data"))
					.andExpect(status().isForbidden());
		}

		@Test
		@DisplayName("존재하지 않는 글이면 404")
		void getEditData_notFound() throws Exception {
			given(fleaService.getPostForEdit(1L)).willReturn(null);

			mockMvc.perform(get("/api/fleaMarket/posts/1/edit-data").with(user(loginUser)))
					.andExpect(status().isNotFound())
					.andExpect(jsonPath("$.detail").value("존재하지 않는 게시글입니다."));
		}

		@Test
		@DisplayName("인증된 사용자면 post/images를 반환한다")
		void getEditData_success() throws Exception {
			FleaMarketDto post = new FleaMarketDto();
			post.setTitle("제목");
			given(fleaService.getPostForEdit(1L)).willReturn(post);
			given(fleaService.getImages(1L)).willReturn(List.of(new ImageFileDto()));

			mockMvc.perform(get("/api/fleaMarket/posts/1/edit-data").with(user(loginUser)))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.post.title").value("제목"))
					.andExpect(jsonPath("$.images").isArray());
		}
	}

	@Nested
	@DisplayName("POST /api/fleaMarket/post/update")
	class EditBoard {

		private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder validRequest() {
			return multipart("/api/fleaMarket/post/update")
					.param("postId", "1")
					.param("title", "제목")
					.param("content", "내용")
					.param("price", "10000")
					.param("category_id", "1")
					.param("thumbnailId", "5");
		}

		@Test
		@DisplayName("비인증이면 접근 거부된다")
		void editBoard_anonymous_denied() throws Exception {
			mockMvc.perform(validRequest())
					.andExpect(status().isForbidden());
		}

		@Test
		@DisplayName("title이 비어있으면 400")
		void editBoard_blankTitle() throws Exception {
			mockMvc.perform(multipart("/api/fleaMarket/post/update")
							.param("postId", "1")
							.param("title", "")
							.param("content", "내용")
							.param("price", "10000")
							.param("category_id", "1")
							.param("thumbnailId", "5")
							.with(user(loginUser)))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.detail").value("제목을 입력하세요."));
		}

		@Test
		@DisplayName("thumbnailId와 thumbnailName이 둘 다 없으면 400")
		void editBoard_noThumbnail() throws Exception {
			mockMvc.perform(multipart("/api/fleaMarket/post/update")
							.param("postId", "1")
							.param("title", "제목")
							.param("content", "내용")
							.param("price", "10000")
							.param("category_id", "1")
							.with(user(loginUser)))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.detail").value("썸네일이 설정되지 않았습니다."));
		}

		@Test
		@DisplayName("가격이 0이면 400")
		void editBoard_zeroPrice() throws Exception {
			mockMvc.perform(multipart("/api/fleaMarket/post/update")
							.param("postId", "1")
							.param("title", "제목")
							.param("content", "내용")
							.param("price", "0")
							.param("category_id", "1")
							.param("thumbnailId", "5")
							.with(user(loginUser)))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.detail").value("가격은 0원보다 커야 합니다."));
		}

		@Test
		@DisplayName("정상 수정 시 postId를 응답한다")
		void editBoard_success() throws Exception {
			mockMvc.perform(validRequest().with(user(loginUser)))
					.andExpect(status().isOk())
					.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string("1"));

			verify(fleaService).editPost(any(FleaMarketDto.class), eq(null), eq(null), eq(5L), eq(null));
		}

		@Test
		@DisplayName("본인 글이 아니면 403이 전파된다")
		void editBoard_notOwner_forbidden() throws Exception {
			doThrow(new ForbiddenException("수정 권한이 없습니다."))
					.when(fleaService).editPost(any(FleaMarketDto.class), any(), any(), any(), any());

			mockMvc.perform(validRequest().with(user(loginUser)))
					.andExpect(status().isForbidden())
					.andExpect(jsonPath("$.detail").value("수정 권한이 없습니다."));
		}
	}

	@Nested
	@DisplayName("DELETE /api/fleaMarket/post/{post_id}")
	class BoardDelete {

		@Test
		@DisplayName("비인증이면 접근 거부된다")
		void boardDelete_anonymous_denied() throws Exception {
			mockMvc.perform(delete("/api/fleaMarket/post/1"))
					.andExpect(status().isForbidden());
		}

		@Test
		@DisplayName("정상 삭제 시 200을 응답한다")
		void boardDelete_success() throws Exception {
			mockMvc.perform(delete("/api/fleaMarket/post/1").with(user(loginUser)))
					.andExpect(status().isOk());

			verify(fleaService).deletePost(1L, "user1");
		}

		@Test
		@DisplayName("본인 글이 아니면 403이 전파된다")
		void boardDelete_notOwner_forbidden() throws Exception {
			doThrow(new ForbiddenException("게시물을 삭제할 권한이 없습니다."))
					.when(fleaService).deletePost(1L, "user1");

			mockMvc.perform(delete("/api/fleaMarket/post/1").with(user(loginUser)))
					.andExpect(status().isForbidden())
					.andExpect(jsonPath("$.detail").value("게시물을 삭제할 권한이 없습니다."));
		}
	}

	@Nested
	@DisplayName("GET /api/fleaMarket/my/store")
	class GetMyStore {

		@Test
		@DisplayName("비인증이면 접근 거부된다")
		void getMyStore_anonymous_denied() throws Exception {
			mockMvc.perform(get("/api/fleaMarket/my/store"))
					.andExpect(status().isForbidden());
		}

		@Test
		@DisplayName("정상 조회 시 fleaMarketList/pageMaker를 반환하고 user_id가 세팅된다")
		void getMyStore_success() throws Exception {
			given(fleaService.getMyStore(any(FleaMarketDto.class))).willReturn(List.of());
			given(fleaService.getMyStoreCnt(any(FleaMarketDto.class))).willReturn(0);

			mockMvc.perform(get("/api/fleaMarket/my/store").with(user(loginUser)))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.fleaMarketList").isArray())
					.andExpect(jsonPath("$.pageMaker").exists());

			ArgumentCaptor<FleaMarketDto> captor = ArgumentCaptor.forClass(FleaMarketDto.class);
			verify(fleaService).getMyStore(captor.capture());
			assertThat(captor.getValue().getUser_id()).isEqualTo("user1");
		}
	}

	@Nested
	@DisplayName("GET /api/fleaMarket/my/favorites")
	class Favorites {

		@Test
		@DisplayName("비인증이면 접근 거부된다")
		void favorites_anonymous_denied() throws Exception {
			mockMvc.perform(get("/api/fleaMarket/my/favorites"))
					.andExpect(status().isForbidden());
		}

		@Test
		@DisplayName("정상 조회 시 fleaMarketList/pageMaker를 반환하고, total은 getFavoritesCnt로 구한다")
		void favorites_success_usesFavoritesCnt() throws Exception {
			given(fleaService.getFavorites(any(FleaMarketDto.class))).willReturn(List.of());
			given(fleaService.getFavoritesCnt(any(FleaMarketDto.class))).willReturn(3);

			mockMvc.perform(get("/api/fleaMarket/my/favorites").with(user(loginUser)))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.fleaMarketList").isArray())
					.andExpect(jsonPath("$.pageMaker.total").value(3));

			verify(fleaService).getFavoritesCnt(any(FleaMarketDto.class));
			verify(fleaService, never()).getMyStoreCnt(any());
		}
	}
}
