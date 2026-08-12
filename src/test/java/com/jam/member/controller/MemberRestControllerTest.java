package com.jam.member.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import com.jam.config.MyBatisConfig;
import com.jam.global.jwt.JwtService;
import com.jam.global.jwt.TokenInfo;
import com.jam.global.security.JwtAuthenticationFilter;
import com.jam.member.dto.MemberDto;
import com.jam.member.service.MemberService;

import jakarta.servlet.http.Cookie;

/**
 * MemberRestController @WebMvcTest 슬라이스 테스트. job/community/fleaMarket과 같은 패턴:
 * 실제 SecurityConfig 대신 @PreAuthorize만 동작하는 최소 Security 설정(permitAll)을 따로 구성해서 씀.
 * MemberRestController는 인증 여부를 Spring Security가 아니라 @AuthenticationPrincipal이 null인지로
 * 직접 판단하므로(UnauthorizedException 수동 throw), 비인증 케이스도 200이 아닌 401로 응답 코드만 확인한다.
 */
@WebMvcTest(
		controllers = MemberRestController.class,
		excludeFilters = {
				@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class),
				@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = MyBatisConfig.class)
		})
@Import(MemberRestControllerTest.MethodSecurityTestConfig.class)
class MemberRestControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private MemberService memberService;
	@MockBean
	private PasswordEncoder encoder;
	@MockBean
	private JwtService jwtService;
	@MockBean
	private StringRedisTemplate redisTemplate;
	@MockBean
	private ValueOperations<String, String> valueOperations;

	private MemberDto loginUser;

	@BeforeEach
	void setUp() {
		reset(memberService, encoder, jwtService, redisTemplate, valueOperations);

		loginUser = new MemberDto();
		loginUser.setUser_id("user1");
		loginUser.setUser_name("tester");

		given(redisTemplate.opsForValue()).willReturn(valueOperations);
	}

	@TestConfiguration
	@EnableMethodSecurity
	static class MethodSecurityTestConfig {
		@Bean
		SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
			http.csrf(csrf -> csrf.disable())
					.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
			return http.build();
		}
	}

	@Nested
	@DisplayName("POST /api/member/join")
	class Join {

		private static final String VALID_BODY =
				"{\"user_id\":\"testuser1\",\"user_pw\":\"pass1234\",\"user_name\":\"닉네임\","
						+ "\"phone\":\"01012345678\",\"email\":\"a@a.com\"}";

		@Test
		@DisplayName("아이디가 형식에 맞지 않으면 400")
		void join_invalidUserId_badRequest() throws Exception {
			mockMvc.perform(post("/api/member/join")
							.contentType(MediaType.APPLICATION_JSON)
							.content("{\"user_id\":\"ab\",\"user_pw\":\"pass1234\",\"user_name\":\"닉네임\","
									+ "\"phone\":\"01012345678\",\"email\":\"a@a.com\"}"))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.detail").value("잘못된 형식의 아이디입니다."));
		}

		@Test
		@DisplayName("정상 가입 시 비밀번호가 인코딩되어 서비스로 전달되고 200을 응답한다")
		void join_success_encodesPassword() throws Exception {
			given(encoder.encode("pass1234")).willReturn("encodedPw");

			mockMvc.perform(post("/api/member/join")
							.contentType(MediaType.APPLICATION_JSON)
							.content(VALID_BODY))
					.andExpect(status().isOk());

			verify(memberService).join(org.mockito.ArgumentMatchers.argThat(m -> "encodedPw".equals(m.getUser_pw())));
		}
	}

	@Nested
	@DisplayName("GET /api/member/userId/check")
	class IdCheck {

		@Test
		@DisplayName("아이디가 없으면 400")
		void idChk_blank_badRequest() throws Exception {
			mockMvc.perform(get("/api/member/userId/check").param("userId", ""))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.detail").value("아이디를 입력하세요."));
		}

		@Test
		@DisplayName("형식이 올바르지 않으면 400")
		void idChk_invalidFormat_badRequest() throws Exception {
			mockMvc.perform(get("/api/member/userId/check").param("userId", "ab"))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.detail").value("아이디는 8~20자 이내로 영문, 숫자를 혼용하여 입력해 주세요"));
		}

		@Test
		@DisplayName("HTML 태그가 포함되면 400")
		void idChk_htmlTag_badRequest() throws Exception {
			mockMvc.perform(get("/api/member/userId/check").param("userId", "<b>test123</b>"))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.detail").value("HTML 태그는 허용되지 않습니다."));
		}

		@Test
		@DisplayName("이미 사용중이면 409")
		void idChk_duplicate_conflict() throws Exception {
			given(memberService.idCheck("testuser1")).willReturn(1);

			mockMvc.perform(get("/api/member/userId/check").param("userId", "testuser1"))
					.andExpect(status().isConflict())
					.andExpect(jsonPath("$.detail").value("이미 사용중인 아이디 입니다."));
		}

		@Test
		@DisplayName("사용 가능하면 200")
		void idChk_available_ok() throws Exception {
			given(memberService.idCheck("testuser1")).willReturn(0);

			mockMvc.perform(get("/api/member/userId/check").param("userId", "testuser1"))
					.andExpect(status().isOk());
		}
	}

	@Nested
	@DisplayName("GET /api/member/userName/check")
	class NameCheck {

		@Test
		@DisplayName("닉네임이 없으면 400")
		void nameChk_blank_badRequest() throws Exception {
			mockMvc.perform(get("/api/member/userName/check").param("userName", " "))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.detail").value("닉네임을 입력하세요."));
		}

		@Test
		@DisplayName("이미 사용 중이면 409")
		void nameChk_duplicate_conflict() throws Exception {
			given(memberService.nameCheck("닉네임")).willReturn(1);

			mockMvc.perform(get("/api/member/userName/check").param("userName", "닉네임"))
					.andExpect(status().isConflict())
					.andExpect(jsonPath("$.detail").value("이미 사용 중인 닉네임입니다."));
		}

		@Test
		@DisplayName("사용 가능하면 200")
		void nameChk_available_ok() throws Exception {
			given(memberService.nameCheck("닉네임")).willReturn(0);

			mockMvc.perform(get("/api/member/userName/check").param("userName", "닉네임"))
					.andExpect(status().isOk());
		}
	}

	@Nested
	@DisplayName("GET /api/member/phone/check")
	class PhoneCheck {

		@Test
		@DisplayName("형식이 올바르지 않으면 400")
		void phoneChk_invalidFormat_badRequest() throws Exception {
			mockMvc.perform(get("/api/member/phone/check").param("phone", "123"))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.detail").value("전화번호 형식이 올바르지 않습니다."));
		}

		@Test
		@DisplayName("이미 사용 중이면 409")
		void phoneChk_duplicate_conflict() throws Exception {
			given(memberService.phoneCheck("01012345678")).willReturn(1);

			mockMvc.perform(get("/api/member/phone/check").param("phone", "01012345678"))
					.andExpect(status().isConflict());
		}

		@Test
		@DisplayName("사용 가능하면 200")
		void phoneChk_available_ok() throws Exception {
			given(memberService.phoneCheck("01012345678")).willReturn(0);

			mockMvc.perform(get("/api/member/phone/check").param("phone", "01012345678"))
					.andExpect(status().isOk());
		}
	}

	@Nested
	@DisplayName("GET /api/member/email/check")
	class EmailCheck {

		@Test
		@DisplayName("형식이 올바르지 않으면 400")
		void emailChk_invalidFormat_badRequest() throws Exception {
			mockMvc.perform(get("/api/member/email/check").param("email", "notanemail"))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.detail").value("올바른 이메일 형식이 아닙니다."));
		}

		@Test
		@DisplayName("이미 사용 중이면 409")
		void emailChk_duplicate_conflict() throws Exception {
			given(memberService.emailCheck("a@a.com")).willReturn(1);

			mockMvc.perform(get("/api/member/email/check").param("email", "a@a.com"))
					.andExpect(status().isConflict());
		}

		@Test
		@DisplayName("사용 가능하면 200")
		void emailChk_available_ok() throws Exception {
			given(memberService.emailCheck("a@a.com")).willReturn(0);

			mockMvc.perform(get("/api/member/email/check").param("email", "a@a.com"))
					.andExpect(status().isOk());
		}
	}

	@Nested
	@DisplayName("POST /api/member/id/find")
	class FindId {

		@Test
		@DisplayName("이메일이 없으면 400")
		void findId_noEmail_badRequest() throws Exception {
			mockMvc.perform(post("/api/member/id/find")
							.contentType(MediaType.APPLICATION_JSON)
							.content("{\"phone\":\"01012345678\"}"))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.detail").value("이메일을 입력하세요."));
		}

		@Test
		@DisplayName("일치하는 회원이 없으면 404")
		void findId_notFound() throws Exception {
			given(memberService.FindId("a@a.com", "01012345678")).willReturn(null);

			mockMvc.perform(post("/api/member/id/find")
							.contentType(MediaType.APPLICATION_JSON)
							.content("{\"email\":\"a@a.com\",\"phone\":\"01012345678\"}"))
					.andExpect(status().isNotFound());
		}

		@Test
		@DisplayName("일치하는 회원이 있으면 마스킹된 아이디를 반환한다")
		void findId_found_returnsMaskedId() throws Exception {
			given(memberService.FindId("a@a.com", "01012345678")).willReturn("test1234");

			mockMvc.perform(post("/api/member/id/find")
							.contentType(MediaType.APPLICATION_JSON)
							.content("{\"email\":\"a@a.com\",\"phone\":\"01012345678\"}"))
					.andExpect(status().isOk())
					.andExpect(content().string("tes***34"));
		}
	}

	@Nested
	@DisplayName("POST /api/member/password/temp")
	class IssueTempPassword {

		@Test
		@DisplayName("아이디가 없으면 400")
		void issueTempPassword_noUserId_badRequest() throws Exception {
			mockMvc.perform(post("/api/member/password/temp")
							.contentType(MediaType.APPLICATION_JSON)
							.content("{\"email\":\"a@a.com\",\"phone\":\"01012345678\"}"))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.detail").value("아이디를 입력하세요."));
		}

		@Test
		@DisplayName("정상 요청이면 200을 응답하고 인증 플래그를 삭제한다")
		void issueTempPassword_success_deletesFlag() throws Exception {
			mockMvc.perform(post("/api/member/password/temp")
							.contentType(MediaType.APPLICATION_JSON)
							.content("{\"user_id\":\"user1\",\"email\":\"a@a.com\",\"phone\":\"01012345678\"}"))
					.andExpect(status().isOk());

			verify(memberService).updatePwAndSendEmail("user1", "a@a.com", "01012345678");
			verify(redisTemplate).delete("auth:mypage:user1");
		}
	}

	@Nested
	@DisplayName("PUT /api/member/userName")
	class UpdateUserName {

		@Test
		@DisplayName("비인증이면 401")
		void updateUserName_anonymous_unauthorized() throws Exception {
			mockMvc.perform(put("/api/member/userName")
							.contentType(MediaType.APPLICATION_JSON)
							.content("{\"user_name\":\"새닉네임\"}"))
					.andExpect(status().isUnauthorized());
		}

		@Test
		@DisplayName("인증 플래그가 유효하지 않으면 403")
		void updateUserName_invalidFlag_forbidden() throws Exception {
			given(valueOperations.get("auth:mypage:user1")).willReturn(null);

			mockMvc.perform(put("/api/member/userName")
							.with(user(loginUser))
							.cookie(new Cookie("Authorization", "token"))
							.contentType(MediaType.APPLICATION_JSON)
							.content("{\"user_name\":\"새닉네임\"}"))
					.andExpect(status().isForbidden())
					.andExpect(jsonPath("$.detail").value("유효하지 않은 인증 정보입니다."));
		}

		@Test
		@DisplayName("닉네임 형식이 올바르지 않으면 400")
		void updateUserName_invalidNickname_badRequest() throws Exception {
			given(valueOperations.get("auth:mypage:user1")).willReturn("true");

			mockMvc.perform(put("/api/member/userName")
							.with(user(loginUser))
							.cookie(new Cookie("Authorization", "token"))
							.contentType(MediaType.APPLICATION_JSON)
							.content("{\"user_name\":\"a\"}"))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.detail").value("닉네임은 3~10자 이내로 입력해주세요."));
		}

		@Test
		@DisplayName("정상 변경 시 200과 함께 새 토큰 쿠키가 발급되고 인증 플래그가 삭제된다")
		void updateUserName_success_reissuesTokens() throws Exception {
			given(valueOperations.get("auth:mypage:user1")).willReturn("true");
			given(jwtService.extractLoginType(any(), any(), any())).willReturn("local");
			given(jwtService.extractAutoLogin(any(), any(), any())).willReturn(true);
			Authentication authentication = new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
			given(memberService.updateUserNameAndTokens(any(), anyBoolean(), anyString(), any())).willReturn(authentication);
			given(jwtService.generateTokenFromAuthentication(any(), anyBoolean(), anyString()))
					.willReturn(new TokenInfo("access-token", "refresh-token"));

			mockMvc.perform(put("/api/member/userName")
							.with(user(loginUser))
							.cookie(new Cookie("Authorization", "token"))
							.contentType(MediaType.APPLICATION_JSON)
							.content("{\"user_name\":\"새닉네임\"}"))
					.andExpect(status().isOk())
					.andExpect(cookie().value("Authorization", "access-token"));

			verify(redisTemplate).delete("auth:mypage:user1");
		}
	}

	@Nested
	@DisplayName("PUT /api/member/phone")
	class UpdatePhone {

		@Test
		@DisplayName("비인증이면 401")
		void updatePhone_anonymous_unauthorized() throws Exception {
			mockMvc.perform(put("/api/member/phone")
							.contentType(MediaType.APPLICATION_JSON)
							.content("{\"phone\":\"01012345678\"}"))
					.andExpect(status().isUnauthorized());
		}

		@Test
		@DisplayName("인증 플래그가 유효하지 않으면 403")
		void updatePhone_invalidFlag_forbidden() throws Exception {
			given(valueOperations.get("auth:mypage:user1")).willReturn(null);

			mockMvc.perform(put("/api/member/phone")
							.with(user(loginUser))
							.contentType(MediaType.APPLICATION_JSON)
							.content("{\"phone\":\"01012345678\"}"))
					.andExpect(status().isForbidden());
		}

		@Test
		@DisplayName("정상 변경 시 200을 응답하고 인증 플래그를 삭제한다")
		void updatePhone_success() throws Exception {
			given(valueOperations.get("auth:mypage:user1")).willReturn("true");

			mockMvc.perform(put("/api/member/phone")
							.with(user(loginUser))
							.contentType(MediaType.APPLICATION_JSON)
							.content("{\"phone\":\"01012345678\"}"))
					.andExpect(status().isOk());

			verify(memberService).updatePhone(org.mockito.ArgumentMatchers.argThat(
					m -> "user1".equals(m.getUser_id()) && "01012345678".equals(m.getPhone())));
			verify(redisTemplate).delete("auth:mypage:user1");
		}
	}

	@Nested
	@DisplayName("POST /api/member/verify-password")
	class VerifyPassword {

		@Test
		@DisplayName("비밀번호가 없으면 400")
		void verifyPassword_noPassword_badRequest() throws Exception {
			mockMvc.perform(post("/api/member/verify-password")
							.with(user(loginUser))
							.contentType(MediaType.APPLICATION_JSON)
							.content("{}"))
					.andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("비인증이면 401")
		void verifyPassword_anonymous_unauthorized() throws Exception {
			mockMvc.perform(post("/api/member/verify-password")
							.contentType(MediaType.APPLICATION_JSON)
							.content("{\"user_pw\":\"pass1234\"}"))
					.andExpect(status().isUnauthorized());
		}

		@Test
		@DisplayName("비밀번호가 일치하지 않으면 401")
		void verifyPassword_mismatch_unauthorized() throws Exception {
			given(memberService.getPassword(any())).willReturn("encodedPw");
			given(encoder.matches("wrongpw", "encodedPw")).willReturn(false);

			mockMvc.perform(post("/api/member/verify-password")
							.with(user(loginUser))
							.contentType(MediaType.APPLICATION_JSON)
							.content("{\"user_pw\":\"wrongpw\"}"))
					.andExpect(status().isUnauthorized())
					.andExpect(jsonPath("$.detail").value("비밀번호가 일치하지 않습니다."));
		}

		@Test
		@DisplayName("비밀번호가 일치하면 200을 응답하고 인증 플래그를 저장한다")
		void verifyPassword_match_setsFlag() throws Exception {
			given(memberService.getPassword(any())).willReturn("encodedPw");
			given(encoder.matches("pass1234", "encodedPw")).willReturn(true);

			mockMvc.perform(post("/api/member/verify-password")
							.with(user(loginUser))
							.contentType(MediaType.APPLICATION_JSON)
							.content("{\"user_pw\":\"pass1234\"}"))
					.andExpect(status().isOk());

			verify(valueOperations).set(eq("auth:mypage:user1"), eq("true"), any(java.time.Duration.class));
		}
	}

	@Nested
	@DisplayName("PUT /api/member/password")
	class UpdatePw {

		@Test
		@DisplayName("비인증이면 401")
		void updatePw_anonymous_unauthorized() throws Exception {
			mockMvc.perform(put("/api/member/password")
							.contentType(MediaType.APPLICATION_JSON)
							.content("{\"user_pw\":\"newpass123\"}"))
					.andExpect(status().isUnauthorized());
		}

		@Test
		@DisplayName("형식이 올바르지 않으면 400")
		void updatePw_invalidFormat_badRequest() throws Exception {
			mockMvc.perform(put("/api/member/password")
							.with(user(loginUser))
							.contentType(MediaType.APPLICATION_JSON)
							.content("{\"user_pw\":\"a\"}"))
					.andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("인증 플래그가 유효하지 않으면 403")
		void updatePw_invalidFlag_forbidden() throws Exception {
			given(valueOperations.get("auth:mypage:user1")).willReturn(null);

			mockMvc.perform(put("/api/member/password")
							.with(user(loginUser))
							.contentType(MediaType.APPLICATION_JSON)
							.content("{\"user_pw\":\"newpass123\"}"))
					.andExpect(status().isForbidden());
		}

		@Test
		@DisplayName("정상 변경 시 200을 응답하고 로그아웃 처리(인증 쿠키 삭제)한다")
		void updatePw_success_clearsAuthCookies() throws Exception {
			given(valueOperations.get("auth:mypage:user1")).willReturn("true");
			given(encoder.encode("newpass123")).willReturn("encodedNewPw");

			mockMvc.perform(put("/api/member/password")
							.with(user(loginUser))
							.contentType(MediaType.APPLICATION_JSON)
							.content("{\"user_pw\":\"newpass123\"}"))
					.andExpect(status().isOk())
					.andExpect(cookie().maxAge("Authorization", 0));

			verify(memberService).updatePw("user1", "encodedNewPw");
			verify(redisTemplate).delete("auth:mypage:user1");
		}
	}

	@Nested
	@DisplayName("PUT /api/member/address")
	class UpdateAddress {

		@Test
		@DisplayName("주소가 없으면 비인증 상태여도 400이 우선 반환된다")
		void updateAddress_noAddress_badRequestBeforeAuthCheck() throws Exception {
			mockMvc.perform(put("/api/member/address")
							.contentType(MediaType.APPLICATION_JSON)
							.content("{}"))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.detail").value("주소를 입력하세요."));
		}

		@Test
		@DisplayName("주소는 있지만 비인증이면 401")
		void updateAddress_anonymous_unauthorized() throws Exception {
			mockMvc.perform(put("/api/member/address")
							.contentType(MediaType.APPLICATION_JSON)
							.content("{\"address\":\"서울시\"}"))
					.andExpect(status().isUnauthorized());
		}

		@Test
		@DisplayName("정상 변경 시 200을 응답한다")
		void updateAddress_success() throws Exception {
			mockMvc.perform(put("/api/member/address")
							.with(user(loginUser))
							.contentType(MediaType.APPLICATION_JSON)
							.content("{\"address\":\"서울시\"}"))
					.andExpect(status().isOk());

			verify(memberService).updateAddress("서울시", "user1");
		}
	}

	@Nested
	@DisplayName("DELETE /api/member/me")
	class DeleteAccount {

		@Test
		@DisplayName("비인증이면 401")
		void deleteAccount_anonymous_unauthorized() throws Exception {
			mockMvc.perform(delete("/api/member/me"))
					.andExpect(status().isUnauthorized());
		}

		@Test
		@DisplayName("인증 플래그가 유효하지 않으면 403")
		void deleteAccount_invalidFlag_forbidden() throws Exception {
			given(valueOperations.get("auth:mypage:user1")).willReturn(null);

			mockMvc.perform(delete("/api/member/me").with(user(loginUser)))
					.andExpect(status().isForbidden());
		}

		@Test
		@DisplayName("정상 탈퇴 시 200을 응답하고 회원 데이터를 삭제한다")
		void deleteAccount_success() throws Exception {
			given(valueOperations.get("auth:mypage:user1")).willReturn("true");

			mockMvc.perform(delete("/api/member/me").with(user(loginUser)))
					.andExpect(status().isOk());

			verify(memberService).deleteAccount("user1");
			verify(redisTemplate).delete("auth:mypage:user1");
		}
	}

	@Nested
	@DisplayName("POST /api/member/convertBusiness")
	class ConvertBusiness {

		@Test
		@DisplayName("비인증이면 401")
		void convertBusiness_anonymous_unauthorized() throws Exception {
			mockMvc.perform(post("/api/member/convertBusiness")
							.contentType(MediaType.APPLICATION_JSON)
							.content("{\"company_name\":\"회사\"}"))
					.andExpect(status().isUnauthorized());
		}

		@Test
		@DisplayName("회사명이 없으면 400")
		void convertBusiness_noCompanyName_badRequest() throws Exception {
			mockMvc.perform(post("/api/member/convertBusiness")
							.with(user(loginUser))
							.cookie(new Cookie("Authorization", "token"))
							.contentType(MediaType.APPLICATION_JSON)
							.content("{}"))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.detail").value("회사명을 입력하세요."));
		}

		@Test
		@DisplayName("정상 전환 시 200과 성공 메시지를 응답한다")
		void convertBusiness_success() throws Exception {
			given(jwtService.extractLoginType(any(), any(), any())).willReturn("local");
			given(jwtService.extractAutoLogin(any(), any(), any())).willReturn(true);
			Authentication authentication = new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
			given(memberService.convertBusiness(eq("user1"), eq("회사"), any())).willReturn(authentication);
			given(jwtService.generateTokenFromAuthentication(any(), anyBoolean(), anyString()))
					.willReturn(new TokenInfo("access-token", "refresh-token"));

			mockMvc.perform(post("/api/member/convertBusiness")
							.with(user(loginUser))
							.cookie(new Cookie("Authorization", "token"))
							.contentType(MediaType.APPLICATION_JSON)
							.content("{\"company_name\":\"회사\"}"))
					.andExpect(status().isOk())
					.andExpect(content().string("기업회원 전환 성공"));
		}
	}
}
