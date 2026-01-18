package com.jam.global.handler;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class CustomAuthEntryPoint implements AuthenticationEntryPoint  {
	
	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException ex) throws IOException {
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType("application/json;charset=UTF-8");

		Map<String, Object> body = new LinkedHashMap<>();
		body.put("status", 401);
		body.put("error", "UNAUTHORIZED");
		body.put("detail", "로그인이 필요한 서비스입니다.");
		body.put("loginRequired", true);
		body.put("path", request.getRequestURI());

		ObjectMapper mapper = new ObjectMapper();
		response.getWriter().write(mapper.writeValueAsString(body));
	}
}
