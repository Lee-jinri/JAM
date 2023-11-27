package com.jam.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class TokenInfo {
	private String accessToken;
	private String refreshToken;
	private String grantType; // JWT 인증 타입 - Bearer 
}
