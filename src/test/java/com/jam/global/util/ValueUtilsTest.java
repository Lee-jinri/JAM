package com.jam.global.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ValueUtilsTest {

	@Nested
	@DisplayName("emptyToNull")
	class EmptyToNull {

		@Test
		@DisplayName("null이면 null을 반환한다")
		void nullValue_returnsNull() {
			assertThat(ValueUtils.emptyToNull(null)).isNull();
		}

		@Test
		@DisplayName("빈 문자열/공백만 있으면 null을 반환한다")
		void blankValue_returnsNull() {
			assertThat(ValueUtils.emptyToNull("")).isNull();
			assertThat(ValueUtils.emptyToNull("   ")).isNull();
		}

		@Test
		@DisplayName("값이 있으면 트림하지 않고 원본 그대로 반환한다")
		void nonBlankValue_returnsAsIs() {
			assertThat(ValueUtils.emptyToNull(" abc ")).isEqualTo(" abc ");
		}
	}

	@Nested
	@DisplayName("guNullToAll")
	class GuNullToAll {

		@Test
		@DisplayName("null이면 전체를 반환한다")
		void nullValue_returnsAll() {
			assertThat(ValueUtils.guNullToAll(null)).isEqualTo("전체");
		}

		@Test
		@DisplayName("값이 있으면 그대로 반환한다")
		void nonNullValue_returnsAsIs() {
			assertThat(ValueUtils.guNullToAll("서울")).isEqualTo("서울");
		}
	}

	@Nested
	@DisplayName("sanitizeForLike")
	class SanitizeForLike {

		@Test
		@DisplayName("null이면 null을 반환한다")
		void nullValue_returnsNull() {
			assertThat(ValueUtils.sanitizeForLike(null)).isNull();
		}

		@Test
		@DisplayName("공백만 있으면 null을 반환한다")
		void blankValue_returnsNull() {
			assertThat(ValueUtils.sanitizeForLike("   ")).isNull();
		}

		@Test
		@DisplayName("앞뒤 공백을 트림한다")
		void trimsSurroundingWhitespace() {
			assertThat(ValueUtils.sanitizeForLike("  기타  ")).isEqualTo("기타");
		}

		@Test
		@DisplayName("50자를 초과하면 잘라낸다")
		void truncatesTo50Chars() {
			String longKeyword = "가".repeat(60);

			String result = ValueUtils.sanitizeForLike(longKeyword);

			assertThat(result).hasSize(50);
		}

		@Test
		@DisplayName("제어문자는 공백으로 치환되고 연속 공백은 하나로 합쳐진다")
		void controlCharsAndRepeatedSpacesCollapsed() {
			String result = ValueUtils.sanitizeForLike("a\t\tb\nc");

			assertThat(result).isEqualTo("a b c");
		}

		@Test
		@DisplayName("유니코드를 NFKC 형태로 정규화한다 (전각 문자 -> 반각)")
		void normalizesToNfkc() {
			String result = ValueUtils.sanitizeForLike("ＡＢ"); // 전각 A, B

			assertThat(result).isEqualTo("AB");
		}

		@Test
		@DisplayName("LIKE 와일드카드(%, _)와 ESCAPE 문자(\\)를 이스케이프한다")
		void escapesLikeWildcardsAndBackslash() {
			String result = ValueUtils.sanitizeForLike("100%_off\\test");

			assertThat(result).isEqualTo("100\\%\\_off\\\\test");
		}
	}
}
