package com.jam.global.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CookieEnum {
	ACCESS_TOKEN("Authorization", 3 * 60 * 60),          // 3시간
    REFRESH_TOKEN("RefreshToken", 24 * 60 * 60),        // 기본 24시간
    REFRESH_TOKEN_EXTENDED("RefreshToken", 30 * 24 * 60 * 60), // 자동 로그인 시 30일
	
	KAKAO_ACCESS_TOKEN("kakaoAccessToken", 3 * 60 * 60),
	NAVER_ACCESS_TOKEN("naverAccessToken", 3 * 60 * 60);

    private final String name;
    private final int expiry;

    /**
     * 자동 로그인 여부에 따라 적절한 RefreshToken을 반환하는 편의 메서드
     */
    public static CookieEnum getRefreshToken(boolean isAutoLogin) {
        return isAutoLogin ? REFRESH_TOKEN_EXTENDED : REFRESH_TOKEN;
    }
}
