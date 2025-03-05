package com.jam.config;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.runtime.log.Log;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.jam.client.member.service.MemberService;
import com.jam.security.JwtTokenProvider;
import com.jam.security.JwtUtils;

import lombok.extern.log4j.Log4j;

@Log4j
@Component
public class CustomWebSocketInterceptor implements HandshakeInterceptor {

	private final JwtUtils jwtUtils;

	public CustomWebSocketInterceptor(JwtUtils jwtUtils) {
		this.jwtUtils = jwtUtils;
	}
	
	@Override
	public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
			Map<String, Object> attributes) throws Exception {
		
		if (request instanceof ServletServerHttpRequest) {
			
	        ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
	        HttpServletRequest httpRequest = servletRequest.getServletRequest();

	        ServletServerHttpResponse servletResponse = (ServletServerHttpResponse) response;
	        HttpServletResponse httpResponse = servletResponse.getServletResponse();
	        
	        Map<String, String> userMap = new HashMap<>();
	        
	        Cookie[] cookies = httpRequest.getCookies();
	        if (cookies != null) {
	        	userMap = jwtUtils.getUserInfo(cookies, httpRequest, httpResponse); 
	        }
	        
	        if(userMap != null) {
		        // attributes에 사용자 ID 저장
		        attributes.put("userId", userMap.get("userId"));
		        attributes.put("userName", userMap.get("userName"));
	        }
	    }
	    return true;
	}

	@Override
	public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
			Exception exception) {
		// TODO Auto-generated method stub
		
	}

	

}
