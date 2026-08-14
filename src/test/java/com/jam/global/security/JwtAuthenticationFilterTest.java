package com.jam.global.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.jam.global.jwt.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

	@Mock
	private JwtService jwtService;
	@Mock
	private FilterChain filterChain;

	@InjectMocks
	private JwtAuthenticationFilter filter;

	private MockHttpServletRequest request;
	private MockHttpServletResponse response;

	@BeforeEach
	void setUp() {
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
	}

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Nested
	@DisplayName("doFilterInternal")
	class DoFilterInternal {

		@Test
		@DisplayName("쿠키가 없으면 인증을 시도하지 않고 다음 필터로 넘긴다")
		void noCookies_skipsAuthenticationAndProceeds() throws Exception {
			filter.doFilterInternal(request, response, filterChain);

			verify(jwtService, org.mockito.Mockito.never()).getAuthentication(any(), any(), any());
			assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
			verify(filterChain).doFilter(request, response);
		}

		@Test
		@DisplayName("인증된 Authentication을 반환하면 SecurityContext에 설정한다")
		void authenticatedResult_setsSecurityContext() throws Exception {
			request.setCookies(new Cookie("Authorization", "access-token"));
			Authentication authentication =
					new UsernamePasswordAuthenticationToken("user1", null, java.util.List.of());
			given(jwtService.getAuthentication(request.getCookies(), request, response)).willReturn(authentication);

			filter.doFilterInternal(request, response, filterChain);

			assertThat(SecurityContextHolder.getContext().getAuthentication()).isSameAs(authentication);
			verify(filterChain).doFilter(request, response);
		}

		@Test
		@DisplayName("Authentication이 null이면 SecurityContext를 정리한다")
		void nullAuthentication_clearsSecurityContext() throws Exception {
			request.setCookies(new Cookie("Authorization", "bad-token"));
			given(jwtService.getAuthentication(request.getCookies(), request, response)).willReturn(null);
			SecurityContextHolder.getContext()
					.setAuthentication(new UsernamePasswordAuthenticationToken("stale", null, java.util.List.of()));

			filter.doFilterInternal(request, response, filterChain);

			assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
			verify(filterChain).doFilter(request, response);
		}

		@Test
		@DisplayName("Authentication이 인증되지 않은 상태면 SecurityContext를 정리한다")
		void unauthenticatedResult_clearsSecurityContext() throws Exception {
			request.setCookies(new Cookie("Authorization", "access-token"));
			Authentication unauthenticated =
					new UsernamePasswordAuthenticationToken("user1", null, java.util.List.of()) {
						@Override
						public boolean isAuthenticated() {
							return false;
						}
					};
			given(jwtService.getAuthentication(request.getCookies(), request, response)).willReturn(unauthenticated);

			filter.doFilterInternal(request, response, filterChain);

			assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
			verify(filterChain).doFilter(request, response);
		}
	}

	@Nested
	@DisplayName("shouldNotFilter")
	class ShouldNotFilter {

		@Test
		@DisplayName("정적 리소스 경로는 필터를 건너뛴다")
		void staticResourcePaths_returnsTrue() {
			assertThat(shouldNotFilterFor("/css/style.css")).isTrue();
			assertThat(shouldNotFilterFor("/js/app.js")).isTrue();
			assertThat(shouldNotFilterFor("/images/logo.png")).isTrue();
			assertThat(shouldNotFilterFor("/favicon.ico")).isTrue();
			assertThat(shouldNotFilterFor("/fonts/font.woff")).isTrue();
		}

		@Test
		@DisplayName("로그인 처리 경로는 필터를 건너뛴다")
		void loginProcessPath_returnsTrue() {
			assertThat(shouldNotFilterFor("/api/member/login-process")).isTrue();
		}

		@Test
		@DisplayName("그 외 경로는 필터를 통과시킨다")
		void otherPaths_returnsFalse() {
			assertThat(shouldNotFilterFor("/api/community/posts")).isFalse();
		}

		private boolean shouldNotFilterFor(String servletPath) {
			MockHttpServletRequest req = new MockHttpServletRequest();
			req.setServletPath(servletPath);
			return filter.shouldNotFilter(req);
		}
	}
}
