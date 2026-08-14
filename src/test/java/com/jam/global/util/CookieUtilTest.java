package com.jam.global.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.servlet.http.Cookie;

class CookieUtilTest {

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
	@DisplayName("getValue")
	class GetValue {

		@Test
		@DisplayName("쿠키가 없으면 null을 반환한다")
		void noCookies_returnsNull() {
			assertThat(CookieUtil.getValue(request, "Authorization")).isNull();
		}

		@Test
		@DisplayName("이름이 일치하는 쿠키가 없으면 null을 반환한다")
		void noMatchingCookie_returnsNull() {
			request.setCookies(new Cookie("Other", "value"));

			assertThat(CookieUtil.getValue(request, "Authorization")).isNull();
		}

		@Test
		@DisplayName("이름이 일치하는 쿠키가 있으면 값을 반환한다")
		void matchingCookie_returnsValue() {
			request.setCookies(new Cookie("Authorization", "token-value"));

			assertThat(CookieUtil.getValue(request, "Authorization")).isEqualTo("token-value");
		}
	}

	@Nested
	@DisplayName("addCookie")
	class AddCookie {

		@Test
		@DisplayName("HttpOnly/Path/SameSite=Lax를 설정하고 응답에 추가한다")
		void addsCookieWithSecurityAttributes() {
			CookieUtil.addCookie(request, response, "Authorization", "token-value", 3600);

			Cookie cookie = response.getCookie("Authorization");
			assertThat(cookie.getValue()).isEqualTo("token-value");
			assertThat(cookie.isHttpOnly()).isTrue();
			assertThat(cookie.getPath()).isEqualTo("/");
			assertThat(cookie.getMaxAge()).isEqualTo(3600);
			assertThat(cookie.getAttribute("SameSite")).isEqualTo("Lax");
		}

		@Test
		@DisplayName("request가 secure이면 쿠키도 secure로 설정한다")
		void secureRequest_setsSecureCookie() {
			request.setSecure(true);

			CookieUtil.addCookie(request, response, "Authorization", "token-value", 3600);

			assertThat(response.getCookie("Authorization").getSecure()).isTrue();
		}
	}

	@Nested
	@DisplayName("deleteCookie")
	class DeleteCookie {

		@Test
		@DisplayName("값을 null, maxAge를 0으로 설정해 응답에 추가한다")
		void addsExpiringCookie() {
			CookieUtil.deleteCookie(request, response, "Authorization");

			Cookie cookie = response.getCookie("Authorization");
			assertThat(cookie.getValue()).isNull();
			assertThat(cookie.getMaxAge()).isZero();
		}
	}

	@Nested
	@DisplayName("clearAuthCookies")
	class ClearAuthCookies {

		@Test
		@DisplayName("SecurityContext를 비우고 Authorization/RefreshToken 쿠키를 만료시킨다")
		void clearsContextAndBothCookies() {
			SecurityContextHolder.getContext()
					.setAuthentication(new UsernamePasswordAuthenticationToken("user1", null, java.util.List.of()));

			CookieUtil.clearAuthCookies(request, response);

			assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
			assertThat(response.getCookie("Authorization").getMaxAge()).isZero();
			assertThat(response.getCookie("RefreshToken").getMaxAge()).isZero();
		}
	}
}
