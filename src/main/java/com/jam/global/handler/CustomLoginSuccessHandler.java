package com.jam.global.handler;

import java.io.IOException;

import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.jam.global.jwt.JwtService;
import com.jam.global.jwt.TokenInfo;
import com.jam.global.util.CookieEnum;
import com.jam.global.util.CookieUtil;
import com.jam.global.util.SecurityUtil;
import com.jam.member.service.MemberService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
		memberService.addRefreshToken(userId, SecurityUtil.hashToken(token.getRefreshToken()));

		CookieUtil.addCookie(
				response, 
			    CookieEnum.ACCESS_TOKEN.getName(), 
			    token.getAccessToken(), 
			    CookieEnum.ACCESS_TOKEN.getExpiry()
			);
		
		CookieEnum refreshConfig = CookieEnum.getRefreshToken(autoLogin);
		CookieUtil.addCookie(
				response, 
				refreshConfig.getName(), 
				token.getRefreshToken(), 
				refreshConfig.getExpiry()
			);

		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/json;charset=UTF-8");

		String redirectUrl = request.getParameter("redirect");
		if (redirectUrl == null || redirectUrl.isBlank() || redirectUrl.startsWith("http")) {
	        redirectUrl = "/";
	    }
		
	    response.setContentType("application/json;charset=UTF-8");
		response.getWriter().write("{ \"redirect\": \"" + redirectUrl + "\" }");
	}
}
