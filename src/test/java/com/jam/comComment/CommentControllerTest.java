package com.jam.comComment;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

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

import com.jam.comComment.dto.CommentDto;
import com.jam.comComment.service.CommentService;
import com.jam.config.MyBatisConfig;
import com.jam.global.exception.ForbiddenException;
import com.jam.global.security.JwtAuthenticationFilter;
import com.jam.member.dto.MemberDto;

/**
 * CommentController @WebMvcTest 슬라이스 테스트. 다른 도메인과 같은 패턴:
 * 실제 SecurityConfig 대신 @PreAuthorize만 동작하는 최소 Security 설정(permitAll)을 따로 구성해서 씀.
 */
@WebMvcTest(
		controllers = CommentController.class,
		excludeFilters = {
				@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class),
				@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = MyBatisConfig.class)
		})
@Import(CommentControllerTest.MethodSecurityTestConfig.class)
class CommentControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private CommentService commentService;

	private MemberDto loginUser;

	@BeforeEach
	void setUp() {
		reset(commentService);

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
	@DisplayName("GET /community/posts/{postId}/comments")
	class CommentList {

		@Test
		@DisplayName("비로그인이어도 조회 가능하며 user_id 없이 서비스에 전달된다")
		void commentList_anonymous_noUserId() throws Exception {
			given(commentService.commentList(eq(1L), isNull())).willReturn(List.of());

			mockMvc.perform(get("/community/posts/1/comments"))
					.andExpect(status().isOk());

			verify(commentService).commentList(1L, null);
		}

		@Test
		@DisplayName("로그인 사용자면 user_id가 서비스에 전달된다")
		void commentList_loggedIn_passesUserId() throws Exception {
			given(commentService.commentList(eq(1L), eq("user1"))).willReturn(List.of());

			mockMvc.perform(get("/community/posts/1/comments").with(user(loginUser)))
					.andExpect(status().isOk());

			verify(commentService).commentList(1L, "user1");
		}
	}

	@Nested
	@DisplayName("POST /community/posts/{postId}/comments")
	class InsertComment {

		@Test
		@DisplayName("비인증이면 접근 거부된다")
		void insertComment_anonymous_denied() throws Exception {
			mockMvc.perform(post("/community/posts/1/comments")
							.contentType(MediaType.APPLICATION_JSON)
							.content("{\"content\":\"댓글\"}"))
					.andExpect(status().isForbidden());
		}

		@Test
		@DisplayName("정상 작성 시 200을 응답하고 postId/user_id가 세팅되어 전달된다")
		void insertComment_success_setsPostIdAndUserId() throws Exception {
			mockMvc.perform(post("/community/posts/1/comments")
							.with(user(loginUser))
							.contentType(MediaType.APPLICATION_JSON)
							.content("{\"content\":\"댓글 내용\"}"))
					.andExpect(status().isOk());

			ArgumentCaptor<CommentDto> captor = ArgumentCaptor.forClass(CommentDto.class);
			verify(commentService).insertComment(captor.capture());
			assertPostIdAndUserId(captor.getValue(), 1L, "user1");
		}

		private void assertPostIdAndUserId(CommentDto dto, Long postId, String userId) {
			org.assertj.core.api.Assertions.assertThat(dto.getPost_id()).isEqualTo(postId);
			org.assertj.core.api.Assertions.assertThat(dto.getUser_id()).isEqualTo(userId);
		}
	}

	@Nested
	@DisplayName("PUT /community/comments/{commentId}")
	class UpdateComment {

		@Test
		@DisplayName("비인증이면 접근 거부된다")
		void updateComment_anonymous_denied() throws Exception {
			mockMvc.perform(put("/community/comments/1")
							.contentType(MediaType.APPLICATION_JSON)
							.content("{\"content\":\"수정\"}"))
					.andExpect(status().isForbidden());
		}

		@Test
		@DisplayName("정상 수정 시 200을 응답한다")
		void updateComment_success() throws Exception {
			given(commentService.updateComment(any(CommentDto.class))).willReturn(1);

			mockMvc.perform(put("/community/comments/1")
							.with(user(loginUser))
							.contentType(MediaType.APPLICATION_JSON)
							.content("{\"content\":\"수정된 내용\"}"))
					.andExpect(status().isOk());

			ArgumentCaptor<CommentDto> captor = ArgumentCaptor.forClass(CommentDto.class);
			verify(commentService).updateComment(captor.capture());
			org.assertj.core.api.Assertions.assertThat(captor.getValue().getComment_id()).isEqualTo(1L);
			org.assertj.core.api.Assertions.assertThat(captor.getValue().getUser_id()).isEqualTo("user1");
		}

		@Test
		@DisplayName("본인 댓글이 아니면(ForbiddenException) 403이 전파된다")
		void updateComment_notOwner_forbidden() throws Exception {
			doThrow(new ForbiddenException("수정 권한이 없습니다."))
					.when(commentService).updateComment(any(CommentDto.class));

			mockMvc.perform(put("/community/comments/1")
							.with(user(loginUser))
							.contentType(MediaType.APPLICATION_JSON)
							.content("{\"content\":\"수정된 내용\"}"))
					.andExpect(status().isForbidden())
					.andExpect(jsonPath("$.detail").value("수정 권한이 없습니다."));
		}
	}

	@Nested
	@DisplayName("DELETE /community/comments/{commentId}")
	class DeleteComment {

		@Test
		@DisplayName("비인증이면 접근 거부된다")
		void deleteComment_anonymous_denied() throws Exception {
			mockMvc.perform(delete("/community/comments/1"))
					.andExpect(status().isForbidden());
		}

		@Test
		@DisplayName("정상 삭제 시 200을 응답한다")
		void deleteComment_success() throws Exception {
			mockMvc.perform(delete("/community/comments/1").with(user(loginUser)))
					.andExpect(status().isOk());

			verify(commentService).deleteComment(1L, "user1");
		}

		@Test
		@DisplayName("본인 댓글이 아니면(ForbiddenException) 403이 전파된다")
		void deleteComment_notOwner_forbidden() throws Exception {
			doThrow(new ForbiddenException("삭제 권한이 없습니다."))
					.when(commentService).deleteComment(1L, "user1");

			mockMvc.perform(delete("/community/comments/1").with(user(loginUser)))
					.andExpect(status().isForbidden())
					.andExpect(jsonPath("$.detail").value("삭제 권한이 없습니다."));
		}
	}
}
