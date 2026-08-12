package com.jam.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.jam.global.exception.ConflictException;
import com.jam.member.dto.MemberDto;
import com.jam.member.mapper.MemberMapper;

import jakarta.mail.internet.MimeMessage;
import jakarta.mail.Session;

/**
 * MemberService에 대한 Mockito 단위 테스트.
 * 단순 mapper 위임 메서드는 위임 확인용으로만 작성했고, XML에 있는 실제 SQL은
 * 이 유닛테스트로 검증되지 않는다. kakao/naver 탈퇴 연동(RestTemplate 직접 생성, 외부 API 호출)은
 * 목으로 대체할 수 없어 unit test 범위에서 제외했다.
 */
@ExtendWith(MockitoExtension.class)
class MemberServiceUnitTest {

	@Mock
	private MemberMapper memberMapper;
	@Mock
	private PasswordEncoder encoder;
	@Mock
	private RedisTemplate<String, String> stringRedisTemplate;
	@Mock
	private JavaMailSender mailSender;
	@Mock
	private ValueOperations<String, String> valueOperations;

	@InjectMocks
	private MemberService memberService;

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(memberService, "mailUsername", "no-reply@jam.com");
	}

	@AfterEach
	void clearSecurityContext() {
		SecurityContextHolder.clearContext();
	}

	@Nested
	@DisplayName("join")
	class Join {

		private MemberDto member() {
			MemberDto member = new MemberDto();
			member.setUser_id("newuser1");
			member.setUser_name("newnick");
			return member;
		}

		@Test
		@DisplayName("기본 권한 부여에 성공하면 정상적으로 가입 처리된다")
		void join_roleAssigned_success() {
			given(memberMapper.assignDefaultRoleToMember("newuser1")).willReturn(1);
			given(stringRedisTemplate.opsForValue()).willReturn(valueOperations);

			memberService.join(member());

			verify(memberMapper).memberJoin(any(MemberDto.class));
			verify(valueOperations).set("users:name:newuser1", "newnick");
		}

		@Test
		@DisplayName("기본 권한 부여에 실패하면(0건) ConflictException을 던진다")
		void join_roleAssignFails_throwsConflict() {
			given(memberMapper.assignDefaultRoleToMember("newuser1")).willReturn(0);

			assertThatThrownBy(() -> memberService.join(member()))
					.isInstanceOf(ConflictException.class);

			verify(stringRedisTemplate, never()).opsForValue();
		}
	}

	@Nested
	@DisplayName("중복확인 위임 메서드")
	class DuplicateChecks {

		@Test
		@DisplayName("idCheck - mapper 결과 그대로 반환")
		void idCheck_delegates() {
			given(memberMapper.idCheck("id1")).willReturn(1);
			assertThat(memberService.idCheck("id1")).isEqualTo(1);
		}

		@Test
		@DisplayName("nameCheck - mapper 결과 그대로 반환")
		void nameCheck_delegates() {
			given(memberMapper.nameCheck("nick1")).willReturn(0);
			assertThat(memberService.nameCheck("nick1")).isEqualTo(0);
		}

		@Test
		@DisplayName("phoneCheck - mapper 결과 그대로 반환")
		void phoneCheck_delegates() {
			given(memberMapper.phoneCheck("01011112222")).willReturn(1);
			assertThat(memberService.phoneCheck("01011112222")).isEqualTo(1);
		}

		@Test
		@DisplayName("emailCheck - mapper 결과 그대로 반환")
		void emailCheck_delegates() {
			given(memberMapper.emailCheck("a@a.com")).willReturn(0);
			assertThat(memberService.emailCheck("a@a.com")).isEqualTo(0);
		}
	}

	@Nested
	@DisplayName("FindId")
	class FindId {

		@Test
		@DisplayName("mapper 결과 그대로 반환한다")
		void findId_delegates() {
			given(memberMapper.findId("a@a.com", "01011112222")).willReturn("finduser");

			assertThat(memberService.FindId("a@a.com", "01011112222")).isEqualTo("finduser");
		}
	}

	@Nested
	@DisplayName("updatePwAndSendEmail")
	class UpdatePwAndSendEmail {

		@Test
		@DisplayName("일치하는 사용자가 없으면(0건) 아무 것도 하지 않고 종료한다")
		void noMatchingUser_doesNothing() {
			given(memberMapper.countByUserIdEmailPhone("user1", "a@a.com", "01011112222")).willReturn(0);

			memberService.updatePwAndSendEmail("user1", "a@a.com", "01011112222");

			verify(memberMapper, never()).updatePw(anyString(), anyString());
			verify(mailSender, never()).createMimeMessage();
		}

		@Test
		@DisplayName("비밀번호 변경에 실패하면(갱신 0건) IllegalStateException을 던지고 메일을 보내지 않는다")
		void updateFails_throwsIllegalState_doesNotSendMail() {
			given(memberMapper.countByUserIdEmailPhone("user1", "a@a.com", "01011112222")).willReturn(1);
			given(encoder.encode(anyString())).willReturn("encoded");
			given(memberMapper.updatePw(eq("user1"), anyString())).willReturn(0);

			assertThatThrownBy(() -> memberService.updatePwAndSendEmail("user1", "a@a.com", "01011112222"))
					.isInstanceOf(IllegalStateException.class);

			verify(mailSender, never()).createMimeMessage();
		}

		@Test
		@DisplayName("일치하는 사용자가 있으면 비밀번호를 한 번만 갱신하고 메일을 전송한다")
		void matchingUser_updatesPwOnceAndSendsMail() throws Exception {
			given(memberMapper.countByUserIdEmailPhone("user1", "a@a.com", "01011112222")).willReturn(1);
			given(encoder.encode(anyString())).willReturn("encoded");
			given(memberMapper.updatePw(eq("user1"), anyString())).willReturn(1);
			MimeMessage message = new MimeMessage((Session) null);
			given(mailSender.createMimeMessage()).willReturn(message);

			memberService.updatePwAndSendEmail("user1", "a@a.com", "01011112222");

			verify(memberMapper, times(1)).updatePw(eq("user1"), anyString());
			verify(mailSender).send(message);
		}
	}

	@Nested
	@DisplayName("generateTempPassword")
	class GenerateTempPassword {

		@Test
		@DisplayName("길이 10자리의 임시 비밀번호를 생성한다")
		void generatesTenCharPassword() {
			String tempPw = memberService.generateTempPassword();

			assertThat(tempPw).hasSize(10);
		}
	}

	@Nested
	@DisplayName("socialLoginOrRegister")
	class SocialLoginOrRegister {

		@Test
		@DisplayName("가입 이력이 없으면 신규 가입 처리하고 기본 닉네임을 사용한다")
		void newUser_noDuplicateName_registers() {
			Map<String, Object> userInfo = new HashMap<>();
			userInfo.put("user_id", "kakao_1");
			userInfo.put("user_name", "홍길동");
			given(memberMapper.findSocialUser("kakao_1")).willReturn(0);
			given(memberMapper.nameCheck("kakao_홍길동")).willReturn(0);
			given(encoder.encode(anyString())).willReturn("encoded");
			given(memberMapper.assignDefaultRoleToMember("kakao_1")).willReturn(1);

			MemberDto result = memberService.socialLoginOrRegister(userInfo, "kakao");

			assertThat(result.getUser_name()).isEqualTo("kakao_홍길동");
			verify(memberMapper).SocialRegister(userInfo);
		}

		@Test
		@DisplayName("기본 닉네임이 중복이면 숫자를 붙여 유일한 닉네임을 만든다")
		void newUser_duplicateName_appendsCount() {
			Map<String, Object> userInfo = new HashMap<>();
			userInfo.put("user_id", "kakao_2");
			userInfo.put("user_name", "홍길동");
			given(memberMapper.findSocialUser("kakao_2")).willReturn(0);
			given(memberMapper.nameCheck("kakao_홍길동")).willReturn(1);
			given(memberMapper.nameCheck("kakao_홍길동_1")).willReturn(0);
			given(encoder.encode(anyString())).willReturn("encoded");
			given(memberMapper.assignDefaultRoleToMember("kakao_2")).willReturn(1);

			MemberDto result = memberService.socialLoginOrRegister(userInfo, "kakao");

			assertThat(result.getUser_name()).isEqualTo("kakao_홍길동_1");
		}

		@Test
		@DisplayName("신규 가입 시 기본 권한 부여에 실패하면 ConflictException을 던진다")
		void newUser_roleAssignFails_throwsConflict() {
			Map<String, Object> userInfo = new HashMap<>();
			userInfo.put("user_id", "kakao_3");
			userInfo.put("user_name", "홍길동");
			given(memberMapper.findSocialUser("kakao_3")).willReturn(0);
			given(memberMapper.nameCheck("kakao_홍길동")).willReturn(0);
			given(encoder.encode(anyString())).willReturn("encoded");
			given(memberMapper.assignDefaultRoleToMember("kakao_3")).willReturn(0);

			assertThatThrownBy(() -> memberService.socialLoginOrRegister(userInfo, "kakao"))
					.isInstanceOf(ConflictException.class);
		}

		@Test
		@DisplayName("이미 가입된 사용자면 기존 정보를 조회해 반환한다")
		void existingUser_returnsFoundUser() {
			Map<String, Object> userInfo = new HashMap<>();
			userInfo.put("user_id", "kakao_4");
			MemberDto existing = new MemberDto();
			existing.setUser_id("kakao_4");
			given(memberMapper.findSocialUser("kakao_4")).willReturn(1);
			given(memberMapper.findByUserInfo("kakao_4")).willReturn(existing);

			MemberDto result = memberService.socialLoginOrRegister(userInfo, "kakao");

			assertThat(result).isEqualTo(existing);
			verify(memberMapper, never()).SocialRegister(any());
		}
	}

	@Nested
	@DisplayName("updateUserName")
	class UpdateUserName {

		@Test
		@DisplayName("변경에 성공하면(1건) true를 반환하고 Redis에 캐시한다")
		void success_returnsTrue() {
			MemberDto member = new MemberDto();
			member.setUser_id("user1");
			member.setUser_name("newnick");
			given(stringRedisTemplate.opsForValue()).willReturn(valueOperations);
			given(memberMapper.updateUserName(member)).willReturn(1);

			boolean result = memberService.updateUserName(member);

			assertThat(result).isTrue();
			verify(valueOperations).set("users:name:user1", "newnick");
		}

		@Test
		@DisplayName("변경 대상이 없으면(0건) false를 반환한다")
		void notFound_returnsFalse() {
			MemberDto member = new MemberDto();
			member.setUser_id("user1");
			member.setUser_name("newnick");
			given(stringRedisTemplate.opsForValue()).willReturn(valueOperations);
			given(memberMapper.updateUserName(member)).willReturn(0);

			assertThat(memberService.updateUserName(member)).isFalse();
		}

		@Test
		@DisplayName("mapper에서 예외가 발생하면 그대로 전파한다")
		void mapperThrows_propagates() {
			MemberDto member = new MemberDto();
			member.setUser_id("user1");
			member.setUser_name("newnick");
			given(stringRedisTemplate.opsForValue()).willReturn(valueOperations);
			given(memberMapper.updateUserName(member)).willThrow(new RuntimeException("DB 오류"));

			assertThatThrownBy(() -> memberService.updateUserName(member))
					.isInstanceOf(RuntimeException.class)
					.hasMessage("DB 오류");
		}
	}

	@Nested
	@DisplayName("단순 mapper 위임 메서드 (mapper 위임 확인용, XML SQL 검증 X)")
	class MapperDelegation {

		@Test
		@DisplayName("updatePhone - mapper를 호출한다")
		void updatePhone_delegates() {
			MemberDto member = new MemberDto();
			memberService.updatePhone(member);
			verify(memberMapper).updatePhone(member);
		}

		@Test
		@DisplayName("getPassword - mapper 결과 그대로 반환")
		void getPassword_delegates() {
			MemberDto member = new MemberDto();
			given(memberMapper.getPassword(member)).willReturn("encodedPw");
			assertThat(memberService.getPassword(member)).isEqualTo("encodedPw");
		}

		@Test
		@DisplayName("updatePw - mapper를 호출한다")
		void updatePw_delegates() {
			memberService.updatePw("user1", "encodedPw");
			verify(memberMapper).updatePw("user1", "encodedPw");
		}

		@Test
		@DisplayName("updateAddress - mapper 결과 그대로 반환")
		void updateAddress_delegates() {
			given(memberMapper.updateAddress("서울", "user1")).willReturn(1);
			assertThat(memberService.updateAddress("서울", "user1")).isEqualTo(1);
		}

		@Test
		@DisplayName("getUserName - mapper 결과 그대로 반환")
		void getUserName_delegates() {
			given(memberMapper.getUserName("user1")).willReturn("nick1");
			assertThat(memberService.getUserName("user1")).isEqualTo("nick1");
		}

		@Test
		@DisplayName("getUserId - mapper 결과 그대로 반환")
		void getUserId_delegates() {
			given(memberMapper.getUserId("nick1")).willReturn("user1");
			assertThat(memberService.getUserId("nick1")).isEqualTo("user1");
		}

		@Test
		@DisplayName("addRefreshToken - mapper 결과 그대로 반환")
		void addRefreshToken_delegates() {
			given(memberMapper.addRefreshToken("user1", "token")).willReturn(1);
			assertThat(memberService.addRefreshToken("user1", "token")).isEqualTo(1);
		}

		@Test
		@DisplayName("deleteRefreshToken - mapper 결과 그대로 반환")
		void deleteRefreshToken_delegates() {
			given(memberMapper.deleteRefreshToken("user1")).willReturn(1);
			assertThat(memberService.deleteRefreshToken("user1")).isEqualTo(1);
		}

		@Test
		@DisplayName("getRefreshToken - mapper 결과 그대로 반환")
		void getRefreshToken_delegates() {
			given(memberMapper.getRefreshToken("user1")).willReturn("token");
			assertThat(memberService.getRefreshToken("user1")).isEqualTo("token");
		}

		@Test
		@DisplayName("getUserProfile - mapper 결과 그대로 반환")
		void getUserProfile_delegates() {
			MemberDto profile = new MemberDto();
			given(memberMapper.getUserProfile("user1")).willReturn(profile);
			assertThat(memberService.getUserProfile("user1")).isEqualTo(profile);
		}

		@Test
		@DisplayName("findByUserInfo - mapper 결과 그대로 반환")
		void findByUserInfo_delegates() {
			MemberDto found = new MemberDto();
			given(memberMapper.findByUserInfo("user1")).willReturn(found);
			assertThat(memberService.findByUserInfo("user1")).isEqualTo(found);
		}

		@Test
		@DisplayName("isActiveUser - mapper 결과 그대로 반환")
		void isActiveUser_delegates() {
			given(memberMapper.isActiveUser("user1")).willReturn(true);
			assertThat(memberService.isActiveUser("user1")).isTrue();
		}
	}

	@Nested
	@DisplayName("deleteAccount")
	class DeleteAccount {

		@Test
		@DisplayName("mapper로 무작위 익명 닉네임과 함께 탈퇴 처리하고 캐시된 닉네임 키를 Redis에서 삭제한다")
		void deletesMemberAndCachedNickname() {
			memberService.deleteAccount("user1");

			ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
			verify(memberMapper).deleteAccount(eq("user1"), nameCaptor.capture());
			assertThat(nameCaptor.getValue()).startsWith("del_").hasSize(20);
			verify(stringRedisTemplate).delete("users:name:user1");
		}
	}

	@Nested
	@DisplayName("authenticateUser / authenticateSocialUser")
	class Authenticate {

		@Test
		@DisplayName("authenticateUser는 SecurityContext에 인증 정보를 설정하고 반환한다")
		void authenticateUser_setsSecurityContext() {
			MemberDto user = new MemberDto();
			user.setUser_id("user1");

			Authentication authentication = memberService.authenticateUser(user);

			assertThat(authentication.getPrincipal()).isEqualTo(user);
			assertThat(SecurityContextHolder.getContext().getAuthentication()).isEqualTo(authentication);
		}

		@Test
		@DisplayName("authenticateSocialUser는 SecurityContext를 건드리지 않고 인증 정보만 반환한다")
		void authenticateSocialUser_doesNotTouchSecurityContext() {
			MemberDto user = new MemberDto();
			user.setUser_id("user1");

			Authentication authentication = memberService.authenticateSocialUser(user);

			assertThat(authentication.getPrincipal()).isEqualTo(user);
			assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
		}
	}

	@Nested
	@DisplayName("updateUserNameAndTokens")
	class UpdateUserNameAndTokens {

		@Test
		@DisplayName("닉네임 변경에 실패하면 IllegalStateException을 던진다")
		void updateFails_throwsIllegalState() {
			MemberDto user = new MemberDto();
			user.setUser_id("user1");
			user.setUser_name("newnick");
			given(stringRedisTemplate.opsForValue()).willReturn(valueOperations);
			given(memberMapper.updateUserName(user)).willReturn(0);

			assertThatThrownBy(() -> memberService.updateUserNameAndTokens(user, false, "local", null))
					.isInstanceOf(IllegalStateException.class);
		}

		@Test
		@DisplayName("닉네임 변경에 성공하면 인증 정보를 발급한다")
		void updateSucceeds_returnsAuthentication() {
			MemberDto user = new MemberDto();
			user.setUser_id("user1");
			user.setUser_name("newnick");
			given(stringRedisTemplate.opsForValue()).willReturn(valueOperations);
			given(memberMapper.updateUserName(user)).willReturn(1);

			Authentication authentication = memberService.updateUserNameAndTokens(user, false, "local", null);

			assertThat(authentication.getPrincipal()).isEqualTo(user);
		}
	}

	@Nested
	@DisplayName("convertBusiness")
	class ConvertBusiness {

		@Test
		@DisplayName("roles가 없으면 새로 만들어서 ROLE_COMPANY를 추가한다")
		void nullRoles_createsListWithRoleCompany() {
			MemberDto user = new MemberDto();
			user.setUser_id("user1");

			memberService.convertBusiness("user1", "회사이름", user);

			assertThat(user.getRoles()).containsExactly("ROLE_COMPANY");
			ArgumentCaptor<Map<String, String>> captor = ArgumentCaptor.forClass(Map.class);
			verify(memberMapper).updateCompanyName(captor.capture());
			assertThat(captor.getValue()).containsEntry("user_id", "user1").containsEntry("company_name", "회사이름");
			verify(memberMapper).insertCompanyRole(any());
		}

		@Test
		@DisplayName("이미 ROLE_COMPANY를 가지고 있으면 중복 추가하지 않는다")
		void alreadyHasRoleCompany_doesNotDuplicate() {
			MemberDto user = new MemberDto();
			user.setUser_id("user1");
			List<String> roles = new ArrayList<>(List.of("ROLE_COMPANY"));
			user.setRoles(roles);

			memberService.convertBusiness("user1", "회사이름", user);

			assertThat(user.getRoles()).containsExactly("ROLE_COMPANY");
		}

		@Test
		@DisplayName("성공 시 인증 정보를 발급한다")
		void success_returnsAuthentication() {
			MemberDto user = new MemberDto();
			user.setUser_id("user1");

			Authentication authentication = memberService.convertBusiness("user1", "회사이름", user);

			assertThat(((UserDetails) authentication.getPrincipal()).getUsername()).isEqualTo("user1");
		}
	}
}
