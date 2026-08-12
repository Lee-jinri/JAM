package com.jam.mypage.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import com.jam.config.MyBatisConfig;
import com.jam.global.exception.ConflictException;
import com.jam.global.jwt.JwtService;
import com.jam.global.security.JwtAuthenticationFilter;
import com.jam.member.dto.MemberDto;
import com.jam.mypage.service.MypageService;

/**
 * MypageRestController @WebMvcTest 슬라이스 테스트. 다른 도메인과 같은 패턴.
 */
@WebMvcTest(
		controllers = MypageRestController.class,
		excludeFilters = {
				@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class),
				@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = MyBatisConfig.class)
		})
@Import(MypageRestControllerTest.MethodSecurityTestConfig.class)
class MypageRestControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private MypageService mypageService;
	@MockBean
	private JwtService jwtService;
	@MockBean
	private StringRedisTemplate redisTemplate;
	@MockBean
	private ValueOperations<String, String> valueOperations;

	private MemberDto loginUser;

	@BeforeEach
	void setUp() {
		reset(mypageService, jwtService, redisTemplate, valueOperations);

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
	@DisplayName("POST /api/mypage/favorite/{postId}")
	class AddFavorite {

		@Test
		@DisplayName("비로그인이면 401을 응답한다")
		void anonymous_unauthorized() throws Exception {
			mockMvc.perform(post("/api/mypage/favorite/1").param("boardType", "JOB"))
					.andExpect(status().isUnauthorized());
		}

		@Test
		@DisplayName("잘못된 boardType이면 400을 응답한다")
		void invalidBoardType_badRequest() throws Exception {
			mockMvc.perform(post("/api/mypage/favorite/1").param("boardType", "studio").with(user(loginUser)))
					.andExpect(status().isBadRequest());

			verify(mypageService, never()).addFavorite(anyString(), anyString(), any());
		}

		@Test
		@DisplayName("정상 추가되면 200을 응답하고 boardType이 정규화되어 전달된다")
		void success_normalizesBoardType() throws Exception {
			given(mypageService.addFavorite("user1", "JOB", 1L)).willReturn(true);

			mockMvc.perform(post("/api/mypage/favorite/1").param("boardType", "job").with(user(loginUser)))
					.andExpect(status().isOk());

			verify(mypageService).addFavorite("user1", "JOB", 1L);
		}

		@Test
		@DisplayName("이미 즐겨찾기한 글이면(ConflictException) 409가 전파된다")
		void duplicate_conflict() throws Exception {
			given(mypageService.addFavorite("user1", "JOB", 1L))
					.willThrow(new ConflictException("이미 즐겨찾기에 추가한 게시글입니다."));

			mockMvc.perform(post("/api/mypage/favorite/1").param("boardType", "JOB").with(user(loginUser)))
					.andExpect(status().isConflict());
		}
	}

	@Nested
	@DisplayName("DELETE /api/mypage/favorite/{postId}")
	class DeleteFavorite {

		@Test
		@DisplayName("비로그인이면 401을 응답한다")
		void anonymous_unauthorized() throws Exception {
			mockMvc.perform(delete("/api/mypage/favorite/1").param("boardType", "JOB"))
					.andExpect(status().isUnauthorized());
		}

		@Test
		@DisplayName("잘못된 boardType이면 400을 응답한다")
		void invalidBoardType_badRequest() throws Exception {
			mockMvc.perform(delete("/api/mypage/favorite/1").param("boardType", "invalid").with(user(loginUser)))
					.andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("정상 삭제되면 200을 응답한다")
		void success() throws Exception {
			given(mypageService.deleteFavorite("user1", "JOB", 1L)).willReturn(true);

			mockMvc.perform(delete("/api/mypage/favorite/1").param("boardType", "JOB").with(user(loginUser)))
					.andExpect(status().isOk());
		}

		@Test
		@DisplayName("삭제할 즐겨찾기가 없으면 404를 응답한다")
		void notFound() throws Exception {
			given(mypageService.deleteFavorite("user1", "JOB", 1L)).willReturn(false);

			mockMvc.perform(delete("/api/mypage/favorite/1").param("boardType", "JOB").with(user(loginUser)))
					.andExpect(status().isNotFound());
		}
	}

	@Nested
	@DisplayName("GET /api/mypage/account")
	class GetAccount {

		@Test
		@DisplayName("비로그인이면 접근 거부된다")
		void anonymous_denied() throws Exception {
			mockMvc.perform(get("/api/mypage/account"))
					.andExpect(status().isForbidden());
		}

		@Test
		@DisplayName("인증 플래그가 유효하면 계정 정보를 반환한다")
		void verified_returnsAccount() throws Exception {
			given(jwtService.extractLoginType(any(), any(), any())).willReturn("local");
			given(redisTemplate.opsForValue()).willReturn(valueOperations);
			given(valueOperations.get("auth:mypage:user1")).willReturn("true");
			MemberDto account = new MemberDto();
			account.setUser_id("user1");
			given(mypageService.account("user1")).willReturn(account);

			mockMvc.perform(get("/api/mypage/account").with(user(loginUser)))
					.andExpect(status().isOk());
		}

		@Test
		@DisplayName("인증 플래그가 없으면 401을 응답한다")
		void notVerified_unauthorized() throws Exception {
			given(jwtService.extractLoginType(any(), any(), any())).willReturn("local");
			given(redisTemplate.opsForValue()).willReturn(valueOperations);
			given(valueOperations.get("auth:mypage:user1")).willReturn(null);

			mockMvc.perform(get("/api/mypage/account").with(user(loginUser)))
					.andExpect(status().isUnauthorized());

			verify(mypageService, never()).account(anyString());
		}
	}

	@Nested
	@DisplayName("GET /api/mypage/account/verify-status")
	class GetVerifyStatus {

		@Test
		@DisplayName("비로그인이면 401을 응답한다")
		void anonymous_unauthorized() throws Exception {
			mockMvc.perform(get("/api/mypage/account/verify-status"))
					.andExpect(status().isUnauthorized());
		}

		@Test
		@DisplayName("소셜 로그인 사용자는 별도 확인 없이 true를 반환하고 인증 플래그를 저장한다")
		void socialLogin_autoVerifiesTrue() throws Exception {
			given(jwtService.extractLoginType(any(), any(), any())).willReturn("kakao");
			given(redisTemplate.opsForValue()).willReturn(valueOperations);
			given(valueOperations.get("auth:mypage:user1")).willReturn(null);

			mockMvc.perform(get("/api/mypage/account/verify-status").with(user(loginUser)))
					.andExpect(status().isOk())
					.andExpect(content().string("true"));

			verify(valueOperations).set(eq("auth:mypage:user1"), eq("true"), any(java.time.Duration.class));
		}

		@Test
		@DisplayName("일반 로그인 사용자는 redis 플래그 값을 그대로 반환한다")
		void localLogin_returnsRedisFlag() throws Exception {
			given(jwtService.extractLoginType(any(), any(), any())).willReturn("local");
			given(redisTemplate.opsForValue()).willReturn(valueOperations);
			given(valueOperations.get("auth:mypage:user1")).willReturn("true");

			mockMvc.perform(get("/api/mypage/account/verify-status").with(user(loginUser)))
					.andExpect(status().isOk())
					.andExpect(content().string("true"));
		}
	}

	@Nested
	@DisplayName("GET /api/mypage/verify-status/set")
	class SetVerifyStatus {

		@Test
		@DisplayName("비로그인이면 401을 응답한다")
		void anonymous_unauthorized() throws Exception {
			mockMvc.perform(get("/api/mypage/verify-status/set"))
					.andExpect(status().isUnauthorized());
		}

		@Test
		@DisplayName("로그인 사용자면 인증 플래그를 10분 TTL로 저장한다")
		void loggedIn_setsFlag() throws Exception {
			given(redisTemplate.opsForValue()).willReturn(valueOperations);

			mockMvc.perform(get("/api/mypage/verify-status/set").with(user(loginUser)))
					.andExpect(status().isOk());

			verify(valueOperations).set(eq("auth:mypage:user1"), eq("true"), any(java.time.Duration.class));
		}
	}
}
