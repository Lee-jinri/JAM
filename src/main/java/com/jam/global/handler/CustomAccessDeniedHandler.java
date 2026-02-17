package com.jam.global.handler;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler{

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
			AccessDeniedException accessDeniedException) throws IOException, ServletException {
		
		String accept = request.getHeader("Accept");
		
		if (accept != null && accept.contains("application/json")) {
	        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
	        response.setContentType("application/json;charset=UTF-8");

	        Map<String, Object> body = new LinkedHashMap<>();
	        body.put("status", 403);
	        body.put("error", "Forbidden");
			body.put("detail", "접근 권한이 없습니다.");
			body.put("forbidden", true);
			body.put("path", request.getRequestURI());

	        new ObjectMapper().writeValue(response.getWriter(), body);
	    } 
	    else {
	        request.setAttribute("msg", "권한이 없는 페이지 입니다.");
	        request.getRequestDispatcher("/error/403").forward(request, response);
	    }
	}
}
