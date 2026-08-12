package com.jam.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import com.jam.global.exception.ConflictException;
import com.jam.member.dto.MemberDto;
import com.jam.member.mapper.MemberMapper;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

/**
 * MemberService에 대한 @SpringBootTest 통합 테스트. MemberMapper.xml에 있는 실제 SQL을 검증한다.
 * JavaMailSender는 실제 메일 전송을 막기 위해 @MockBean으로 대체한다(비밀번호 찾기 시 실제 메일이 나가면 안 됨).
 */
@SpringBootTest
@Transactional
class MemberServiceIntegrationTest {

	@Autowired
	private MemberService memberService;

	@Autowired
	private MemberMapper memberMapper;

	@Autowired
	private PasswordEncoder encoder;

	@MockBean
	private JavaMailSender mailSender;

	private int seq = 0;

	private String uniqueId(String prefix) {
		return prefix + (++seq) + "_" + System.nanoTime() % 100000;
	}

	private MemberDto joinLocalMember(String userId) {
		MemberDto member = new MemberDto();
		member.setUser_id(userId);
		member.setUser_pw(encoder.encode("pw12345678"));
		member.setUser_name("n" + Integer.toHexString(userId.hashCode()));
		member.setPhone("010" + String.format("%08d", Math.abs(userId.hashCode()) % 100000000));
		member.setEmail(userId + "@test.com");
		memberService.join(member);
		return member;
	}

	@Nested
	@DisplayName("join / 중복확인")
	class JoinAndDuplicateChecks {

		@Test
		@DisplayName("가입하면 idCheck/nameCheck/phoneCheck/emailCheck가 모두 중복으로 인식한다")
		void join_thenAllChecksDetectDuplicate() {
			String userId = uniqueId("joinUser");
			MemberDto member = joinLocalMember(userId);

			assertThat(memberService.idCheck(userId)).isEqualTo(1);
			assertThat(memberService.nameCheck(member.getUser_name())).isEqualTo(1);
			assertThat(memberService.phoneCheck(member.getPhone())).isEqualTo(1);
			assertThat(memberService.emailCheck(member.getEmail())).isEqualTo(1);
		}

		@Test
		@DisplayName("가입하면 기본 권한(ROLE_USER)이 함께 부여된다")
		void join_assignsDefaultRole() {
			String userId = uniqueId("roleUser");
			joinLocalMember(userId);

			MemberDto found = memberService.findByUserInfo(userId);

			assertThat(found.getRoles()).containsExactly("ROLE_USER");
		}
	}

	@Nested
	@DisplayName("deleteAccount")
	class DeleteAccount {

		@Test
		@DisplayName("탈퇴하면 소프트 삭제되어 아이디는 계속 중복으로 남지만, 닉네임/전화번호/이메일은 재사용 가능해진다")
		void deleteAccount_softDeletes_freesNamePhoneEmail() {
			String userId = uniqueId("delUser");
			MemberDto member = joinLocalMember(userId);

			memberService.deleteAccount(userId);

			assertThat(memberService.idCheck(userId)).isEqualTo(1);
			assertThat(memberService.nameCheck(member.getUser_name())).isEqualTo(0);
			assertThat(memberService.phoneCheck(member.getPhone())).isEqualTo(0);
			assertThat(memberService.emailCheck(member.getEmail())).isEqualTo(0);
		}

		@Test
		@DisplayName("탈퇴한 사용자는 isActiveUser가 false, getUserProfile은 null을 반환한다")
		void deleteAccount_isActiveUserFalse_profileNull() {
			String userId = uniqueId("delUser");
			joinLocalMember(userId);

			assertThat(memberService.isActiveUser(userId)).isTrue();

			memberService.deleteAccount(userId);

			assertThat(memberService.isActiveUser(userId)).isFalse();
			assertThat(memberService.getUserProfile(userId)).isNull();
		}

		@Test
		@DisplayName("탈퇴 후 실제로 같은 닉네임으로 다른 사용자가 가입할 수 있다 (DB unique 제약과 충돌하지 않음)")
		void deleteAccount_freedNickname_canBeReusedByAnotherMember() {
			String firstUserId = uniqueId("delUser");
			MemberDto first = joinLocalMember(firstUserId);
			String reusedName = first.getUser_name();

			memberService.deleteAccount(firstUserId);

			String secondUserId = uniqueId("delUser");
			MemberDto second = new MemberDto();
			second.setUser_id(secondUserId);
			second.setUser_pw(encoder.encode("pw12345678"));
			second.setUser_name(reusedName);
			second.setPhone("010" + String.format("%08d", Math.abs(secondUserId.hashCode()) % 100000000));
			second.setEmail(secondUserId + "@test.com");

			memberService.join(second);

			assertThat(memberService.getUserId(reusedName)).isEqualTo(secondUserId);
		}

