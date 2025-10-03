package com.jam.global.handler;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.jam.global.exception.BadRequestException;
import com.jam.global.exception.ConflictException;
import com.jam.global.exception.NotFoundException;
import com.jam.global.exception.UnauthorizedException;

import lombok.extern.log4j.Log4j;

@RestControllerAdvice
@Log4j
public class GlobalExceptionHandler {
	
	@ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
	public void handleAuth(AuthenticationException ex, HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		SecurityContextHolder.clearContext();
	    request.getSession(false).invalidate();
	    
	    // Authorization 쿠키 삭제
	    Cookie cookie = new Cookie("Authorization", null);
	    cookie.setHttpOnly(true);
	    cookie.setPath("/");
	    cookie.setMaxAge(0);  // 쿠키 만료 시간 0으로 설정
	    
	    response.addCookie(cookie);
	    
	    // refreshToken 쿠키 삭제
	    Cookie refreshTokenCookie = new Cookie("RefreshToken", null);
	    refreshTokenCookie.setHttpOnly(true); 
	    refreshTokenCookie.setMaxAge(0);
	    refreshTokenCookie.setPath("/");
	    
	    response.addCookie(refreshTokenCookie);
	    
	    response.sendRedirect("/common/unauthorized");
	}

	@ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
	public void handleDenied(AccessDeniedException ex, HttpServletResponse response) throws IOException {

	    response.sendRedirect("/common/accessDenied");
	}
	
	@ExceptionHandler(UnauthorizedException.class)
	public ResponseEntity<Map<String, String>> handleUnauthrized(UnauthorizedException ex, HttpServletRequest request, HttpServletResponse response) {

		SecurityContextHolder.clearContext();
	    request.getSession(false).invalidate();
	    
	    // Authorization 쿠키 삭제
	    Cookie cookie = new Cookie("Authorization", null);
	    cookie.setHttpOnly(true);
	    cookie.setPath("/");
	    cookie.setMaxAge(0);  // 쿠키 만료 시간 0으로 설정
	    
	    response.addCookie(cookie);
	    
	    // refreshToken 쿠키 삭제
	    Cookie refreshTokenCookie = new Cookie("RefreshToken", null);
	    refreshTokenCookie.setHttpOnly(true); 
	    refreshTokenCookie.setMaxAge(0);
	    refreshTokenCookie.setPath("/");
	    
	    response.addCookie(refreshTokenCookie);
	    
	    log.error(ex.getMessage());
		return ResponseEntity
			.status(HttpStatus.UNAUTHORIZED)
			.body(Map.of("error", ex.getMessage()));
	}
	
	@ExceptionHandler(NotFoundException.class)
	public ResponseEntity<Map<String, String>> handleNotFound(NotFoundException ex) {
	    log.error(ex.getMessage());
		return ResponseEntity
			.status(HttpStatus.NOT_FOUND)
			.body(Map.of("error", ex.getMessage()));
	}
	
	@ExceptionHandler(ConflictException.class)
    public ResponseEntity<Map<String, String>> handleConflict(ConflictException ex) {
	    log.error(ex.getMessage());
		return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(Map.of("error", ex.getMessage()));
    }
	
	@ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, String>> handleBadRequst(BadRequestException ex) {
	    log.error(ex.getMessage());
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleAny(Exception ex) {
	    log.error(ex.getMessage());
    	return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of("error", "서버 오류가 발생했습니다."));
    }
    
}
