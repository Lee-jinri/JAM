package com.jam.security;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

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
	/*Jwt 토큰 발급, 인증, 갱신하는 클래스*/
	
	// Key 타입은 암호화, 서명, 해시 등과 같은 다양한 보안 작업에서 사용되는 키를 표현
	private final Key key;

    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey) {
        
    	byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }
 
    // 유저 정보로 AccessToken, RefreshToken 을 생성하는 메서드
    public TokenInfo generateToken(Authentication authentication, String user_name) {
        // 유저의 권한 가져오기
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        
        // Access Token 생성
        String accessToken = Jwts.builder()
                .setSubject(authentication.getName())
                .claim("auth", authorities)
                // 만료시간 : 현재부터 하루 뒤
                .setExpiration(new Date(System.currentTimeMillis() + 24 * 3600 * 1000)) // 유효 기간 1일
                .signWith(key, SignatureAlgorithm.HS256)
                .setHeaderParam("typ","JWT")
                .claim("username", user_name)
                .compact();
 
        // Refresh Token 생성
        String refreshToken = generateRefreshToken();
        
        // Access Token과 Refresh Token을 발급
        return TokenInfo.builder()
                .grantType("Bearer ")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
    
    
    // refresh 토큰 생성
    public static String generateRefreshToken() {
        // 무작위로 생성된 256비트 (32바이트) 키
        Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

        // Refresh Token을 생성하고 서명
        String refreshToken = Jwts.builder()
                .setExpiration(new Date(System.currentTimeMillis() + 14 * 24 * 3600 * 1000)) // 유효 기간 14일 
                .signWith(key)
                .compact();

        return refreshToken;
    }
 
    // JWT 토큰에서 권한을 꺼내는 메서드
    public Authentication getAuthentication(String accessToken) {
        
    	if(validateToken(accessToken)) {
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
            
        	log.info("jwtprovider getAuthentication : " + principal);
        	
            return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    	}
		return null;
    }
    
    // 아이디와 닉네임을 꺼내는 메서드
    public Map<String, String> getUserInfo(String accessToken) {
    	
    	if(validateToken(accessToken)) {
    		Map<String, String> user = new HashMap<>();
	    	// 토큰 복호화
	        Claims claims = getClaims(accessToken);
	        
	        String user_name = claims.get("username", String.class);
	    	String user_id = claims.getSubject();
	    	
	    	user.put("user_id", user_id);
	    	user.put("user_name", user_name);
	    	
	    	return user;
    	}else return null;
    }
    
    // 토큰 정보를 검증하는 메서드
    public boolean validateToken(String token) {
        try {
        	Jwts.parserBuilder()
        		.setSigningKey(key)
				.build()
				.parseClaimsJws(token);
            
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("Invalid JWT Token", e);
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT Token", e);
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT Token", e);
        } catch (IllegalArgumentException e) {
            log.info("JWT claims string is empty.", e);
        }
		return false;
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
            log.info("Invalid JWT Token", e);
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT Token", e);
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT Token", e);
        } catch (IllegalArgumentException e) {
            log.info("JWT claims string is empty.", e);
        }
		return null;
    }
 
    
}