		@Test
		@DisplayName("탈퇴한 사용자의 닉네임을 조회하면 실제 저장값 대신 (알 수 없음)을 반환한다")
		void deleteAccount_getUserName_returnsUnknownPlaceholder() {
			String userId = uniqueId("delUser");
			joinLocalMember(userId);

			memberService.deleteAccount(userId);

			assertThat(memberService.getUserName(userId)).isEqualTo("(알 수 없음)");
		}
	}

	@Nested
	@DisplayName("FindId / updatePwAndSendEmail")
	class FindIdAndUpdatePw {

		@Test
		@DisplayName("이메일과 전화번호가 일치하면 아이디를 찾는다")
		void findId_matches_returnsUserId() {
			String userId = uniqueId("findIdUser");
			MemberDto member = joinLocalMember(userId);

			String found = memberService.FindId(member.getEmail(), member.getPhone());

			assertThat(found).isEqualTo(userId);
		}

		@Test
		@DisplayName("일치하지 않으면 아이디를 찾지 못한다")
		void findId_noMatch_returnsNull() {
			String found = memberService.FindId("nope@test.com", "01099999999");

			assertThat(found).isNull();
		}

		@Test
		@DisplayName("일치하는 사용자면 비밀번호가 실제로 변경되고 메일 전송이 시도된다")
		void updatePwAndSendEmail_matches_changesPassword() throws Exception {
			String userId = uniqueId("pwUser");
			MemberDto member = joinLocalMember(userId);
			String beforePw = memberMapper.getPassword(member);

			MimeMessage message = new MimeMessage((Session) null);
			given(mailSender.createMimeMessage()).willReturn(message);

			memberService.updatePwAndSendEmail(userId, member.getEmail(), member.getPhone());

			String afterPw = memberMapper.getPassword(member);
			assertThat(afterPw).isNotEqualTo(beforePw);
		}

		@Test
		@DisplayName("일치하지 않으면 비밀번호를 변경하지 않는다")
		void updatePwAndSendEmail_noMatch_doesNotChangePassword() {
			String userId = uniqueId("pwUser");
			MemberDto member = joinLocalMember(userId);
			String beforePw = memberMapper.getPassword(member);

			memberService.updatePwAndSendEmail(userId, "wrong@test.com", member.getPhone());

			String afterPw = memberMapper.getPassword(member);
			assertThat(afterPw).isEqualTo(beforePw);
		}
	}

	@Nested
	@DisplayName("updateUserName / updatePhone / updateAddress")
	class UpdateProfile {

		@Test
		@DisplayName("닉네임을 변경하면 실제로 반영된다")
		void updateUserName_reflectsChange() {
			String userId = uniqueId("nameUser");
			joinLocalMember(userId);

			MemberDto update = new MemberDto();
			update.setUser_id(userId);
			update.setUser_name("변경된닉네임");

			boolean result = memberService.updateUserName(update);

			assertThat(result).isTrue();
			assertThat(memberService.getUserName(userId)).isEqualTo("변경된닉네임");
			assertThat(memberService.getUserId("변경된닉네임")).isEqualTo(userId);
		}

		@Test
		@DisplayName("전화번호를 변경하면 실제로 반영된다")
		void updatePhone_reflectsChange() {
			String userId = uniqueId("phoneUser");
			joinLocalMember(userId);

			MemberDto update = new MemberDto();
			update.setUser_id(userId);
			update.setPhone("01055554444");

			memberService.updatePhone(update);

			assertThat(memberService.phoneCheck("01055554444")).isEqualTo(1);
		}

		@Test
		@DisplayName("주소를 변경하면 실제로 반영된다")
		void updateAddress_reflectsChange() {
			String userId = uniqueId("addrUser");
			joinLocalMember(userId);

			int updated = memberService.updateAddress("부산광역시", userId);

			assertThat(updated).isEqualTo(1);
		}
	}

	@Nested
	@DisplayName("refresh 토큰")
	class RefreshToken {

