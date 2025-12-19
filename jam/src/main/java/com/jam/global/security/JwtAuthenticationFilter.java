package com.jam.global.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.filter.OncePerRequestFilter;

import com.jam.global.jwt.JwtService;

@Component
//public class JwtAuthenticationFilter extends GenericFilterBean {
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtService jwtService;
	public JwtAuthenticationFilter(JwtService jwtService) {
		this.jwtService = jwtService;
	}
 /*
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
		if (((HttpServletRequest) request).getCookies() != null) {
			Authentication authentication = jwtService.getAuthentication(
					((HttpServletRequest) request).getCookies(),
					(HttpServletRequest) request,
					(HttpServletResponse) response);
			
			if(authentication != null) SecurityContextHolder.getContext().setAuthentication(authentication);
		}
		
		// 다음 필터로 요청 전달
		chain.doFilter(request, response);
	}
*/
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

			if (authentication != null) {
				SecurityContextHolder.getContext().setAuthentication(authentication);
			}
		}
		
		// 다음 필터로 요청 전달
		filterChain.doFilter(request, response);
	}
	
	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		return request.getServletPath().equals("/api/member/login-process");
	}

}
