package com.jam.global.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.SQLException;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.servlet.ModelAndView;

import com.jam.global.exception.BadRequestException;
import com.jam.global.exception.ConflictException;
import com.jam.global.exception.ForbiddenException;
import com.jam.global.exception.NotFoundException;
import com.jam.global.exception.UnauthorizedException;

class GlobalExceptionHandlerTest {

	private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

	private MockHttpServletRequest requestFor(String uri) {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRequestURI(uri);
		return request;
	}

	@Nested
	@DisplayName("타입별 예외 -> 상태코드/본문 매핑")
	class TypedExceptions {

		@Test
		@DisplayName("BadRequestException -> 400")
		void badRequest() {
			ResponseEntity<Map<String, Object>> result =
					handler.handleBadRequest(new BadRequestException("잘못된 요청"), requestFor("/api/x"));

			assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
			assertThat(result.getBody().get("status")).isEqualTo(400);
			assertThat(result.getBody().get("detail")).isEqualTo("잘못된 요청");
			assertThat(result.getBody().get("path")).isEqualTo("/api/x");
		}

		@Test
		@DisplayName("UnauthorizedException -> 401, loginRequired=true")
		void unauthorized() throws Exception {
			ResponseEntity<Map<String, Object>> result =
					handler.handleUnauthrized(new UnauthorizedException("로그인 필요"), requestFor("/api/x"));

			assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
			assertThat(result.getBody().get("loginRequired")).isEqualTo(true);
		}

		@Test
		@DisplayName("ForbiddenException -> 403, forbidden=true")
		void forbidden() throws Exception {
			ResponseEntity<Map<String, Object>> result =
					handler.handleForbidden(new ForbiddenException("권한 없음"), requestFor("/api/x"));

			assertThat(result.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
			assertThat(result.getBody().get("forbidden")).isEqualTo(true);
		}

		@Test
		@DisplayName("NoResourceFoundException -> error/404 뷰")
		void noResourceFound() {
			ModelAndView result = handler.handle404();

			assertThat(result.getViewName()).isEqualTo("error/404");
		}

		@Test
		@DisplayName("NotFoundException -> 404")
		void notFound() {
			ResponseEntity<Map<String, Object>> result =
					handler.handleNotFound(new NotFoundException("게시글 없음"), requestFor("/api/x"));

			assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
			assertThat(result.getBody().get("detail")).isEqualTo("게시글 없음");
		}

		@Test
		@DisplayName("ConflictException -> 409")
		void conflict() {
			ResponseEntity<Map<String, Object>> result =
					handler.handleConflict(new ConflictException("중복 요청"), requestFor("/api/x"));

			assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
			assertThat(result.getBody().get("detail")).isEqualTo("중복 요청");
		}
	}

	@Nested
	@DisplayName("DB 관련 예외 -> 500, 상세 메시지는 노출하지 않는다")
	class DbExceptions {

		@Test
		@DisplayName("SQLException -> 500, 원본 메시지 대신 안내 문구")
		void sqlException() {
			ResponseEntity<Map<String, Object>> result =
					handler.handleSqlError(new SQLException("ORA-00001 unique constraint"), requestFor("/api/x"));

			assertThat(result.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
			assertThat(result.getBody().get("detail")).isEqualTo("일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
		}

		@Test
		@DisplayName("DataAccessException -> 500, 원본 메시지 대신 안내 문구")
		void dataAccessException() {
			ResponseEntity<Map<String, Object>> result = handler.handleDataAccess(
					new DataAccessResourceFailureException("connection pool exhausted"), requestFor("/api/x"));

			assertThat(result.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
			assertThat(result.getBody().get("detail")).isEqualTo("일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
		}
	}

	@Nested
	@DisplayName("handleAny")
	class HandleAny {

		@Test
		@DisplayName("그 외 일반 예외는 500으로 처리한다")
		void genericException_returns500() throws Exception {
			ResponseEntity<Map<String, Object>> result =
					handler.handleAny(new RuntimeException("무언가 터짐"), requestFor("/api/x"));

			assertThat(result.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
			assertThat(result.getBody().get("detail")).isEqualTo("일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
		}

		@Test
		@DisplayName("AccessDeniedException은 직접 처리하지 않고 다시 던져 Security 핸들러로 넘긴다")
		void accessDeniedException_rethrown() {
			MockHttpServletRequest request = requestFor("/api/x");

			assertThatThrownBy(() -> handler.handleAny(new AccessDeniedException("no access"), request))
					.isInstanceOf(AccessDeniedException.class);
		}

		@Test
		@DisplayName("AuthorizationDeniedException도 다시 던져 Security 핸들러로 넘긴다")
		void authorizationDeniedException_rethrown() {
			MockHttpServletRequest request = requestFor("/api/x");

			assertThatThrownBy(() -> handler.handleAny(new AuthorizationDeniedException("no access"), request))
					.isInstanceOf(AuthorizationDeniedException.class);
		}
	}
}
