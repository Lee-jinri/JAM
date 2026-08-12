package com.jam.global.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class SecurityUtilTest {

	@Nested
	@DisplayName("hashToken")
	class HashToken {

		@Test
		@DisplayName("같은 입력이면 항상 같은 해시를 반환한다")
		void sameInput_sameHash() {
			String hash1 = SecurityUtil.hashToken("my-refresh-token");
			String hash2 = SecurityUtil.hashToken("my-refresh-token");

			assertThat(hash1).isEqualTo(hash2);
		}

		@Test
		@DisplayName("다른 입력이면 다른 해시를 반환한다")
		void differentInput_differentHash() {
			String hash1 = SecurityUtil.hashToken("token-a");
			String hash2 = SecurityUtil.hashToken("token-b");

			assertThat(hash1).isNotEqualTo(hash2);
		}

		@Test
		@DisplayName("원본 토큰 문자열을 그대로 반환하지 않는다")
		void doesNotReturnRawInput() {
			String raw = "my-refresh-token";

			assertThat(SecurityUtil.hashToken(raw)).isNotEqualTo(raw);
		}

		@Test
		@DisplayName("null이면 null을 반환한다")
		void nullInput_returnsNull() {
			assertThat(SecurityUtil.hashToken(null)).isNull();
		}
	}

	@Nested
	@DisplayName("matches")
	class Matches {

		@Test
		@DisplayName("원본과 그 해시값을 비교하면 true를 반환한다")
		void rawAndItsHash_true() {
			String raw = "my-refresh-token";
			String hashed = SecurityUtil.hashToken(raw);

			assertThat(SecurityUtil.matches(raw, hashed)).isTrue();
		}

		@Test
		@DisplayName("다른 원본이면 false를 반환한다")
		void differentRaw_false() {
			String hashed = SecurityUtil.hashToken("original-token");

			assertThat(SecurityUtil.matches("different-token", hashed)).isFalse();
		}
	}
}
