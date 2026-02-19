package com.jam.global.util;

import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public final class AuthClearUtil {
	private AuthClearUtil() {}
	
	public static void clearAuth(HttpServletRequest request, HttpServletResponse response) {
	    SecurityContextHolder.clearContext();
	    
	    deleteCookie(response, "Authorization");
	    deleteCookie(response, "RefreshToken");
	}

	private static void deleteCookie(HttpServletResponse response, String name) {
	    Cookie cookie = new Cookie(name, null);
	    cookie.setHttpOnly(true);
	    cookie.setPath("/");
	    cookie.setMaxAge(0);
	    response.addCookie(cookie);
	}
}
