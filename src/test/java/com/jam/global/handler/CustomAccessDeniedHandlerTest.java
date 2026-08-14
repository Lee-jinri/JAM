package com.jam.global.handler;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;

import com.fasterxml.jackson.databind.ObjectMapper;

class CustomAccessDeniedHandlerTest {

	private final CustomAccessDeniedHandler handler = new CustomAccessDeniedHandler();
	private final ObjectMapper objectMapper = new ObjectMapper();

	private MockHttpServletRequest request;
	private MockHttpServletResponse response;

	@BeforeEach
	void setUp() {
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
	}

	@Nested
	@DisplayName("handle")
	class Handle {

		@Test
		@DisplayName("Accept 헤더가 application/json이면 403 JSON 응답을 작성한다")
		void jsonAccept_writesJsonBody() throws Exception {
			request.addHeader("Accept", "application/json");
			request.setRequestURI("/api/admin/users");

			handler.handle(request, response, new AccessDeniedException("no access"));

			assertThat(response.getStatus()).isEqualTo(403);
			assertThat(response.getContentType()).startsWith("application/json");

			Map<?, ?> body = objectMapper.readValue(response.getContentAsString(), Map.class);
			assertThat(body.get("status")).isEqualTo(403);
			assertThat(body.get("error")).isEqualTo("Forbidden");
			assertThat(body.get("forbidden")).isEqualTo(true);
			assertThat(body.get("path")).isEqualTo("/api/admin/users");
		}

		@Test
		@DisplayName("Accept 헤더가 json이 아니면 /error/403으로 forward한다")
		void nonJsonAccept_forwardsToErrorPage() throws Exception {
			request.addHeader("Accept", "text/html");

			handler.handle(request, response, new AccessDeniedException("no access"));

			assertThat(response.getForwardedUrl()).isEqualTo("/error/403");
			assertThat(request.getAttribute("msg")).isEqualTo("권한이 없는 페이지 입니다.");
		}

		@Test
		@DisplayName("Accept 헤더가 없으면 /error/403으로 forward한다")
		void noAcceptHeader_forwardsToErrorPage() throws Exception {
			handler.handle(request, response, new AccessDeniedException("no access"));

			assertThat(response.getForwardedUrl()).isEqualTo("/error/403");
		}
	}
}
