package com.jam.global.handler;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.jam.global.exception.BadRequestException;
import com.jam.global.exception.ConflictException;
import com.jam.global.exception.ForbiddenException;
import com.jam.global.exception.NotFoundException;
import com.jam.global.exception.UnauthorizedException;

import lombok.extern.log4j.Log4j;

@RestControllerAdvice
@Log4j
public class GlobalExceptionHandler {

	@ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(BadRequestException ex) {
		log.warn("400 BAD_REQUEST: " + ex.getMessage());
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(Map.of("error", ex.getMessage()));
    }
	
	@ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
	public void handleAuth(AuthenticationException ex, HttpServletRequest request, HttpServletResponse response) throws IOException {
		log.warn("401 UNAUTHORIZED: " + ex.getMessage());
		
		clearAuthAndRedirect(request, response, "/common/unauthorized");
	}

	@ExceptionHandler(UnauthorizedException.class)
	public void handleUnauthrized(UnauthorizedException ex, HttpServletRequest request, HttpServletResponse response) throws IOException {
		log.warn("401 UNAUTHORIZED: " + ex.getMessage());

		clearAuthAndRedirect(request, response, "/common/unauthorized");
	}
	
	@ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
	public void handleDenied(AccessDeniedException ex, HttpServletResponse response) throws IOException {
		log.warn("403 FORBIDDEN: " + ex.getMessage());
	    response.sendRedirect("/common/accessDenied");
	}
	
	
	@ExceptionHandler(ForbiddenException.class)
	public void handleForbidden(ForbiddenException ex, HttpServletResponse response) throws IOException {
		log.warn("403 FORBIDDEN: "+ ex.getMessage());
		response.sendRedirect("/common/accessDenied");
	}

	@ExceptionHandler(NotFoundException.class)
	public ResponseEntity<Map<String, String>> handleNotFound(NotFoundException ex) {
	    log.warn("404 NOT_FOUND: " + ex.getMessage());
		return ResponseEntity
			.status(HttpStatus.NOT_FOUND)
			.body(Map.of("error", ex.getMessage()));
	}
	
	@ExceptionHandler(ConflictException.class)
    public ResponseEntity<Map<String, String>> handleConflict(ConflictException ex) {
	    log.warn("409 CONFLICT: " + ex.getMessage());
		return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(Map.of("error", ex.getMessage()));
    }
	
	@ExceptionHandler(SQLException.class)
	public ResponseEntity<Map<String, Object>> handleSqlError(SQLException ex) {
	    log.error("SQL Error: " + ex.getMessage());
	    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	            .body(Map.of("error", "데이터베이스 오류가 발생했습니다."));
	}

	@ExceptionHandler(DataAccessException.class)
	public ResponseEntity<Map<String, Object>> handleDataAccess(DataAccessException ex) {
	    log.error("DataAccessException: " + ex.getMessage());
	    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	            .body(Map.of("error", "DB 접근 중 문제가 발생했습니다."));
	}

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleAny(Exception ex) {
	    log.error("500 INTERNAL_SERVER_ERROR: " + ex.getMessage());
    	return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of("error", "서버 오류가 발생했습니다."));
    }
    
    private void clearAuthAndRedirect(HttpServletRequest request,
                                      HttpServletResponse response,
                                      String redirectPath) throws IOException {
        SecurityContextHolder.clearContext();

        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();

        // access 토큰 쿠키 삭제
        Cookie auth = new Cookie("Authorization", null);
        auth.setHttpOnly(true);
        auth.setPath("/");
        auth.setMaxAge(0);
        response.addCookie(auth);

        // refresh 토큰 쿠키 삭제
        Cookie refresh = new Cookie("RefreshToken", null);
        refresh.setHttpOnly(true);
        refresh.setPath("/");
        refresh.setMaxAge(0);
        response.addCookie(refresh);

        response.sendRedirect(redirectPath);
    }

}
