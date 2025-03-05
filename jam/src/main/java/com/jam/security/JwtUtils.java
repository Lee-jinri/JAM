package com.jam.security;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.jam.client.member.service.MemberService;
import com.jam.client.member.vo.MemberVO;
import com.jam.security.TokenInfo.TokenStatus;

import io.jsonwebtoken.Claims;
import lombok.extern.log4j.Log4j;

@Log4j
@Component
public class JwtUtils {
	private final JwtTokenProvider jwtTokenProvider; // JWT 검증 클래스
	private final MemberService memberService;

	public JwtUtils(JwtTokenProvider jwtTokenProvider, MemberService memberService) {
		this.jwtTokenProvider = jwtTokenProvider;
		this.memberService = memberService;
	}
	
	public Map<String, String> getUserInfo(Cookie[] cookies, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		Map<String, String> userMap = new HashMap<>();
				
		try {
			String isLogin = (String)request.getSession().getAttribute("isLogin");
			String refreshToken = extractTokenFromCookies(cookies, "RefreshToken");
			
			// refreshToken이 없으면 autoLogin 기본값은 false
			boolean autoLogin = false;
			
			if (refreshToken != null) {
				TokenStatus refreshTokenStatus = jwtTokenProvider.validateToken(refreshToken);
				    
				if(refreshTokenStatus == TokenStatus.VALID)
					autoLogin = jwtTokenProvider.getAutoLoginFromRefreshToken(refreshToken);
			}
			
			if(isLogin == null && autoLogin == false) {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); 
			}
			
			String accessToken = extractTokenFromCookies(cookies, "Authorization");
			
			TokenStatus tokenStatus = jwtTokenProvider.validateToken(accessToken);
			String userId, auth, userName;
			
			switch(tokenStatus) {
			
			case VALID: // accessToken 인증됨
				log.info("accessToken is VALID.");
				Claims claim = jwtTokenProvider.getClaims(accessToken);
				
	            userId = claim.get("sub", String.class);
	            auth = claim.get("auth", String.class);
	            userName = claim.get("userName", String.class);
	            
	            userMap.put("userId", userId);
	            userMap.put("auth", auth);
	            userMap.put("userName", userName);
	            
				break;
			case EXPIRED:
			case EMPTY:
				if (refreshToken == null) {
	                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
	                break;
	            }

				if(autoLogin) {
					userMap = processRefreshToken(refreshToken, response, request, autoLogin);
        		}else 
        			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);  
				
				break;
			case INVALID:
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); 
	        	break;
			}
		}catch(Exception e) {
	        log.error("Exception in preHandle: ", e);
	        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	    }
		return userMap;
	}
	
	private Map<String, String> processRefreshToken(String refreshToken, HttpServletResponse response, HttpServletRequest request, boolean autoLogin) {
		MemberVO member = memberService.getUserInfo(refreshToken);
    	
    	String userId = member.getUser_id();
    	String userName = member.getUser_name();
    	String auth = member.getRole();
    	
    	log.info("member: " + member);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
        	    userId, null,  member.getAuthorities());

        // SecurityContext에 Authentication 설정
        SecurityContextHolder.getContext().setAuthentication(authentication);
    	
        TokenInfo token = jwtTokenProvider.generateToken(authentication, member.getUser_name(), autoLogin);
        
        memberService.addRefreshToken(userId, token.getRefreshToken());
        
        // 쿠키에 jwt 토큰 저장
        addCookieToResponse(response, "Authorization", token.getAccessToken(), 0);
        
        int maxAge = autoLogin? -1 : 24 * 60 * 60;
        addCookieToResponse(response, "RefreshToken", token.getRefreshToken(), maxAge);
        
        Map<String, String> userMap = new HashMap<>();
        userMap.put("userId", userId);
        userMap.put("userName", userName);
        userMap.put("auth", auth);
        
        return userMap;
	}
	
	// 쿠키에서 토큰 추출
	private String extractTokenFromCookies(Cookie[] cookies, String cookieName) {
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookieName.equals(cookie.getName())) {
					return cookie.getValue();
				}
			}
		}
		return null;
	}

	// 쿠키 추가
	private void addCookieToResponse(HttpServletResponse response, String name, String value, int maxAge) {
		Cookie cookie = new Cookie(name, value);
		cookie.setHttpOnly(true);
		cookie.setSecure(true);
		cookie.setPath("/");
		cookie.setMaxAge(maxAge);
		response.addCookie(cookie);
	}
	
}
