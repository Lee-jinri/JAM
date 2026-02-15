package com.jam.global.security;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.jam.global.jwt.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	private final JwtService jwtService;
	public JwtAuthenticationFilter(JwtService jwtService) {
		this.jwtService = jwtService;
	}
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		
		if (request.getCookies() != null) {
			Authentication authentication =
				jwtService.getAuthentication(
					request.getCookies(),
					request,
					response
				);

			if (authentication != null && authentication.isAuthenticated()) {
				SecurityContextHolder.getContext().setAuthentication(authentication);
			} else {
				SecurityContextHolder.clearContext();
			}
		}
		
		// 다음 필터로 요청 전달
		filterChain.doFilter(request, response);
	}
	
	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		
	    // 정적 리소스나 공용 API는 필터를 통과시키지 않음
	    String path = request.getServletPath();
	    
	    boolean isStaticResource = path.startsWith("/css/") || 
	                               path.startsWith("/js/") || 
	                               path.startsWith("/images/") || 
	                               path.startsWith("/favicon.ico") ||
	                               path.startsWith("/fonts/");

	    boolean isLoginProcess = path.equals("/api/member/login-process");

	    return isStaticResource || isLoginProcess;
	}
}
