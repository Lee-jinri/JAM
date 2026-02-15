package com.jam.global.jwt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class TokenInfo {
	private String accessToken;
	private String refreshToken;
	
	public enum TokenStatus {
        VALID,          // 정상 토큰
        EXPIRED,        // 만료된 토큰
        INVALID,         // 변조된 토큰
        EMPTY
    }
	
}
