package com.jam.global.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.jam.global.exception.BadRequestException;
import com.jam.global.exception.UnauthorizedException;
import com.jam.member.dto.MemberDto;

class ValidationUtilsTest {

	@Nested
	@DisplayName("validateUserId")
	class ValidateUserId {

		@Test
		@DisplayName("null이면 예외를 던진다")
		void nullId_throws() {
			assertThatThrownBy(() -> ValidationUtils.validateUserId(null))
					.isInstanceOf(BadRequestException.class);
		}

		@Test
		@DisplayName("영문+숫자 8~20자면 통과")
		void validFormat_true() {
			assertThat(ValidationUtils.validateUserId("abcd1234")).isTrue();
		}

		@Test
		@DisplayName("7자 이하면 실패")
		void tooShort_false() {
			assertThat(ValidationUtils.validateUserId("abc123")).isFalse();
		}

		@Test
		@DisplayName("21자 이상이면 실패")
		void tooLong_false() {
			assertThat(ValidationUtils.validateUserId("abcdefghijklmnop12345")).isFalse();
		}

		@Test
		@DisplayName("영문만 있고 숫자가 없으면 실패")
		void noDigit_false() {
			assertThat(ValidationUtils.validateUserId("abcdefgh")).isFalse();
		}

		@Test
		@DisplayName("숫자만 있고 영문이 없으면 실패")
		void noLetter_false() {
			assertThat(ValidationUtils.validateUserId("12345678")).isFalse();
		}

		@Test
		@DisplayName("특수문자가 포함되면 실패")
		void specialChar_false() {
			assertThat(ValidationUtils.validateUserId("abcd123!")).isFalse();
		}
	}

	@Nested
	@DisplayName("validatePassword")
	class ValidatePassword {

		@Test
		@DisplayName("null이면 예외를 던진다")
		void nullPassword_throws() {
			assertThatThrownBy(() -> ValidationUtils.validatePassword(null))
					.isInstanceOf(BadRequestException.class);
		}

		@Test
		@DisplayName("영문+숫자 8~20자면 통과")
		void validFormat_true() {
			assertThat(ValidationUtils.validatePassword("pass1234")).isTrue();
		}

		@Test
		@DisplayName("영문만 있으면 실패")
		void noDigit_false() {
			assertThat(ValidationUtils.validatePassword("passwords")).isFalse();
		}
	}

	@Nested
	@DisplayName("validateNickname")
	class ValidateNickname {

		@Test
		@DisplayName("null이면 예외를 던진다")
		void nullNickname_throws() {
			assertThatThrownBy(() -> ValidationUtils.validateNickname(null))
					.isInstanceOf(BadRequestException.class);
		}

		@Test
		@DisplayName("한글 3~10자면 통과")
		void koreanNickname_true() {
			assertThat(ValidationUtils.validateNickname("기타리스트")).isTrue();
		}

		@Test
		@DisplayName("영문+숫자+언더스코어 조합이면 통과")
		void alphanumericUnderscore_true() {
			assertThat(ValidationUtils.validateNickname("user_123")).isTrue();
		}

		@Test
		@DisplayName("2자 이하면 실패")
		void tooShort_false() {
			assertThat(ValidationUtils.validateNickname("ab")).isFalse();
		}

		@Test
		@DisplayName("11자 이상이면 실패")
		void tooLong_false() {
			assertThat(ValidationUtils.validateNickname("012345678901")).isFalse();
		}

		@Test
		@DisplayName("공백이 포함되면 실패")
		void whitespace_false() {
			assertThat(ValidationUtils.validateNickname("my name")).isFalse();
		}
	}

	@Nested
	@DisplayName("validatePhone")
	class ValidatePhone {

		@Test
		@DisplayName("null이면 예외를 던진다")
		void nullPhone_throws() {
			assertThatThrownBy(() -> ValidationUtils.validatePhone(null))
					.isInstanceOf(BadRequestException.class);
		}

		@Test
		@DisplayName("올바른 휴대폰 번호면 통과")
		void validFormat_true() {
			assertThat(ValidationUtils.validatePhone("01012345678")).isTrue();
		}

		@Test
		@DisplayName("010이 아닌 011 등도 두 번째 자리 허용 범위면 통과")
		void otherSecondDigit_true() {
			assertThat(ValidationUtils.validatePhone("01712345678")).isTrue();
		}

		@Test
		@DisplayName("하이픈이 포함되면 실패")
		void withHyphen_false() {
			assertThat(ValidationUtils.validatePhone("010-1234-5678")).isFalse();
		}

		@Test
		@DisplayName("02로 시작하는 지역번호면 실패")
		void landline_false() {
			assertThat(ValidationUtils.validatePhone("0212345678")).isFalse();
		}

