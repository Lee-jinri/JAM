package com.jam.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import com.jam.client.member.service.MemberService;
import com.jam.security.TokenInfo.TokenStatus;

public class CustomLogoutHandler implements LogoutHandler  {

	private final JwtTokenProvider jwtTokenProvider;
    private final MemberService memberService;

    public CustomLogoutHandler(JwtTokenProvider jwtTokenProvider, MemberService memberService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.memberService = memberService;
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        // Authorization 헤더에서 JWT 토큰을 가져옴
        String accessToken = request.getHeader("Authorization");
        
        TokenStatus tokenStatus = jwtTokenProvider.validateToken(accessToken);
        if (tokenStatus == TokenStatus.VALID) {
            // JWT 토큰에서 사용자 정보를 가져옴
            Authentication auth = jwtTokenProvider.getAuthentication(accessToken);
            String userId = ((UserDetails) auth.getPrincipal()).getUsername();

            // Refresh Token 삭제 로직
            memberService.deleteRefreshToken(userId);
        }
    }
}
