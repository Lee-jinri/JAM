package com.jam.member.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;

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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.jam.config.MyBatisConfig;
import com.jam.global.jwt.JwtService;
import com.jam.global.security.JwtAuthenticationFilter;
import com.jam.member.dto.MemberDto;
import com.jam.member.service.MemberService;

/**
 * OAuthController @WebMvcTest 슬라이스 테스트.
 * getKakaoAccessToken/getKakaoUserInfo/getNaverAccessToken/getNaverUserInfo는 메서드 내부에서
 * new RestTemplate()으로 실제 카카오/네이버 API를 직접 호출하기 때문에(주입 불가) 목으로 대체할 수 없다.
 * 그래서 콜백의 "정상 로그인 성공" 경로는 테스트 범위에서 제외하고, 외부 API 호출 없이 도는 부분
 * (state 저장/검증, redirect URL 구성, 로그아웃의 쿠키/DB 정리)만 검증한다.
 */
@WebMvcTest(
		controllers = OAuthController.class,
		excludeFilters = {
				@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class),
				@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = MyBatisConfig.class)
		})
@Import(OAuthControllerTest.MethodSecurityTestConfig.class)
class OAuthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private MemberService memberService;
	@MockBean
	private JwtService jwtService;
	@MockBean
	private RedisTemplate<String, String> stringRedisTemplate;
	@MockBean
	private ValueOperations<String, String> valueOperations;

	private MemberDto loginUser;

	@BeforeEach
	void setUp() {
		reset(memberService, jwtService, stringRedisTemplate, valueOperations);

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

	private String extractState(String redisKey) {
		assertThat(redisKey).startsWith("oauth:state:");
		return redisKey.substring("oauth:state:".length());
	}

	@Nested
	@DisplayName("GET /oauth/kakao")
	class RedirectToKakaoAuth {

		@Test
		@DisplayName("redirect 파라미터가 없으면 기본값 '/'로 state를 redis에 5분 TTL로 저장하고 카카오 인증 페이지로 리다이렉트한다")
		void noRedirectParam_storesDefaultPrevPage() throws Exception {
			given(stringRedisTemplate.opsForValue()).willReturn(valueOperations);

			MvcResult result = mockMvc.perform(get("/oauth/kakao"))
					.andExpect(status().is3xxRedirection())
					.andReturn();

			String location = result.getResponse().getRedirectedUrl();
			assertThat(location).startsWith("https://kauth.kakao.com/oauth/authorize");

			ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
			verify(valueOperations).set(keyCaptor.capture(), valueCaptor.capture(), eq(Duration.ofMinutes(5)));
			assertThat(valueCaptor.getValue()).isEqualTo("kakao|/");

			String state = extractState(keyCaptor.getValue());
			assertThat(location).contains("state=" + state);
		}

		@Test
		@DisplayName("redirect 파라미터가 있으면 해당 값을 prevPage로 저장한다")
		void withRedirectParam_storesGivenPrevPage() throws Exception {
			given(stringRedisTemplate.opsForValue()).willReturn(valueOperations);

			mockMvc.perform(get("/oauth/kakao").param("redirect", "/mypage"))
					.andExpect(status().is3xxRedirection());

			verify(valueOperations).set(anyString(), eq("kakao|/mypage"), eq(Duration.ofMinutes(5)));
		}

		@Test
		@DisplayName("redis 저장 중 예외가 발생하면 오류 페이지로 리다이렉트한다")
		void redisFails_redirectsToErrorPage() throws Exception {
			given(stringRedisTemplate.opsForValue()).willThrow(new RuntimeException("redis down"));

			mockMvc.perform(get("/oauth/kakao"))
					.andExpect(status().is3xxRedirection())
					.andExpect(result -> assertThat(result.getResponse().getRedirectedUrl())
							.isEqualTo("/member/login?error=oauth"));
		}
	}

	@Nested
	@DisplayName("GET /oauth/naver")
	class RedirectToNaverAuth {

		@Test
		@DisplayName("redirect 파라미터가 없으면 기본값 '/'로 state를 redis에 저장하고 네이버 인증 페이지로 리다이렉트한다")
		void noRedirectParam_storesDefaultPrevPage() throws Exception {
			given(stringRedisTemplate.opsForValue()).willReturn(valueOperations);

			MvcResult result = mockMvc.perform(get("/oauth/naver"))
					.andExpect(status().is3xxRedirection())
					.andReturn();

			String location = result.getResponse().getRedirectedUrl();
			assertThat(location).startsWith("https://nid.naver.com/oauth2.0/authorize");

			verify(valueOperations).set(anyString(), eq("naver|/"), eq(Duration.ofMinutes(5)));
		}

		@Test
		@DisplayName("redis 저장 중 예외가 발생하면 오류 페이지로 리다이렉트한다")
		void redisFails_redirectsToErrorPage() throws Exception {
			given(stringRedisTemplate.opsForValue()).willThrow(new RuntimeException("redis down"));

			mockMvc.perform(get("/oauth/naver"))
					.andExpect(status().is3xxRedirection())
					.andExpect(result -> assertThat(result.getResponse().getRedirectedUrl())
							.isEqualTo("/member/login?error=oauth"));
		}
	}

	@Nested
	@DisplayName("GET /oauth/kakao/callback")
	class HandleKakaoCallback {

		@Test
		@DisplayName("state가 redis에 없으면(hasKey=false) invalid_state로 리다이렉트하고 외부 API를 호출하지 않는다")
		void invalidState_redirectsWithoutExternalCall() throws Exception {
			given(stringRedisTemplate.hasKey(anyString())).willReturn(false);

			mockMvc.perform(get("/oauth/kakao/callback").param("code", "code1").param("state", "bad-state"))
					.andExpect(status().is3xxRedirection())
					.andExpect(result -> assertThat(result.getResponse().getRedirectedUrl())
							.isEqualTo("/member/login?error=invalid_state"));

			verify(memberService, never()).socialLoginOrRegister(any(), anyString());
		}

		@Test
		@DisplayName("hasKey가 null이어도 invalid_state로 리다이렉트한다")
		void hasKeyNull_redirectsInvalidState() throws Exception {
			given(stringRedisTemplate.hasKey(anyString())).willReturn(null);

			mockMvc.perform(get("/oauth/kakao/callback").param("code", "code1").param("state", "bad-state"))
					.andExpect(status().is3xxRedirection())
					.andExpect(result -> assertThat(result.getResponse().getRedirectedUrl())
							.isEqualTo("/member/login?error=invalid_state"));
		}
	}

	@Nested
	@DisplayName("GET /oauth/naver/callback")
	class HandleNaverCallback {

		@Test
		@DisplayName("state가 redis에 없으면 invalid_state로 리다이렉트하고 외부 API를 호출하지 않는다")
		void invalidState_redirectsWithoutExternalCall() throws Exception {
			given(stringRedisTemplate.opsForValue()).willReturn(valueOperations);
			given(valueOperations.get(anyString())).willReturn(null);

			mockMvc.perform(get("/oauth/naver/callback").param("code", "code1").param("state", "bad-state"))
					.andExpect(status().is3xxRedirection())
					.andExpect(result -> assertThat(result.getResponse().getRedirectedUrl())
							.isEqualTo("/member/login?error=invalid_state"));

			verify(memberService, never()).socialLoginOrRegister(any(), anyString());
		}
	}

	@Nested
	@DisplayName("POST /oauth/kakao/logout")
	class KakaoLogout {

		@Test
		@DisplayName("카카오 액세스 토큰 쿠키가 없으면 외부 API 호출 없이 로컬 쿠키만 정리한다")
		void noKakaoCookie_clearsLocalCookiesOnly() throws Exception {
			mockMvc.perform(post("/oauth/kakao/logout"))
					.andExpect(status().isOk())
					.andExpect(cookie().maxAge("Authorization", 0))
					.andExpect(cookie().maxAge("kakaoAccessToken", 0))
					.andExpect(cookie().maxAge("RefreshToken", 0));
		}

		@Test
		@DisplayName("비로그인 상태면 refreshToken 삭제를 호출하지 않는다")
		void anonymous_doesNotDeleteRefreshToken() throws Exception {
			mockMvc.perform(post("/oauth/kakao/logout"))
					.andExpect(status().isOk());

			verify(memberService, never()).deleteRefreshToken(anyString());
		}

		@Test
		@DisplayName("로그인 상태면 refreshToken 삭제를 호출한다")
		void loggedIn_deletesRefreshToken() throws Exception {
			mockMvc.perform(post("/oauth/kakao/logout").with(user(loginUser)))
					.andExpect(status().isOk());

			verify(memberService).deleteRefreshToken("user1");
		}

		@Test
		@DisplayName("refreshToken 삭제 중 예외가 발생해도 로그아웃은 계속 진행되어 200을 응답한다")
		void deleteRefreshTokenThrows_stillSucceeds() throws Exception {
			doThrow(new RuntimeException("DB 오류")).when(memberService).deleteRefreshToken(anyString());

			mockMvc.perform(post("/oauth/kakao/logout").with(user(loginUser)))
					.andExpect(status().isOk())
					.andExpect(cookie().maxAge("Authorization", 0));
		}
	}

	@Nested
	@DisplayName("POST /oauth/naver/logout")
	class NaverLogout {

		@Test
		@DisplayName("로컬 쿠키(Authorization, naverAccessToken, RefreshToken)를 정리한다")
		void clearsLocalCookies() throws Exception {
			mockMvc.perform(post("/oauth/naver/logout"))
					.andExpect(status().isOk())
					.andExpect(cookie().maxAge("Authorization", 0))
					.andExpect(cookie().maxAge("naverAccessToken", 0))
					.andExpect(cookie().maxAge("RefreshToken", 0));
		}

		@Test
		@DisplayName("로그인 상태면 refreshToken 삭제를 호출한다")
		void loggedIn_deletesRefreshToken() throws Exception {
			mockMvc.perform(post("/oauth/naver/logout").with(user(loginUser)))
					.andExpect(status().isOk());

			verify(memberService).deleteRefreshToken("user1");
		}

		@Test
		@DisplayName("비로그인 상태면 refreshToken 삭제를 호출하지 않는다")
		void anonymous_doesNotDeleteRefreshToken() throws Exception {
			mockMvc.perform(post("/oauth/naver/logout"))
					.andExpect(status().isOk());

			verify(memberService, never()).deleteRefreshToken(anyString());
		}
	}
}
