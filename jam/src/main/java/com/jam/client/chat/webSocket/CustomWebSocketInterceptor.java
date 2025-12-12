package com.jam.client.chat.webSocket;

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

import com.jam.client.member.vo.MemberVO;
import com.jam.global.jwt.JwtService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
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
	        
	        MemberVO userInfo = new MemberVO();
	        
	        Cookie[] cookies = httpRequest.getCookies();
	        
	        if (cookies != null) {
	        	userInfo = jwtService.getUserInfo(cookies, httpRequest, httpResponse); 
	        }
	        
	        if(userInfo != null) {
		        attributes.put("userId", userInfo.getUser_id());
		        attributes.put("auth", userInfo.getRoles());
	        }
	        
	        log.info("userinfo:" ,userInfo);
	    }
	    return true;
	}

	@Override
	public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
			Exception exception) {
		// TODO Auto-generated method stub
		
	}
}
