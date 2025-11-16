package com.jam.global.jwt;

import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.jam.client.member.service.MemberService;
import com.jam.client.member.vo.MemberVO;
import com.jam.global.jwt.TokenInfo.TokenStatus;
import com.jam.global.util.AuthClearUtil;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;

@Log4j
@Component
@RequiredArgsConstructor
public class JwtService {
	
	private final JwtTokenProvider jwtTokenProvider;
	private final MemberService memberService;
	
	/**
	 * 쿠키에 저장된 토큰을 이용해 로그인된 사용자 정보를 반환
	 * 
	 * - AccessToken이 유효하면 사용자 정보를 바로 추출합니다.
	 * - AccessToken이 만료되었고, RefreshToken이 존재하며 autoLogin=true인 경우
	 *   → 토큰을 재발급하고 사용자 정보를 추출합니다.
	 * - 토큰이 유효하지 않으면 401 응답을 설정합니다.
	 * 
	 * @param cookies    요청에 포함된 쿠키 배열
	 * @param request    HttpServletRequest (세션 접근용)
	 * @param response   HttpServletResponse (401 응답 처리 등)
	 * @return           userId, auth가 포함된 Map (로그인된 경우), 실패 시 빈 Map
	 * @throws Exception 내부 처리 중 예외 발생 시
	 */
	public MemberVO getUserInfo(Cookie[] cookies, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		try {
			String accessToken = extractToken(cookies, "Authorization");
			
			TokenStatus tokenStatus = jwtTokenProvider.validateToken(accessToken);
			
			log.info("[JWT] AccessToken 상태: " + tokenStatus);

			switch(tokenStatus) {
				// accessToken 인증됨
				case VALID: 
					MemberVO userInfo = extractUserInfoFromToken(accessToken);
					Authentication authentication = new UsernamePasswordAuthenticationToken(
					        userInfo,
					        null,
					        userInfo.getAuthorities()
					    );
					    SecurityContextHolder.getContext().setAuthentication(authentication);
					return userInfo;
				case EXPIRED:
				case EMPTY:
					String refreshToken = extractToken(cookies, "RefreshToken");
					
					if (refreshToken == null || 
					    jwtTokenProvider.validateToken(refreshToken) != TokenStatus.VALID) {
						
						log.warn("[JWT] 자동로그인 실패: refreshToken 없음.");
						
						AuthClearUtil.clearAuth(request, response);
						return null;
					}
	
					boolean autoLogin = jwtTokenProvider.getAutoLoginFromRefreshToken(refreshToken);
					
					if (autoLogin) {
						log.info("[JWT] AccessToken 만료, refreshToken 재발급 시도 autoLogin: " + autoLogin);
						return processRefreshToken(refreshToken, response, request, true);
					}
					
					log.warn("[JWT] 자동로그인 실패: 자동로그인 설정되지 않음.");
					
					AuthClearUtil.clearAuth(request, response);
					return null;
				case INVALID:
					log.warn("[JWT] 유효하지 않은 토큰");
					
					AuthClearUtil.clearAuth(request, response);
					response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
	                return new MemberVO();
			}
		}catch(Exception e) {
	        log.error("[JWT] 내부 처리 중 예외 발생", e);
	        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	    }
		
		return null;
	}
	
	public MemberVO extractUserInfoFromToken(String accessToken){
		Claims claim = jwtTokenProvider.getClaims(accessToken);
		
		MemberVO userInfo = new MemberVO();
		
		userInfo.setUser_id(claim.get("sub", String.class));
		userInfo.setUser_name(claim.get("userName", String.class));
		userInfo.setCompany_name(claim.get("companyName", String.class));

        @SuppressWarnings("unchecked")
		List<String> authList = claim.get("auth", List.class);
        
		userInfo.setRoles(authList);
		
		return userInfo;
	}
	
	private MemberVO processRefreshToken(String refreshToken, HttpServletResponse response, HttpServletRequest request, boolean autoLogin) {
		log.info("processRefreshToken 진입");
		
		// 1. RefreshToken으로 사용자 정보 가져옴.
		String userId = memberService.findUserIdByRefreshToken(refreshToken);
		
		if (userId == null || userId.isEmpty()) {
		    log.error("[JWT] refreshToken으로 사용자 정보 조회 실패");
		    return new MemberVO();
		}
		
		MemberVO userInfo = memberService.findByUserInfo(userId);

    	String loginType = jwtTokenProvider.extractLoginType(refreshToken);

    	// 2. SecurityContext에 Authentication 설정
    	Authentication authentication = new UsernamePasswordAuthenticationToken(
    			userInfo, null,  userInfo.getAuthorities());
    	
        SecurityContextHolder.getContext().setAuthentication(authentication);
    	
        // 3. 새로운 토큰 갱신
        TokenInfo token = jwtTokenProvider.generateToken(authentication, autoLogin, loginType);
        
        // 4. 새로운 JWT 토큰 쿠키에 저장 및 RefreshToken DB에 저장
        // MaxAge : 3시간
        addCookieToResponse(response, "Authorization", token.getAccessToken(), 3 * 60 * 60);
        
        memberService.addRefreshToken(userId, token.getRefreshToken());
        
        int maxAge = autoLogin? 30 * 24 * 60 * 60 : 24 * 60 * 60;
        addCookieToResponse(response, "RefreshToken", token.getRefreshToken(), maxAge);
        
        log.info("[JWT] 새로운 AccessToken/RefreshToken 발급 - userId: " + userId + " loginType: " + loginType);

        return userInfo;
	}
	
