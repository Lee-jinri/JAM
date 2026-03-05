package com.jam.global.jwt;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.jam.global.jwt.TokenInfo.TokenStatus;
import com.jam.global.util.AuthClearUtil;
import com.jam.global.util.SecurityUtil;
import com.jam.member.dto.MemberDto;
import com.jam.member.service.MemberService;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtService {
	
	private final JwtTokenProvider jwtTokenProvider;
	private final MemberService memberService;
	private final StringRedisTemplate redisTemplate;
	
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
	public Authentication getAuthentication(Cookie[] cookies, HttpServletRequest request, HttpServletResponse response){
				
		try {
			String accessToken = extractToken(cookies, "Authorization");
			
			TokenStatus tokenStatus = jwtTokenProvider.validateToken(accessToken);
			
			// tokenStatus가 null인 경우를 대비한 기본값 처리
			if (tokenStatus == null) tokenStatus = TokenStatus.EMPTY;
			
			switch(tokenStatus) {
				// accessToken 인증됨
				case VALID: 
					MemberDto userInfo = extractUserInfoFromToken(accessToken);
					Authentication authentication = new UsernamePasswordAuthenticationToken(
					        userInfo,
					        null,
					        userInfo.getAuthorities()
					    );
					return authentication;
				case EXPIRED:
				case EMPTY:
					String refreshToken = extractToken(cookies, "RefreshToken");
					
					// 비로그인 사용자
				    if (accessToken == null && refreshToken == null) {
				        return null;
				    }
				    
				    // 토큰이 있는데 유효하지 않거나 만료된 경우에만 정리
					if (refreshToken == null ||jwtTokenProvider.validateToken(refreshToken) != TokenStatus.VALID) {
						
						AuthClearUtil.clearAuth(request, response);
						return null;
					}
	
					boolean autoLogin = jwtTokenProvider.getAutoLoginFromRefreshToken(refreshToken);
					
					if (autoLogin) {
						log.info("[JWT] AccessToken 만료, refreshToken 재발급 시도 autoLogin: " + autoLogin);
						authentication = processRefreshToken(refreshToken, response, request, true);
						
						return authentication;
					}else {
						AuthClearUtil.clearAuth(request, response);
					}
					
					return null;
				case INVALID:
					log.warn("[JWT] 유효하지 않은 토큰");
					
					AuthClearUtil.clearAuth(request, response);
	                return null;
			}
		}catch(Exception e) {
	        log.error("[JWT] 내부 처리 중 예외 발생", e);
	        AuthClearUtil.clearAuth(request, response);
	    }
		
		return null;
	}
	
	/**
	 * 토큰에 저장된 사용자 정보를 반환
	 * 
	 * @param 	accessToken 사용자 정보 추출용
	 * @return ID, 닉네임, 회사명, 권한이 포함된 MemberDto 객체
	 */
	// NOTE: 토큰 기반 사용자 정보 (DB 최신 상태와 다를 수 있음)
	public MemberDto extractUserInfoFromToken(String accessToken){
		Claims claim = jwtTokenProvider.getClaims(accessToken);
		
		MemberDto userInfo = new MemberDto();
		
		userInfo.setUser_id(claim.get("sub", String.class));
		userInfo.setUser_name(claim.get("userName", String.class));
		userInfo.setCompany_name(claim.get("companyName", String.class));

        @SuppressWarnings("unchecked")
		List<String> authList = claim.get("auth", List.class);
        
		userInfo.setRoles(authList);
		
		return userInfo;
	}
	
	/**
     * 리프레시 토큰을 검증하고 새로운 액세스 토큰을 발급합니다.
     * 
     * [동시성 제어]
     * 동일 사용자의 여러 요청이 동시에 들어올 경우 중복 갱신을 방지하기 위해 synchronized 처리.
     * 첫번째 요청이 토큰을 갱신하면 아주 짧은 시간 차로 들어온 다음 요청은 
     * DB에 저장된 Refresh Token과의 대조와 Redis의 유예 기간(10초)을 확인하여 인증을 승인합니다.
     * 
     * @param refreshToken 클라이언트로부터 전달받은 리프레시 토큰 원본
     * @return 인증 객체(Authentication), 검증 실패 시 null
     */
	private synchronized Authentication processRefreshToken(String refreshToken, HttpServletResponse response, HttpServletRequest request, boolean autoLogin) {	    
		log.info("processRefreshToken 진입");
		
		String userId = jwtTokenProvider.extractUserId(refreshToken);
		String jti = jwtTokenProvider.extractJti(refreshToken);
		if (userId == null || userId.isEmpty() || jti == null) return null;

		String key = "refresh:prev:" + userId + ":" + jti;
		
		// DB에 저장된 refreshToken과 사용자가 보낸 토큰을 대조
		String encodedRefreshToken = memberService.getRefreshToken(userId);
		if (encodedRefreshToken == null || !SecurityUtil.matches(refreshToken, encodedRefreshToken)) { 
			log.error("[JWT] DB RefreshToken 불일치. encodedRefreshToken: {}, refreshToken: {}", encodedRefreshToken, refreshToken);
			
			if (redisTemplate.hasKey(key)) {
		        log.info("[JWT] 동시 요청으로 인해 이미 갱신 처리된 토큰. 인증을 허용합니다.");
		        return createAuthentication(userId);
		    }
			
			return null;
		}
		
		return renewTokens(userId, refreshToken, jti, response, autoLogin);
	}
	
	/**
     * Security Authentication 객체를 생성합니다.
     * (토큰 갱신 없이 인증 정보만 필요할 때 사용)
     */
	private Authentication createAuthentication(String userId) {
		MemberDto userInfo = memberService.findByUserInfo(userId);
    	
    	Authentication authentication = new UsernamePasswordAuthenticationToken(
    			userInfo, null,  userInfo.getAuthorities());
		
		return authentication;
	}
	
	/**
     * 새로운 토큰(Access/Refresh)을 발급하고 DB와 쿠키를 업데이트합니다.
     * 발급 후 이전 토큰의 JTI를 Redis에 10초간 기록하여 동시 요청에 대비합니다.
     */
	private Authentication renewTokens(String userId, String oldToken, String jti, HttpServletResponse response, boolean autoLogin) {
	    MemberDto userInfo = memberService.findByUserInfo(userId);
	    String loginType = jwtTokenProvider.extractLoginType(oldToken);
	    
	    Authentication auth = new UsernamePasswordAuthenticationToken(userInfo, null, userInfo.getAuthorities());
	    TokenInfo newToken = jwtTokenProvider.generateToken(auth, autoLogin, loginType);

	    // DB 업데이트 및 쿠키 설정
	    memberService.addRefreshToken(userId, SecurityUtil.hashToken(newToken.getRefreshToken()));
	    
	    addCookieToResponse(response, "Authorization", newToken.getAccessToken(), 3 * 60 * 60);
	    
	    int maxAge = autoLogin ? 30 * 24 * 60 * 60 : 24 * 60 * 60;
	    addCookieToResponse(response, "RefreshToken", newToken.getRefreshToken(), maxAge);

	    // 갱신 전 토큰의 JTI를 Redis에 기록하여 유예 기간 부여.
	    // 동시 요청 시 이미 갱신된 이전 토큰으로 접근하는 스레드를 10초간 허용.
	    String key = "refresh:prev:" + userId + ":" + jti;
	    redisTemplate.opsForValue().set(key, "1", 10, TimeUnit.SECONDS);

        log.info("[JWT] 새로운 AccessToken/RefreshToken 발급 - userId: " + userId + " loginType: " + loginType);
	    return auth;
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
            log.warn("유효하지 않은 토큰입니다.");

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
            log.warn("유효하지 않은 토큰입니다.");

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
	public List<String> extractUserRole(HttpServletRequest request, HttpServletResponse response, String accessToken) {
		
		if (accessToken == null || jwtTokenProvider.validateToken(accessToken) != TokenStatus.VALID) {
			AuthClearUtil.clearAuth(request, response);
            log.warn("유효하지 않은 토큰입니다.");

            return null;
        }
		
		return jwtTokenProvider.extractUserRole(accessToken);
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
            log.warn("유효하지 않은 토큰입니다.");

            return null;
        }
		
		return jwtTokenProvider.extractAutoLogin(token); 
	}

	public TokenStatus validateToken(String token) {
		return jwtTokenProvider.validateToken(token);
	}
}
