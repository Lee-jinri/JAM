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
