package com.jam.global.handler;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;

import com.fasterxml.jackson.databind.ObjectMapper;

class CustomAuthEntryPointTest {

	private final CustomAuthEntryPoint entryPoint = new CustomAuthEntryPoint();
	private final ObjectMapper objectMapper = new ObjectMapper();

	private MockHttpServletRequest request;
	private MockHttpServletResponse response;

	@BeforeEach
	void setUp() {
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
	}

	@Nested
	@DisplayName("commence")
	class Commence {

		@Test
		@DisplayName("Accept 헤더가 application/json이면 401 JSON 응답을 작성한다")
		void jsonAccept_writesJsonBody() throws Exception {
			request.addHeader("Accept", "application/json");
			request.setRequestURI("/api/community/posts");

			entryPoint.commence(request, response, new BadCredentialsException("no auth"));

			assertThat(response.getStatus()).isEqualTo(401);
			assertThat(response.getContentType()).startsWith("application/json");

			Map<?, ?> body = objectMapper.readValue(response.getContentAsString(), Map.class);
			assertThat(body.get("status")).isEqualTo(401);
			assertThat(body.get("error")).isEqualTo("UNAUTHORIZED");
			assertThat(body.get("loginRequired")).isEqualTo(true);
			assertThat(body.get("path")).isEqualTo("/api/community/posts");
		}

		@Test
		@DisplayName("Accept 헤더가 json이 아니면 /error/401로 forward한다")
		void nonJsonAccept_forwardsToErrorPage() throws Exception {
			request.addHeader("Accept", "text/html");

			entryPoint.commence(request, response, new BadCredentialsException("no auth"));

			assertThat(response.getForwardedUrl()).isEqualTo("/error/401");
			assertThat(request.getAttribute("msg")).isEqualTo("로그인이 필요한 서비스 입니다.");
		}

		@Test
		@DisplayName("Accept 헤더가 없으면 /error/401로 forward한다")
		void noAcceptHeader_forwardsToErrorPage() throws Exception {
			entryPoint.commence(request, response, new BadCredentialsException("no auth"));

			assertThat(response.getForwardedUrl()).isEqualTo("/error/401");
		}
	}
}
