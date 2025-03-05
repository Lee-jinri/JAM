package com.jam.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import com.jam.security.TokenInfo.TokenStatus;

@Component
public class JwtAuthenticationFilter extends GenericFilterBean {
/*JWt 인증을 위해 생성되는 토큰 , 요청과 함께 바로 실행 , 요청이 들어오면 헤더에서 토큰 추출*/
	
	private final JwtTokenProvider jwtTokenProvider;
	
	public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
		this.jwtTokenProvider = jwtTokenProvider;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
		String token = resolveToken((HttpServletRequest) request);
		
		TokenStatus tokenStatus = jwtTokenProvider.validateToken(token);
		
		// 토큰 유효성 검사
		if(tokenStatus == TokenStatus.VALID) {
			// 유저의 인증 정보를 가져옴
			Authentication authentication = jwtTokenProvider.getAuthentication(token);
			//  사용자 인증 정보를 현재 스레드의 보안 컨텍스트에 저장 
			//SecurityContextHolder는 Spring Security에서 현재 보안 관련 정보를 관리하는 클래스
			SecurityContextHolder.getContext().setAuthentication(authentication);
			
		}
		// 다음 필터로 요청 전달
		chain.doFilter(request, response);
		
	}

	// 헤더에서 토큰 추출
	private String resolveToken(HttpServletRequest request) {
		
		String bearerToken = request.getHeader("Authorization");
		
		if(StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
			return bearerToken.substring(7);
		}
		
		return null;
	}
	
	
}
