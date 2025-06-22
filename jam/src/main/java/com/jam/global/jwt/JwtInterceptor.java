package com.jam.global.jwt;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import lombok.extern.log4j.Log4j;

@Log4j
@Component
public class JwtInterceptor implements HandlerInterceptor {

	private final JwtService jwtService;

	
	public JwtInterceptor(JwtService jwtService) {
		this.jwtService = jwtService;
	}
	
	@Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		
		Map<String, String> userMap = jwtService.getUserInfo(request.getCookies(), request, response);
		
		if (userMap == null || userMap.isEmpty()) {
			return true; 
		}
		
		setRequestAttributes(request, userMap);
		
		return true;
	}

	
	private void setRequestAttributes(HttpServletRequest request, Map<String, String> userMap) {
		if(userMap != null) {
			request.setAttribute("userId", userMap.get("userId"));
	    	request.setAttribute("userName", userMap.get("userName"));
	    	request.setAttribute("auth", userMap.get("auth"));
		}
	}
}
