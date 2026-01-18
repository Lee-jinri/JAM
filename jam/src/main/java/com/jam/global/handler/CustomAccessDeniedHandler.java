package com.jam.global.handler;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler{

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
			AccessDeniedException accessDeniedException) throws IOException, ServletException {

		response.setStatus(HttpServletResponse.SC_FORBIDDEN);
		response.setContentType("application/json;charset=UTF-8");

		Map<String, Object> body = new LinkedHashMap<>();
		body.put("status", 403);
		body.put("error", "FORBIDDEN");
		body.put("detail", "접근 권한이 없습니다.");
		body.put("forbidden", true);
		body.put("path", request.getRequestURI());

		ObjectMapper mapper = new ObjectMapper();
		response.getWriter().write(mapper.writeValueAsString(body));
	}

}
