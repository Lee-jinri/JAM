package com.jam.global.util;

import java.util.regex.Pattern;

import com.jam.member.dto.MemberDto;
import com.jam.global.exception.BadRequestException;
import com.jam.global.exception.UnauthorizedException;

public class ValidationUtils {
	private ValidationUtils() { };
	
	// USER_ID_PATTERN, PASSWORD_PATTERN
	// - 영문자와 숫자를 반드시 모두 포함해야 함 (하나 이상씩)
	// - 허용 문자: 영문(a-z, A-Z), 숫자(0-9)
	// - 총 길이: 8~20자
	private static final Pattern USER_ID_PATTERN = Pattern.compile("^(?=.*[a-zA-Z])(?=.*\\d)[a-zA-Z\\d]{8,20}$");
	private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-zA-Z])(?=.*\\d)[a-zA-Z\\d]{8,20}$");
	
	// NICKNAME_PATTERN
	// - 허용 문자: 한글 (가~힣), 영문 대/소문자, 숫자 0~9, _
	// - 길이: 3~10자
	private static final Pattern NICKNAME_PATTERN = Pattern.compile("^[가-힣a-zA-Z0-9_]{3,10}$");
	
	// PHONE_PATTERN
	// - 반드시 '01'로 시작하고, 두 번째 숫자는 0/1/6/7/8/9 중 하나
	// - 중간 자리(3~4자리)와 끝자리(4자리)
	// - 하이픈 없어야함
	private static final Pattern PHONE_PATTERN = Pattern.compile("^01([016789])([0-9]{3,4})([0-9]{4})$");
	
	// EMAIL_PATTERN
	// - 이메일 기본 형식 검증
	//   [계정명]@[도메인].[최상위도메인]
	// - 계정명/도메인: 영문, 숫자, '.', '-', '_' 가능
	// - 최상위 도메인(TLD)은 2~63자까지 허용
	private static final Pattern EMAIL_PATTERN = Pattern.compile("^[0-9a-zA-Z](?:[-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z](?:[-_.]?[0-9a-zA-Z])*\\.[a-zA-Z]{2,63}$");

	/**
	 * 회원가입 전체 입력 검증.
	 * 모두 통과하면 true 반환.
	 */
	public static boolean validateUserInfo(MemberDto member) {
		if (member == null) {
			throw new BadRequestException("요청 본문이 비어 있습니다.");
		}
		
		final String id = member.getUser_id();
		final String password = member.getPassword();
		final String nickname = member.getUser_name();
		final String phone = member.getPhone();
		final String email = member.getEmail();
		
		if(!ValidationUtils.validateUserId(id)) throw new BadRequestException("잘못된 형식의 아이디입니다.");
		if(!ValidationUtils.validateNickname(nickname)) throw new BadRequestException("잘못된 형식의 닉네임입니다.");
		if(!ValidationUtils.validatePassword(password)) throw new BadRequestException("잘못된 형식의 비밀번호입니다.");
		if(!ValidationUtils.validatePhone(phone)) throw new BadRequestException("잘못된 형식의 전화번호입니다.");
		if(!ValidationUtils.validateEmail(email)) throw new BadRequestException("잘못된 형식의 이메일입니다.");
		
		if (HtmlSanitizer.hasHtmlTag(id) ||
			HtmlSanitizer.hasHtmlTag(password) ||
			HtmlSanitizer.hasHtmlTag(nickname) ||
			HtmlSanitizer.hasHtmlTag(phone) ||
			HtmlSanitizer.hasHtmlTag(email)) {
			throw new BadRequestException("HTML 태그는 허용되지 않습니다.");
		}
		
		return true;
	}
	
	public static boolean validateUserId(String userId) {
		if (userId == null) throw new BadRequestException("아이디를 입력하세요.");
		return USER_ID_PATTERN.matcher(userId).matches();
	}
	public static boolean validatePassword(String password) {
		if (password == null) throw new BadRequestException("비밀번호를 입력하세요.");
		return PASSWORD_PATTERN.matcher(password).matches();
	}
	public static boolean validateNickname(String nickname) {
		if (nickname == null) throw new BadRequestException("닉네임을 입력하세요.");
		return NICKNAME_PATTERN.matcher(nickname).matches();
	}
	public static boolean validatePhone(String phone) {
		if (phone == null) throw new BadRequestException("전화번호를 입력하세요.");
		return PHONE_PATTERN.matcher(phone).matches();
	}
	public static boolean validateEmail(String email) {
		if (email == null) throw new BadRequestException("이메일을 입력하세요.");
		return EMAIL_PATTERN.matcher(email).matches();
	}
	
	public static String requireLogin(String userId) {
	    if (userId == null || userId.isBlank()) {
	        throw new UnauthorizedException("로그인이 필요한 서비스입니다.");
	    }
	    return userId;
	}

	public static Long requireValidId(Long id) {
	    if (id == null || id <= 0) {
	        throw new BadRequestException("잘못된 요청입니다.");
	    }
	    return id;
	}
}
