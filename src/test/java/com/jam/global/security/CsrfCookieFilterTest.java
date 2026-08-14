package com.jam.global.security;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;

import jakarta.servlet.FilterChain;

class CsrfCookieFilterTest {

	private final CsrfCookieFilter filter = new CsrfCookieFilter();

	@Nested
	@DisplayName("doFilterInternal")
	class DoFilterInternal {

		@Test
		@DisplayName("CsrfToken 속성이 있으면 getToken()을 호출해 쿠키 발급을 트리거하고 다음 필터로 넘긴다")
		void csrfTokenPresent_triggersTokenAndProceeds() throws Exception {
			MockHttpServletRequest request = new MockHttpServletRequest();
			MockHttpServletResponse response = new MockHttpServletResponse();
			FilterChain filterChain = mock(FilterChain.class);
			CsrfToken csrfToken = mock(CsrfToken.class);
			request.setAttribute(CsrfToken.class.getName(), csrfToken);

			filter.doFilterInternal(request, response, filterChain);

			verify(csrfToken).getToken();
			verify(filterChain).doFilter(request, response);
		}

		@Test
		@DisplayName("CsrfToken 속성이 없으면 그냥 다음 필터로 넘긴다")
		void csrfTokenAbsent_justProceeds() throws Exception {
			MockHttpServletRequest request = new MockHttpServletRequest();
			MockHttpServletResponse response = new MockHttpServletResponse();
			FilterChain filterChain = mock(FilterChain.class);

			filter.doFilterInternal(request, response, filterChain);

			verify(filterChain).doFilter(request, response);
		}
	}
}
