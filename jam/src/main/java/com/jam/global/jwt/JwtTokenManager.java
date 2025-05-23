package com.jam.global.jwt;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.jam.global.jwt.TokenInfo.TokenStatus;

@Component
public class JwtTokenManager {

	/**
	 * JwtTokenManager
	 * 
	 * - JwtTokenProvider를 기반으로 실제 인증 로직을 조율하는 클래스
	 * - 토큰 검증, 만료 처리, 새로운 토큰 재발급 등의 흐름을 담당
	 * - 단, 내부적으로 MemberService에 직접 의존하지 않음
	 *   → 이를 통해 SecurityConfig와의 순환 참조를 방지함
	 * 
	 * JwtTokenProvider = 발급/검증 등
	 * JwtTokenManager  = 인증 흐름 관리
	 */
	
	
	private final JwtTokenProvider jwtTokenProvider;

    public JwtTokenManager(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public boolean isTokenValid(String token) {
        return jwtTokenProvider.validateToken(token) == TokenStatus.VALID;
    }

    public String regenerateAccessToken(String refreshToken, Authentication auth, boolean autoLogin) {
        // 검증 및 재발급 흐름 조절만 담당
        return jwtTokenProvider.generateToken(auth, autoLogin, "local").getAccessToken();
    }
    
    /**
     * 소셜 로그인에서 사용하는 토큰 발급 로직
     * 걍 jwtTokenProvider에서 사용하면 움 안될 것 같아서 우회함 */
    public TokenInfo generateTokenFromAuthentication(Authentication authentication, boolean autoLogin, String loginType){
    	
	    // 토큰 발급
	    TokenInfo token = jwtTokenProvider.generateToken(authentication, autoLogin, loginType);
	    
		return token;
    }

}
