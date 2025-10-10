package com.jam.global.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.security.core.context.SecurityContextHolder;

public final class AuthClearUtil {
	private AuthClearUtil() {}
	
	public static void clearAuth(HttpServletRequest request, HttpServletResponse response) {
	    SecurityContextHolder.clearContext();
	    HttpSession session = request.getSession(false);
	    if(session != null) session.invalidate();
	    
	    // Authorization 쿠키 삭제
	    Cookie cookie = new Cookie("Authorization", null);
	    cookie.setHttpOnly(true);
	    cookie.setPath("/");
	    cookie.setMaxAge(0);  // 쿠키 만료 시간 0으로 설정
	    
	    response.addCookie(cookie);
	    
	    // refreshToken 쿠키 삭제
	    Cookie refreshTokenCookie = new Cookie("RefreshToken", null);
	    refreshTokenCookie.setHttpOnly(true); 
	    refreshTokenCookie.setMaxAge(0);
	    refreshTokenCookie.setPath("/");
	    
	    response.addCookie(refreshTokenCookie);
	}
}
