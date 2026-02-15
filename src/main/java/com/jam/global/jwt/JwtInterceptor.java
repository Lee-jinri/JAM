package com.jam.global.jwt;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.jam.member.dto.MemberDto;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtInterceptor implements HandlerInterceptor {
	
	@Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		
		Authentication authentication =
			        SecurityContextHolder.getContext().getAuthentication();

	    if (authentication == null || !authentication.isAuthenticated()) {
	        return true; // 비로그인 사용자도 통과
	    }
	    
	    Object principal = authentication.getPrincipal();

	    if (principal instanceof MemberDto) {
	        MemberDto userInfo = (MemberDto) principal;
	        setRequestAttributes(request, userInfo);
	    }
		return true;
	}

	
	private void setRequestAttributes(HttpServletRequest request, MemberDto userInfo) {
		if(userInfo != null) {
			request.setAttribute("userId", userInfo.getUser_id());
			List<String> roles = userInfo.getRoles();
			log.info("setRequestAttributes :" +roles);
			request.setAttribute("roles", roles);
		}
	}
}
