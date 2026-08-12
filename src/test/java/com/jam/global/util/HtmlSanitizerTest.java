package com.jam.global.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class HtmlSanitizerTest {

	@Nested
	@DisplayName("hasHtmlTag")
	class HasHtmlTag {

		@Test
		@DisplayName("순수 텍스트면 false")
		void plainText_false() {
			assertThat(HtmlSanitizer.hasHtmlTag("안녕하세요, 반갑습니다")).isFalse();
		}

		@Test
		@DisplayName("script 태그가 있으면 true")
		void scriptTag_true() {
			assertThat(HtmlSanitizer.hasHtmlTag("<script>alert(1)</script>")).isTrue();
		}

		@Test
		@DisplayName("일반 태그가 있으면 true")
		void basicTag_true() {
			assertThat(HtmlSanitizer.hasHtmlTag("<b>굵게</b>")).isTrue();
		}

		@Test
		@DisplayName("숫자/영문/특수문자 조합에는 오탐하지 않는다")
		void mixedContentWithoutTags_false() {
			assertThat(HtmlSanitizer.hasHtmlTag("가격: 10,000원 (택배비 별도) #급처")).isFalse();
		}
	}

	@Nested
	@DisplayName("sanitizeTitle")
	class SanitizeTitle {

		@Test
		@DisplayName("null이면 빈 문자열을 반환한다")
		void nullInput_emptyString() {
			assertThat(HtmlSanitizer.sanitizeTitle(null)).isEqualTo("");
		}

		@Test
		@DisplayName("순수 텍스트는 그대로 반환한다")
		void plainText_unchanged() {
			assertThat(HtmlSanitizer.sanitizeTitle("정상적인 제목입니다")).isEqualTo("정상적인 제목입니다");
		}

		@Test
		@DisplayName("HTML 태그는 제거하고 텍스트만 남긴다")
		void stripsTagsKeepsText() {
			assertThat(HtmlSanitizer.sanitizeTitle("<b>강조된</b> 제목")).isEqualTo("강조된 제목");
		}

		@Test
		@DisplayName("script 태그는 내용까지 제거한다")
		void stripsScriptTagAndContent() {
			assertThat(HtmlSanitizer.sanitizeTitle("제목<script>alert(1)</script>")).isEqualTo("제목");
		}
	}

	@Nested
	@DisplayName("sanitizeHtml")
	class SanitizeHtml {

		@Test
		@DisplayName("null이면 null을 반환한다")
		void nullInput_returnsNull() {
			assertThat(HtmlSanitizer.sanitizeHtml(null)).isNull();
		}

		@Test
		@DisplayName("허용된 태그(b, p 등)는 유지된다")
		void allowedTags_kept() {
			String result = HtmlSanitizer.sanitizeHtml("<p>문단 <b>강조</b></p>");

			assertThat(result).contains("<p>").contains("<b>").contains("강조");
		}

		@Test
		@DisplayName("script 태그는 제거된다")
		void scriptTag_removed() {
			String result = HtmlSanitizer.sanitizeHtml("<p>내용</p><script>alert(1)</script>");

			assertThat(result).doesNotContain("<script>").doesNotContain("alert(1)");
		}

		@Test
		@DisplayName("a 태그의 javascript: 프로토콜은 제거된다")
		void javascriptProtocol_stripped() {
			String result = HtmlSanitizer.sanitizeHtml("<a href=\"javascript:alert(1)\">링크</a>");

			assertThat(result).doesNotContain("javascript:");
		}

		@Test
		@DisplayName("a 태그에는 rel=nofollow가 강제로 붙는다")
		void linkTag_enforcesNofollow() {
			String result = HtmlSanitizer.sanitizeHtml("<a href=\"https://example.com\">링크</a>");

			assertThat(result).contains("rel=\"nofollow\"");
		}

		@Test
		@DisplayName("p 태그의 style은 color/background-color/font-size/text-align만 허용된다")
		void pStyle_filtersAllowedPropertiesOnly() {
			String result = HtmlSanitizer.sanitizeHtml("<p style=\"color:red; position:absolute; font-size:14px\">문단</p>");

			assertThat(result).contains("color:red").contains("font-size:14px").doesNotContain("position");
		}

		@Test
		@DisplayName("img 태그의 style은 width/height/max-width만 허용된다")
		void imgStyle_filtersAllowedPropertiesOnly() {
			String result = HtmlSanitizer.sanitizeHtml("<img src=\"https://example.com/a.png\" style=\"width:100px; color:red\">");

			assertThat(result).contains("width:100px").doesNotContain("color");
		}
	}
}
