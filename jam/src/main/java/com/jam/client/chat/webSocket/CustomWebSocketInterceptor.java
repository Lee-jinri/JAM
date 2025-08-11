package com.jam.client.chat.webSocket;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.jam.global.jwt.JwtService;

import lombok.extern.log4j.Log4j;

@Log4j
@Component
public class CustomWebSocketInterceptor implements HandshakeInterceptor {

	private final JwtService jwtService;

	public CustomWebSocketInterceptor(JwtService jwtService) {
		this.jwtService = jwtService;
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
	        	userMap = jwtService.getUserInfo(cookies, httpRequest, httpResponse); 
	        }
	        
	        if(userMap != null) {
		        attributes.put("userId", userMap.get("userId"));
		        attributes.put("auth", userMap.get("auth"));
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