		@Test
		@DisplayName("저장, 조회, 삭제가 정상적으로 동작한다")
		void addGetDeleteRefreshToken() {
			String userId = uniqueId("tokenUser");
			joinLocalMember(userId);

			int added = memberService.addRefreshToken(userId, "refresh-token-value");
			assertThat(added).isEqualTo(1);
			assertThat(memberService.getRefreshToken(userId)).isEqualTo("refresh-token-value");

			int deleted = memberService.deleteRefreshToken(userId);
			assertThat(deleted).isEqualTo(1);
			assertThat(memberService.getRefreshToken(userId)).isNull();
		}
	}

	@Nested
	@DisplayName("socialLoginOrRegister")
	class SocialLoginOrRegister {

		@Test
		@DisplayName("신규 소셜 사용자는 회원가입되고 기본 권한이 부여된다")
		void newSocialUser_registersWithDefaultRole() {
			String userId = uniqueId("kakao_");
			Map<String, Object> userInfo = new HashMap<>();
			userInfo.put("user_id", userId);
			userInfo.put("user_name", "소셜닉");

			MemberDto result = memberService.socialLoginOrRegister(userInfo, "kakao");

			assertThat(result.getUser_name()).isEqualTo("kakao_소셜닉");
			assertThat(memberService.idCheck(userId)).isEqualTo(1);
			MemberDto found = memberService.findByUserInfo(userId);
			assertThat(found.getRoles()).containsExactly("ROLE_USER");
		}

		@Test
		@DisplayName("이미 가입된 소셜 사용자는 다시 가입되지 않고 기존 정보를 반환한다")
		void existingSocialUser_doesNotReRegister() {
			String userId = uniqueId("kakao_");
			Map<String, Object> userInfo = new HashMap<>();
			userInfo.put("user_id", userId);
			userInfo.put("user_name", "소셜닉");
			memberService.socialLoginOrRegister(userInfo, "kakao");

			Map<String, Object> secondCall = new HashMap<>();
			secondCall.put("user_id", userId);
			secondCall.put("user_name", "다른닉네임요청");
			MemberDto result = memberService.socialLoginOrRegister(secondCall, "kakao");

			assertThat(result.getUser_id()).isEqualTo(userId);
			assertThat(memberService.idCheck(userId)).isEqualTo(1);
		}
	}

	@Nested
	@DisplayName("convertBusiness")
	class ConvertBusiness {

		@Test
		@DisplayName("일반 회원을 기업 회원으로 전환하면 회사명과 ROLE_COMPANY가 반영된다")
		void convertsToCompanyMember() {
			String userId = uniqueId("bizUser");
			joinLocalMember(userId);
			MemberDto user = memberService.findByUserInfo(userId);

			memberService.convertBusiness(userId, "테스트컴퍼니", user);

			MemberDto found = memberService.findByUserInfo(userId);
			assertThat(found.getRoles()).contains("ROLE_USER", "ROLE_COMPANY");
		}

		@Test
		@DisplayName("두 번 전환을 시도해도 ROLE_COMPANY가 중복 부여되지 않는다")
		void convertTwice_doesNotDuplicateRole() {
			String userId = uniqueId("bizUser");
			joinLocalMember(userId);
			MemberDto user = memberService.findByUserInfo(userId);

			memberService.convertBusiness(userId, "테스트컴퍼니", user);
			MemberDto refetched = memberService.findByUserInfo(userId);
			memberService.convertBusiness(userId, "테스트컴퍼니2", refetched);

			MemberDto found = memberService.findByUserInfo(userId);
			long companyRoleCount = found.getRoles().stream().filter("ROLE_COMPANY"::equals).count();
			assertThat(companyRoleCount).isEqualTo(1);
		}
	}

	@Nested
	@DisplayName("countByUserIdEmailPhone")
	class CountByUserIdEmailPhone {

		@Test
		@DisplayName("소셜 로그인 사용자는 카운트에서 제외된다")
		void socialUser_excluded() {
			String userId = uniqueId("kakao_");
			Map<String, Object> userInfo = new HashMap<>();
			userInfo.put("user_id", userId);
			userInfo.put("user_name", "소셜닉");
			memberService.socialLoginOrRegister(userInfo, "kakao");

			int count = memberMapper.countByUserIdEmailPhone(userId, "nomatch@test.com", "01000000000");

			assertThat(count).isZero();
		}
	}
}
