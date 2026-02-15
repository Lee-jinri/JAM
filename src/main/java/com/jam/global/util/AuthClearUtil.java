package com.jam.global.util;

import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public final class AuthClearUtil {
	private AuthClearUtil() {}
	
	public static void clearAuth(HttpServletRequest request, HttpServletResponse response) {
	    SecurityContextHolder.clearContext();
	    HttpSession session = request.getSession(false);
	    if(session != null) session.invalidate();
	    
	    deleteCookie(response, "Authorization");
	    deleteCookie(response, "RefreshToken");
	    deleteCookie(response, "JSESSIONID");
	}

	private static void deleteCookie(HttpServletResponse response, String name) {
	    Cookie cookie = new Cookie(name, null);
	    cookie.setHttpOnly(true);
	    cookie.setPath("/");
	    cookie.setMaxAge(0);
	    response.addCookie(cookie);
	}
}
