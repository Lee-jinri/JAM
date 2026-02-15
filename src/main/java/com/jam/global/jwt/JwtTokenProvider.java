package com.jam.global.jwt;

import java.security.Key;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jam.global.jwt.TokenInfo.TokenStatus;
import com.jam.member.dto.MemberDto;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JwtTokenProvider {
	
	private final Key key;

    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey) {
        
    	byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }
 
    // 회원 정보로 AccessToken, RefreshToken 을 생성하는 메서드
    public TokenInfo generateToken(Authentication authentication, boolean autoLogin, String loginType) {
    	
        // 회원의 권한 가져오기
        List<String> auth = authentication.getAuthorities().stream()
        		.map(GrantedAuthority::getAuthority)
        		.toList();
        
        if (auth.isEmpty()) {
        	auth = List.of("ROLE_USER");
    	}
        MemberDto member = (MemberDto) authentication.getPrincipal(); 
        
        String userName = member.getUser_name();
        String companyName = member.getCompany_name();
        
        // Access Token 생성
        String accessToken = Jwts.builder()
                .subject(authentication.getName())
                .claim("auth", auth)
                .claim("userName", userName)
                .claim("companyName", companyName)
                .claim("loginType", loginType)
                .expiration(new Date(System.currentTimeMillis() + 3 * 3600 * 1000))
                .signWith(key)
                .header().add("typ", "JWT")
                .and().compact();
        
        // Refresh Token 생성
        String refreshToken = generateRefreshToken(autoLogin, loginType);
        
        // Access Token과 Refresh Token을 발급
        return TokenInfo.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
    
    // refresh 토큰 생성
    public String generateRefreshToken(boolean autoLogin, String loginType) {
    	
    	// 자동 로그인이면 유효 기간 30일, 아니면 1일 
        long expirationTime = autoLogin ? 30L * 24 * 3600 * 1000 : 24L * 3600 * 1000;
        
        // Refresh Token을 생성하고 서명
        String refreshToken = Jwts.builder()
                .claim("autoLogin", autoLogin)
                .claim("loginType", loginType)
        		.id(UUID.randomUUID().toString()) // 고유 ID 설정
        		.issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTime)) 
                .signWith(key)
                .compact();
        
        return refreshToken;
    }
     
    // JWT 토큰에서 아이디, 닉네임, 회사명, 권한을 꺼내는 메서드
    public Authentication getAuthentication(String accessToken) {
        
    	if(validateToken(accessToken) == TokenStatus.VALID) {

    		// 토큰 복호화
            Claims claims = getClaims(accessToken);
     
            if (claims.get("auth") == null) {
                throw new RuntimeException("권한 정보가 없는 토큰입니다.");
            }
     
            Object raw = claims.get("auth");
            List<String> roles = new ObjectMapper().convertValue(raw, new TypeReference<List<String>>() {});
            
            Collection<? extends GrantedAuthority> authorities =
            	roles.stream().map(SimpleGrantedAuthority::new).toList();
     
            MemberDto member = new MemberDto();
            member.setUser_id(claims.getSubject()); // sub
            member.setUser_name(claims.get("userName", String.class));
            member.setRoles(roles);
            member.setCompany_name(claims.get("companyName", String.class));
            
            return new UsernamePasswordAuthenticationToken(member, "", authorities);
    	}
		return null;
    }
 
    // JWT 토큰의 유효성을 검증하고, 토큰 상태를 반환하는 메서드
	// 반환값: TokenStatus (EMPTY, VALID, EXPIRED, INVALID)
    public TokenStatus validateToken(String token) {
        try {
        	if (token == null || token.trim().isEmpty()) {
                return TokenStatus.EMPTY; // 빈 토큰
            }
        	
            Jwts.parser()
            .verifyWith((javax.crypto.SecretKey) key)
            .build()
            .parseSignedClaims(token);
            
            return TokenStatus.VALID; // 유효한 토큰
        } catch (ExpiredJwtException e) {
            //log.info("Expired JWT Token Claims: {}" + e.getClaims(), e);
            return TokenStatus.EXPIRED; // 만료된 토큰
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            //log.info("Invalid JWT Token", e);
            return TokenStatus.INVALID; // 변조된 토큰
        } catch (UnsupportedJwtException e) {
            //log.info("Unsupported JWT Token", e);
            return TokenStatus.INVALID; // 변조된 토큰
        } catch (IllegalArgumentException e) {
            //log.info("Invalid JWT format or corrupted token.", e);
            return TokenStatus.INVALID; // 잘못된 형식의 토큰
        }
    }
    
    // 토큰 정보를 검증하고 사용자 정보 return
    public Claims getClaims(String token) {
    	
        try {
        	Claims claims = Jwts.parser()
				            	.verifyWith((javax.crypto.SecretKey) key)
				            	.build()
				            	.parseSignedClaims(token).getPayload();
        	
            return claims;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.error("Invalid JWT Token", e);
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT Token", e);
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT Token", e);
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty.", e);
        } catch (Exception e) {
            log.error("An unexpected error occurred.", e);
        }
		return null;
		
    }

	/** 사용자 인증 상태 확인을 위한 JWT 토큰 생성  (Account 정보 조회 시 인증 여부 확인 용도)  
	 * 
	 * @param userId 사용자 ID
     * @param purpose (예: "isAuthVerified")
     * @return 생성된 JWT 토큰
	 */
	public String generateAuthFlagToken(String userId, String purpose) {
		
		Claims claims = Jwts.claims()
				.subject(userId)
				.add("purpose", purpose)
				.build();

        Date now = new Date();
        Date validity = new Date(now.getTime() + 30 * 60 * 1000);

        return Jwts.builder()
                .claims(claims) 
                .issuedAt(now) 
                .expiration(validity) 
                .signWith(key) 
                .compact();         
	}
	
	/**
	 * JWT 토큰에서 사용자 ID(Subject)를 추출합니다.
	 *
	 * @param token JWT 액세스 토큰 (Bearer prefix 제외)
	 * @return 사용자 ID (토큰의 Subject 클레임 값)
	 */
    public String extractUserId(String token) {
        
        return Jwts.parser()
                .verifyWith((SecretKey) key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
   
    /**
     * JWT 토큰에서 로그인 방식(loginType)을 추출합니다.
     *
     * @param token JWT 토큰
     * @return 로그인 타입 (예: "local", "naver", "kakao" )
     */
   public String extractLoginType(String token) {
	    return (String) getClaims(token).get("loginType");
	}
   
   /**
    * Refresh Token에서 자동 로그인 여부(autoLogin)를 추출합니다.
    *
    * @param refreshToken Refresh 토큰
    * @return 자동 로그인 여부 (true/false)
    */
	public boolean getAutoLoginFromRefreshToken(String refreshToken) {
		return (boolean) getClaims(refreshToken).get("autoLogin");
	}

	/**
	 * JWT 토큰에서 사용자 역할(Role)을 추출합니다.
	 *
	 * @param token JWT 문자열 
	 * @return 사용자의 역할 리스트 (예: "ROLE_USER", "ROLE_ADMIN")
	 * */
	public List<String> extractUserRole(String token) {

        @SuppressWarnings("unchecked")
		List<String> authList = getClaims(token).get("auth", List.class);
        
		return authList;
	}

	public boolean extractAutoLogin(String token) {
		return (boolean) getClaims(token).get("autoLogin");
	}
}