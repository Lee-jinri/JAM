package com.jam.global.handler;

import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import com.jam.global.jwt.JwtTokenProvider;
import com.jam.global.jwt.TokenInfo.TokenStatus;
import com.jam.global.util.AuthClearUtil;
import com.jam.member.service.MemberService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CustomLogoutHandler implements LogoutHandler  {

	private final JwtTokenProvider jwtTokenProvider;
    private final MemberService memberService;

    public CustomLogoutHandler(JwtTokenProvider jwtTokenProvider, @Lazy MemberService memberService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.memberService = memberService;
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
    	try {
    		String accessToken = "";
    		
    		for (Cookie cookie : request.getCookies()) {
				if (cookie.getName().equals("Authorization")) {
					accessToken = cookie.getValue();
				}
			}
    		
    		if (accessToken == null || accessToken.isBlank()) {
    		    log.warn("로그아웃 요청: 토큰이 없음");
    		    return; 
    		}
    		
            TokenStatus tokenStatus = jwtTokenProvider.validateToken(accessToken);
            
            if(tokenStatus == TokenStatus.INVALID) log.warn("토큰 INVALID 상태 (변조/형식 이상)");
            
            // JWT 토큰에서 사용자 정보를 가져옴
            Authentication auth = jwtTokenProvider.getAuthentication(accessToken);
            if (auth == null || !(auth.getPrincipal() instanceof UserDetails)) {
                log.error("Authentication 또는 UserDetails 없음");
                return;
            }
            
            String userId = ((UserDetails) auth.getPrincipal()).getUsername();
            
            if (userId == null) {
                log.error("사용자 아이디 없음");
                return;
            }

            // Refresh Token 삭제
            memberService.deleteRefreshToken(userId);
            
    	}catch(Exception e) {
    		log.error(e.getMessage());
    	} finally {
    		// 세션, JWT 토큰 쿠키 삭제
    		AuthClearUtil.clearAuth(request, response);
        }
    }
}