		@Test
		@DisplayName("두 번째 자리가 허용되지 않는 숫자(2,3,4,5)면 실패")
		void invalidSecondDigit_false() {
			assertThat(ValidationUtils.validatePhone("01212345678")).isFalse();
		}
	}

	@Nested
	@DisplayName("validateEmail")
	class ValidateEmail {

		@Test
		@DisplayName("null이면 예외를 던진다")
		void nullEmail_throws() {
			assertThatThrownBy(() -> ValidationUtils.validateEmail(null))
					.isInstanceOf(BadRequestException.class);
		}

		@Test
		@DisplayName("올바른 이메일이면 통과")
		void validFormat_true() {
			assertThat(ValidationUtils.validateEmail("test@example.com")).isTrue();
		}

		@Test
		@DisplayName("@가 없으면 실패")
		void noAtSign_false() {
			assertThat(ValidationUtils.validateEmail("testexample.com")).isFalse();
		}

		@Test
		@DisplayName("도메인에 점이 없으면 실패")
		void noDomainDot_false() {
			assertThat(ValidationUtils.validateEmail("test@examplecom")).isFalse();
		}
	}

	@Nested
	@DisplayName("validateUserInfo")
	class ValidateUserInfo {

		private MemberDto validMember() {
			MemberDto member = new MemberDto();
			member.setUser_id("testuser1");
			member.setUser_pw("pass1234");
			member.setUser_name("닉네임");
			member.setPhone("01012345678");
			member.setEmail("test@example.com");
			return member;
		}

		@Test
		@DisplayName("member가 null이면 예외를 던진다")
		void nullMember_throws() {
			assertThatThrownBy(() -> ValidationUtils.validateUserInfo(null))
					.isInstanceOf(BadRequestException.class);
		}

		@Test
		@DisplayName("모든 필드가 올바르면 true를 반환한다")
		void allValid_true() {
			assertThat(ValidationUtils.validateUserInfo(validMember())).isTrue();
		}

		@Test
		@DisplayName("아이디 형식이 잘못되면 예외를 던진다")
		void invalidUserId_throws() {
			MemberDto member = validMember();
			member.setUser_id("ab");

			assertThatThrownBy(() -> ValidationUtils.validateUserInfo(member))
					.isInstanceOf(BadRequestException.class)
					.hasMessage("잘못된 형식의 아이디입니다.");
		}

		@Test
		@DisplayName("HTML 태그가 포함된 값은 각 필드의 형식 정규식 자체에 걸려 형식 오류로 거부된다"
				+ " (모든 필드 패턴이 <, >를 허용하지 않아 HTML 태그 검사 블록에는 도달하지 않음)")
		void htmlTagInField_rejectedByFormatCheckFirst() {
			MemberDto member = validMember();
			member.setUser_name("<b>닉네임</b>");

			assertThatThrownBy(() -> ValidationUtils.validateUserInfo(member))
					.isInstanceOf(BadRequestException.class)
					.hasMessage("잘못된 형식의 닉네임입니다.");
		}
	}

	@Nested
	@DisplayName("requireLogin")
	class RequireLogin {

		@Test
		@DisplayName("null이면 UnauthorizedException을 던진다")
		void nullUserId_throws() {
			assertThatThrownBy(() -> ValidationUtils.requireLogin(null))
					.isInstanceOf(UnauthorizedException.class);
		}

		@Test
		@DisplayName("빈 문자열이면 UnauthorizedException을 던진다")
		void blankUserId_throws() {
			assertThatThrownBy(() -> ValidationUtils.requireLogin("  "))
					.isInstanceOf(UnauthorizedException.class);
		}

		@Test
		@DisplayName("값이 있으면 그대로 반환한다")
		void validUserId_returnsAsIs() {
			assertThat(ValidationUtils.requireLogin("user1")).isEqualTo("user1");
		}
	}

	@Nested
	@DisplayName("requireValidId")
	class RequireValidId {

		@Test
		@DisplayName("null이면 BadRequestException을 던진다")
		void nullId_throws() {
			assertThatThrownBy(() -> ValidationUtils.requireValidId(null))
					.isInstanceOf(BadRequestException.class);
		}

		@Test
		@DisplayName("0 이하이면 BadRequestException을 던진다")
		void nonPositiveId_throws() {
			assertThatThrownBy(() -> ValidationUtils.requireValidId(0L))
					.isInstanceOf(BadRequestException.class);
			assertThatThrownBy(() -> ValidationUtils.requireValidId(-1L))
					.isInstanceOf(BadRequestException.class);
		}

		@Test
		@DisplayName("양수면 그대로 반환한다")
		void positiveId_returnsAsIs() {
			assertThat(ValidationUtils.requireValidId(5L)).isEqualTo(5L);
		}
	}
}
