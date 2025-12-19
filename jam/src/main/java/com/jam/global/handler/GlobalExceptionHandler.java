package com.jam.global.handler;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.jam.global.exception.BadRequestException;
import com.jam.global.exception.ConflictException;
import com.jam.global.exception.ForbiddenException;
import com.jam.global.exception.NotFoundException;
import com.jam.global.exception.UnauthorizedException;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	@ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(BadRequestException ex, HttpServletRequest req) {
		log.warn("400 BAD_REQUEST: " + ex.getMessage());
		
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("status", HttpStatus.BAD_REQUEST.value());
		body.put("error", HttpStatus.BAD_REQUEST.getReasonPhrase());
		body.put("detail", ex.getMessage());
		body.put("path", req.getRequestURI());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
	
	@ExceptionHandler(UnauthorizedException.class)
	public ResponseEntity<Map<String, Object>> handleUnauthrized(UnauthorizedException ex, HttpServletRequest req) throws IOException {
		log.warn("401 UNAUTHORIZED: " + ex.getMessage());

		Map<String, Object> body = new LinkedHashMap<>();
		body.put("status", HttpStatus.UNAUTHORIZED.value());
		body.put("error", HttpStatus.UNAUTHORIZED.getReasonPhrase());
		body.put("detail", ex.getMessage());
		body.put("path", req.getRequestURI());
		body.put("loginRequired", true);
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
	}
	
	@ExceptionHandler(ForbiddenException.class)
	public ResponseEntity<Map<String, Object>> handleForbidden(ForbiddenException ex, HttpServletRequest req) throws IOException {
		log.warn("403 FORBIDDEN: "+ ex.getMessage());
		
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("status", HttpStatus.FORBIDDEN.value());
		body.put("error", HttpStatus.FORBIDDEN.getReasonPhrase());
		body.put("detail", ex.getMessage());
		body.put("path", req.getRequestURI());
		body.put("forbidden", true);
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
	}

	@ExceptionHandler(NotFoundException.class)
	public ResponseEntity<Map<String, Object>> handleNotFound(NotFoundException ex, HttpServletRequest req) {
	    log.warn("404 NOT_FOUND: " + ex.getMessage());
		
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("status", HttpStatus.NOT_FOUND.value());
		body.put("error", HttpStatus.NOT_FOUND.getReasonPhrase());
		body.put("detail", ex.getMessage());
		body.put("path", req.getRequestURI());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
	}
	
	@ExceptionHandler(ConflictException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(ConflictException ex, HttpServletRequest req) {
	    log.warn("409 CONFLICT: " + ex.getMessage());

	    Map<String, Object> body = new LinkedHashMap<>();
		body.put("status", HttpStatus.CONFLICT.value());
		body.put("error", HttpStatus.CONFLICT.getReasonPhrase());
		body.put("detail", ex.getMessage());
		body.put("path", req.getRequestURI());
		return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }
	
	@ExceptionHandler(SQLException.class)
	public ResponseEntity<Map<String, Object>> handleSqlError(SQLException ex, HttpServletRequest req) {
	    log.error("SQL Error: " + ex.getMessage());
	    
	    Map<String, Object> body = new LinkedHashMap<>();
		body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
		body.put("error", HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
		body.put("detail", "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
		body.put("path", req.getRequestURI());
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
	}

	@ExceptionHandler(DataAccessException.class)
	public ResponseEntity<Map<String, Object>> handleDataAccess(DataAccessException ex, HttpServletRequest req) {
	    log.error("DataAccessException: " + ex.getMessage());
		
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
		body.put("error", HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
		body.put("detail", "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
		body.put("path", req.getRequestURI());
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
	}

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAny(Exception ex, HttpServletRequest req) {
    	log.warn("500 INTERNAL_SERVER_ERROR: " + ex.getMessage());
		
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
		body.put("error", HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
		body.put("detail", "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
		body.put("path", req.getRequestURI());
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