	// 쿠키에서 토큰 추출
	public String extractToken(Cookie[] cookies, String tokenName) {
        if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals(tokenName)) {
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
		cookie.setPath("/");
		cookie.setMaxAge(maxAge);
		
		response.addCookie(cookie);
	}
	
	/**
	 * AccessToken에서 사용자 아이디(userId)를 추출
	 * 
	 * - 토큰이 없거나 유효하지 않으면 세션을 만료시키고 null을 반환합니다.
	 * 
	 * @param request HttpServletRequest (쿠키에서 AccessToken을 추출)
	 * @return 로그인 타입 문자열 또는 유효하지 않을 경우 null
	 */
	public String extractUserId(HttpServletRequest request, HttpServletResponse response, Cookie[] cookies) {
		String token = extractToken(cookies, "Authorization");
		
		if (token == null || jwtTokenProvider.validateToken(token) != TokenStatus.VALID) {
			AuthClearUtil.clearAuth(request, response);
            log.warn("유효하지 않은 토큰입니다: " + token);

            return null;
        }
		
		return jwtTokenProvider.extractUserId(token);
	}
	
	/**
	 * AccessToken에서 로그인 타입(loginType)을 추출
	 * 
	 * - 로그인 타입 예시: "kakao", "naver", "local"
	 * - 주로 로그아웃 처리 및 마이페이지(account) 요청 시 사용됩니다.
	 * - 토큰이 없거나 유효하지 않으면 세션을 만료시키고 null을 반환합니다.
	 *
	 * @param request HttpServletRequest (쿠키에서 AccessToken을 추출)
	 * @return 로그인 타입 문자열 또는 유효하지 않을 경우 null
	 */
    public String extractLoginType(HttpServletRequest request, HttpServletResponse response, Cookie[] cookies) {
        
    	String token = extractToken(cookies, "Authorization");
    	
    	if (token == null || jwtTokenProvider.validateToken(token) != TokenStatus.VALID) {
    		AuthClearUtil.clearAuth(request, response);
            log.warn("유효하지 않은 토큰입니다: " + token);

            return null;
        }

        return jwtTokenProvider.extractLoginType(token);
    }


    /**
     * 인증 객체(Authentication)로부터 JWT 토큰을 발급
     *
     * @param authentication 인증된 사용자 정보
     * @param autoLogin 자동 로그인 여부 (refreshToken 유효기간 결정)
     * @param loginType 로그인 방식 (예: kakao, naver, local)
     * @return 발급된 accessToken, refreshToken을 포함한 TokenInfo
     */
	public TokenInfo generateTokenFromAuthentication(Authentication authentication, boolean autoLogin, String loginType){
    	
	    TokenInfo token = jwtTokenProvider.generateToken(authentication, autoLogin, loginType);

		return token;
    }

	/**
	 * JWT 토큰에서 사용자 역할(Role)을 추출합니다.
	 *
	 * @param token JWT 문자열
	 * @return 사용자의 역할 (예: "ROLE_USER", "ROLE_ADMIN")
	 */
	public List<String> extractUserRole(HttpServletRequest request, HttpServletResponse response, Cookie[] cookies) {
		
		String token = extractToken(cookies, "Authorization");
		
		if (token == null || jwtTokenProvider.validateToken(token) != TokenStatus.VALID) {
			AuthClearUtil.clearAuth(request, response);
            log.warn("유효하지 않은 토큰입니다: " + token);

            return null;
        }
		
		return jwtTokenProvider.extractUserRole(token);
	}
	
	
	/**
	 * JWT 토큰에서 자동 로그인 여부를 추출합니다.
	 * 
	 * @param token JWT 문자열
	 * @return 사용자의 자동 로그인 여부 (true, false)
	 * */
	public Boolean extractAutoLogin(HttpServletRequest request, HttpServletResponse response, Cookie[] cookies) {
		String token = extractToken(cookies, "RefreshToken");
		
		if (token == null || jwtTokenProvider.validateToken(token) != TokenStatus.VALID) {
			AuthClearUtil.clearAuth(request, response);
            log.warn("유효하지 않은 토큰입니다: " + token);

            return null;
        }
		
		return jwtTokenProvider.extractAutoLogin(token); 
	}
}
