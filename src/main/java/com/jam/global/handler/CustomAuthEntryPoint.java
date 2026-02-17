package com.jam.global.handler;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomAuthEntryPoint implements AuthenticationEntryPoint  {
	
	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException ex) throws IOException, ServletException {
		String accept = request.getHeader("Accept");
		
		if (accept != null && accept.contains("application/json")) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.setContentType("application/json;charset=UTF-8");

			Map<String, Object> body = new LinkedHashMap<>();
			body.put("status", 401);
			body.put("error", "UNAUTHORIZED");
			body.put("detail", "로그인이 필요한 서비스입니다.");
			body.put("loginRequired", true);
			body.put("path", request.getRequestURI());

			new ObjectMapper().writeValue(response.getWriter(), body);
		} 
	    else {
	        request.setAttribute("msg", "로그인이 필요한 서비스 입니다.");
	        request.getRequestDispatcher("/error/401").forward(request, response);
	    }
	}
}
