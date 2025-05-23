package com.jam.global.handler;

import java.io.IOException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.jam.client.member.service.MemberService;
import com.jam.global.jwt.JwtTokenManager;
import com.jam.global.jwt.TokenInfo;

@Component
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {

	private final MemberService memberService;
    private final JwtTokenManager jwtTokenManager;

    public CustomLoginSuccessHandler(@Lazy MemberService memberService,
                                     JwtTokenManager jwtTokenManager) {
        this.memberService = memberService;
        this.jwtTokenManager = jwtTokenManager;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
    	
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String userId = userDetails.getUsername();

        boolean autoLogin = Boolean.parseBoolean(request.getParameter("autoLogin"));

        TokenInfo token = jwtTokenManager.generateTokenFromAuthentication(authentication, autoLogin, "local");
        memberService.addRefreshToken(userId, token.getRefreshToken());

        // FIXME: AccessToken은 쿠키❌ Authorization 헤더로 변경할 것
        // token.getAuthorizationHeader() = Bearer + accessToken
        // refreshToken은 쿠키 O
        Cookie accessTokenCookie = new Cookie("Authorization", token.getAccessToken());
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setPath("/");
        response.addCookie(accessTokenCookie);

        Cookie refreshTokenCookie = new Cookie("RefreshToken", token.getRefreshToken());
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(autoLogin ? 30 * 24 * 60 * 60 : 24 * 60 * 60);
        response.addCookie(refreshTokenCookie);

        request.getSession().setAttribute("userId", userId);
        request.getSession().setAttribute("userName", memberService.getUserName(userId));

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");

        String prevPage = (String) request.getSession().getAttribute("prevPage");
        String redirectUrl = (prevPage != null && !prevPage.isBlank()) ? prevPage : "/";

        response.getWriter().write("{ \"redirect\": \"" + redirectUrl + "\" }"); 
        
    }
}
