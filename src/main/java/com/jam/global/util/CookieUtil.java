package com.jam.global.util;

import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public final class CookieUtil {
	private CookieUtil() {}
	
	public static String getValue(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
		if (cookies != null && cookieName != null) {
			for (Cookie c : cookies) {
				if (cookieName.equals(c.getName())) {
					return c.getValue();
				}
			}
		}
		return null;
	}
	
	public static void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
	    cookie.setAttribute("SameSite", "Lax");
        response.addCookie(cookie);
    }
	
	public static void clearAuthCookies(HttpServletRequest request, HttpServletResponse response) {
	    SecurityContextHolder.clearContext();
	    
	    deleteCookie(response, "Authorization");
	    deleteCookie(response, "RefreshToken");
	}

	public static void deleteCookie(HttpServletResponse response, String name) {
	    Cookie cookie = new Cookie(name, null);
	    cookie.setHttpOnly(true);
	    cookie.setPath("/");
	    cookie.setMaxAge(0);
	    cookie.setAttribute("SameSite", "Lax");
	    response.addCookie(cookie);
	}
}
