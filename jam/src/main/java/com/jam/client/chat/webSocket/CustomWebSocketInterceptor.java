package com.jam.client.chat.webSocket;

import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.jam.client.member.vo.MemberVO;
import com.jam.global.jwt.JwtTokenProvider;
import com.jam.global.jwt.TokenInfo.TokenStatus;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CustomWebSocketInterceptor implements HandshakeInterceptor {

	private final JwtTokenProvider jwtTokenProvider;

	public CustomWebSocketInterceptor(JwtTokenProvider jwtTokenProvider) {
		this.jwtTokenProvider = jwtTokenProvider;
	}
	
	@Override
	public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
			Map<String, Object> attributes) throws Exception {
		
		ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
		HttpServletRequest httpRequest = servletRequest.getServletRequest();
		
		Cookie[] cookies = httpRequest.getCookies();
		String accessToken = null;

		if (cookies != null) {
			for (Cookie c : cookies) {
				if ("Authorization".equals(c.getName())) {
					accessToken = c.getValue();
					break;
				}
			}
		}
		
		// 토큰 없거나 유효하지 않으면 연결 거부
		if (accessToken == null ||
			jwtTokenProvider.validateToken(accessToken) != TokenStatus.VALID) {
			return false;
		}
		
		Authentication auth = jwtTokenProvider.getAuthentication(accessToken);
		if(auth == null) return false;
		
		MemberVO user = (MemberVO) auth.getPrincipal();
		
		attributes.put("userId", user.getUser_id());
		attributes.put("roles", user.getRoles());
		
		return true;
	}

	@Override
	public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
			Exception exception) {
		// TODO Auto-generated method stub
		
	}
}
