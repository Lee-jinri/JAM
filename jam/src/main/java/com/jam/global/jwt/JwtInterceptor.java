package com.jam.global.jwt;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.jam.client.member.vo.MemberVO;

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
		
		MemberVO userInfo = jwtService.getUserInfo(request.getCookies(), request, response);

		log.info(userInfo);
		
		if (userInfo == null) {
			return true; 
		}
		setRequestAttributes(request, userInfo);
		return true;
	}

	
	private void setRequestAttributes(HttpServletRequest request, MemberVO userInfo) {
		if(userInfo != null) {
			request.setAttribute("userId", userInfo.getUser_id());
			List<String> roles = userInfo.getRoles();
			request.setAttribute("roles", roles);
		}
	}
}
