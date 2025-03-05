package com.jam.security;

import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.velocity.runtime.log.Log;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.jam.client.member.service.MemberService;
import com.jam.client.member.vo.MemberVO;
import com.jam.security.TokenInfo.TokenStatus;

import io.jsonwebtoken.Claims;
import lombok.extern.log4j.Log4j;

@Log4j
@Component
public class JwtInterceptor implements HandlerInterceptor {

	private final JwtUtils jwtUtils;

	
	public JwtInterceptor(JwtUtils jwtUtils) {
		this.jwtUtils = jwtUtils;
	}
	
	@Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		
		Map<String, String> userMap = jwtUtils.getUserInfo(request.getCookies(), request, response);
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
