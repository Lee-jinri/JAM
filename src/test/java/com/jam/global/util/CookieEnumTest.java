package com.jam.global.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CookieEnumTest {

	@Test
	@DisplayName("autoLogin이 true면 만료 기간이 긴 REFRESH_TOKEN_EXTENDED를 반환한다")
	void getRefreshToken_autoLogin_returnsExtended() {
		assertThat(CookieEnum.getRefreshToken(true)).isEqualTo(CookieEnum.REFRESH_TOKEN_EXTENDED);
	}

	@Test
	@DisplayName("autoLogin이 false면 기본 REFRESH_TOKEN을 반환한다")
	void getRefreshToken_notAutoLogin_returnsDefault() {
		assertThat(CookieEnum.getRefreshToken(false)).isEqualTo(CookieEnum.REFRESH_TOKEN);
	}

	@Test
	@DisplayName("REFRESH_TOKEN_EXTENDED의 만료 기간이 REFRESH_TOKEN보다 길다")
	void extendedRefreshToken_hasLongerExpiry() {
		assertThat(CookieEnum.REFRESH_TOKEN_EXTENDED.getExpiry()).isGreaterThan(CookieEnum.REFRESH_TOKEN.getExpiry());
	}

	@Test
	@DisplayName("ACCESS_TOKEN과 REFRESH_TOKEN 계열은 같은 쿠키 이름을 쓰는 것을 제외하면 서로 다른 이름을 쓴다")
	void cookieNames_asExpected() {
		assertThat(CookieEnum.ACCESS_TOKEN.getName()).isEqualTo("Authorization");
		assertThat(CookieEnum.REFRESH_TOKEN.getName()).isEqualTo("RefreshToken");
		assertThat(CookieEnum.REFRESH_TOKEN_EXTENDED.getName()).isEqualTo("RefreshToken");
	}
}
