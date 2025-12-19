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
import com.jam.global.jwt.JwtService;
import com.jam.global.jwt.TokenInfo;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {

	private final MemberService memberService;
	private final JwtService jwtService;

	public CustomLoginSuccessHandler(@Lazy MemberService memberService, JwtService jwtService) {
		this.memberService = memberService;
		this.jwtService = jwtService;
	}

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException {

		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		String userId = userDetails.getUsername();

		boolean autoLogin = Boolean.parseBoolean(request.getParameter("autoLogin"));

		TokenInfo token = jwtService.generateTokenFromAuthentication(authentication, autoLogin, "local");
		memberService.addRefreshToken(userId, token.getRefreshToken());

		Cookie accessTokenCookie = new Cookie("Authorization", token.getAccessToken());
		accessTokenCookie.setHttpOnly(true);
		accessTokenCookie.setPath("/");
		accessTokenCookie.setMaxAge(3 * 60 * 60); // 3시간
		response.addCookie(accessTokenCookie);

		Cookie refreshTokenCookie = new Cookie("RefreshToken", token.getRefreshToken());
		refreshTokenCookie.setHttpOnly(true);
		refreshTokenCookie.setPath("/");
		refreshTokenCookie.setMaxAge(autoLogin ? 30 * 24 * 60 * 60 : 24 * 60 * 60);
		response.addCookie(refreshTokenCookie);

		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/json;charset=UTF-8");

		String prevPage = (String) request.getSession().getAttribute("prevPage");
		String redirectUrl = (prevPage != null && !prevPage.isBlank()) ? prevPage : "/";

		response.getWriter().write("{ \"redirect\": \"" + redirectUrl + "\" }");
	}
}
