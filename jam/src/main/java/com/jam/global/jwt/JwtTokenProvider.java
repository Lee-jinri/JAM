package com.jam.global.jwt;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.servlet.http.Cookie;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.jam.global.jwt.TokenInfo.TokenStatus;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.log4j.Log4j;

@Component
@Log4j
@PropertySource("classpath:application.properties")
public class JwtTokenProvider {
	/*Jwt 토큰 발급, 인증, 갱신하는 클래스
	 * 
	 * */
	// FIXME: 순수 JWT 처리 전용으로 책임 분리할 것
	
	// Key 타입은 암호화, 서명, 해시 등과 같은 다양한 보안 작업에서 사용되는 키를 표현
	private final Key key;
	public static final String BEARER_PREFIX = "Bearer "; 

    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey) {
        
    	byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }
 
    // 회원 정보로 AccessToken, RefreshToken 을 생성하는 메서드
    public TokenInfo generateToken(Authentication authentication, boolean autoLogin, String loginType) {
    	
        // 회원의 권한 가져오기
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        
        // Access Token 생성
        String accessToken = Jwts.builder()
                .setSubject(authentication.getName())
                .claim("auth", authorities)
                .claim("loginType", loginType)
                //.setExpiration(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3 * 3600 * 1000)) // 유효 기간 3시간
                .signWith(key, SignatureAlgorithm.HS256)
                .setHeaderParam("typ","JWT")
                .compact();
        
        // Refresh Token 생성
        String refreshToken = generateRefreshToken(autoLogin, loginType);
        
        // Access Token과 Refresh Token을 발급
        return TokenInfo.builder()
                .grantType("Bearer ")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
    
    // refresh 토큰 생성
    public String generateRefreshToken(boolean autoLogin, String loginType) {
    	
    	// 자동 로그인이면 유효 기간 30일, 아니면 1일 
        int expirationTime = autoLogin ? 30 * 24 * 3600 * 1000 : 24 * 3600 * 1000;
        
        // Refresh Token을 생성하고 서명
        String refreshToken = Jwts.builder()
                .claim("autoLogin", autoLogin)
                .claim("loginType", loginType)
        		.setId(UUID.randomUUID().toString()) // 고유 ID 설정
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime)) 
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        return refreshToken;
    }
     
    // JWT 토큰에서 아이디, 권한을 꺼내는 메서드
    public Authentication getAuthentication(String accessToken) {
        
    	if(validateToken(accessToken) == TokenStatus.VALID) {
    		// 토큰 복호화
            Claims claims = getClaims(accessToken);
     
            if (claims.get("auth") == null) {
                throw new RuntimeException("권한 정보가 없는 토큰입니다.");
            }
     
            // 클레임에서 권한 정보 가져오기
            Collection<? extends GrantedAuthority> authorities =
                    Arrays.stream(claims.get("auth").toString().split(","))
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());
     
            // UserDetails 객체를 만들어서 Authentication 리턴
            UserDetails principal = new User(claims.getSubject(), "", authorities);
            
            return new UsernamePasswordAuthenticationToken(principal, "", authorities);
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
        	
            Jwts.parserBuilder()
                .setSigningKey(key) // 서버의 비밀키로 서명 검증
                .build()
                .parseClaimsJws(token);

            return TokenStatus.VALID; // 유효한 토큰
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT Token Claims: {}" + e.getClaims(), e);
            return TokenStatus.EXPIRED; // 만료된 토큰
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("Invalid JWT Token", e);
            return TokenStatus.INVALID; // 변조된 토큰
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT Token", e);
            return TokenStatus.INVALID; // 변조된 토큰
        } catch (IllegalArgumentException e) {
            log.info("Invalid JWT format or corrupted token.", e);
            return TokenStatus.INVALID; // 잘못된 형식의 토큰
        }
    }
    
    // 토큰 정보를 검증하고 사용자 정보 return
    public Claims getClaims(String token) {
    	
        try {
        	Claims claims = Jwts.parserBuilder()
				            	.setSigningKey(key)
				            	.build()
				            	.parseClaimsJws(token).getBody();
            
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
    
    
	public String getAccessTokenFromCookies(Cookie[] cookies) {
		
		if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("Authorization")) { 
                	return cookie.getValue();
                }
            }
        }
		
		return null;
	}
	
	public String getUserIdFormToken(String accessToken) {
	    try {
	        Authentication user = getAuthentication(accessToken);
	        if (user == null || "anonymousUser".equals(user.getPrincipal())) {
	            throw new IllegalStateException("Authentication failed or user is anonymous");
	        }
	        UserDetails userDetails = (UserDetails) user.getPrincipal();
	        return userDetails.getUsername();
	    } catch (Exception e) {
	        log.error("Failed to get ID from token: " + e.getMessage());
	        return null;
	    }
	}

	/** 사용자 인증 상태 확인을 위한 JWT 토큰 생성  (Account 정보 조회 시 인증 여부 확인 용도)  
	 * 
	 * @param userId 사용자 ID
     * @param purpose (예: "isAuthVerified")
     * @return 생성된 JWT 토큰
	 */
	public String generateAuthFlagToken(String userId, String purpose) {
		
		Claims claims = Jwts.claims().setSubject(userId);
        claims.put("purpose", purpose); 

        Date now = new Date();
        Date validity = new Date(now.getTime() + 30 * 60 * 1000);

        return Jwts.builder()
                .setClaims(claims) 
                .setIssuedAt(now) 
                .setExpiration(validity) 
                .signWith(key, SignatureAlgorithm.HS256) 
                .compact(); 
	}
	
	/**
     * JWT 토큰에서 사용자 ID 추출
     * @param token JWT 토큰
     * @return Subject (사용자 아이디) 반환
     */
    public String extractUserId(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key) 
                .build()
                .parseClaimsJws(token) 
                .getBody()
                .getSubject(); 
    }
 
	/**
    * JWT 토큰에서 purpose 추출
    * @param token JWT 토큰
    * @return purpose (예: "isAuthVerified")
    */
   public String extractPurpose(String token) {
       return (String) Jwts.parserBuilder()
               .setSigningKey(key)
               .build()
               .parseClaimsJws(token) 
               .getBody()
               .get("purpose");
   }
   
   /**
    * 토큰에서 loginType 추출
    * @param token
    * @return
    */
   public String extractLoginType(String token) {
	    return (String) getClaims(token).get("loginType");
	}
   
    
	/**
	 * Refresh token에서 자동 로그인 여부 추출
	 * @param refreshToken
	 * @param key
	 * @return autoLogin
	 */
	public boolean getAutoLoginFromRefreshToken(String refreshToken) {
		return (boolean) getClaims(refreshToken).get("autoLogin");
	}
}