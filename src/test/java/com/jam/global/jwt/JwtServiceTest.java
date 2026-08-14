package com.jam.global.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;

import com.jam.global.jwt.TokenInfo.TokenStatus;
import com.jam.global.util.SecurityUtil;
import com.jam.member.dto.MemberDto;
import com.jam.member.service.MemberService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.Cookie;

/**
 * JwtService에 대한 Mockito 유닛테스트. JwtTokenProvider/MemberService/StringRedisTemplate은 mock 처리하고,
 * 쿠키 read/write는 실제 MockHttpServletRequest/Response로 검증한다.
 */
@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

	@Mock
	private JwtTokenProvider jwtTokenProvider;
	@Mock
	private MemberService memberService;
	@Mock
	private StringRedisTemplate redisTemplate;

	@InjectMocks
	private JwtService jwtService;

	private MockHttpServletRequest request;
	private MockHttpServletResponse response;

	@org.junit.jupiter.api.BeforeEach
	void setUp() {
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
	}

	private MemberDto memberOf(String userId, String userName, String companyName, List<String> roles) {
		MemberDto member = new MemberDto();
		member.setUser_id(userId);
		member.setUser_name(userName);
		member.setCompany_name(companyName);
		member.setRoles(roles);
		return member;
	}

	private Claims claimsOf(String userId, String userName, String companyName, List<String> roles) {
		return Jwts.claims()
				.subject(userId)
				.add("userName", userName)
				.add("companyName", companyName)
				.add("auth", roles)
				.build();
	}

	@Nested
	@DisplayName("extractUserInfoFromToken")
	class ExtractUserInfoFromToken {

		@Test
		@DisplayName("토큰 클레임에서 사용자 정보를 그대로 꺼내온다")
		void extractsUserInfoFromClaims() {
			given(jwtTokenProvider.getClaims("access-token"))
					.willReturn(claimsOf("user1", "닉네임", "회사명", List.of("ROLE_USER")));

			MemberDto result = jwtService.extractUserInfoFromToken("access-token");

			assertThat(result.getUser_id()).isEqualTo("user1");
			assertThat(result.getUser_name()).isEqualTo("닉네임");
			assertThat(result.getCompany_name()).isEqualTo("회사명");
			assertThat(result.getRoles()).containsExactly("ROLE_USER");
		}
	}

	@Nested
	@DisplayName("getAuthentication")
	class GetAuthentication {

		@Test
		@DisplayName("accessToken이 VALID면 토큰 정보로 Authentication을 만들어 반환한다")
		void validAccessToken_returnsAuthentication() {
			request.setCookies(new Cookie("Authorization", "access-token"));
			given(jwtTokenProvider.validateToken("access-token")).willReturn(TokenStatus.VALID);
			given(jwtTokenProvider.getClaims("access-token"))
					.willReturn(claimsOf("user1", "닉네임", null, List.of("ROLE_USER")));

			Authentication result = jwtService.getAuthentication(new Cookie[0], request, response);

			MemberDto principal = (MemberDto) result.getPrincipal();
			assertThat(principal.getUser_id()).isEqualTo("user1");
			assertThat(result.getAuthorities()).extracting(Object::toString).containsExactly("ROLE_USER");
		}

		@Test
		@DisplayName("tokenStatus가 null이면 EMPTY로 간주하고, 토큰이 아예 없으면 null을 반환한다")
		void nullTokenStatus_noTokens_returnsNull() {
			given(jwtTokenProvider.validateToken(null)).willReturn(null);

			Authentication result = jwtService.getAuthentication(new Cookie[0], request, response);

			assertThat(result).isNull();
		}

		@Test
		@DisplayName("accessToken이 EMPTY이고 refreshToken도 유효하지 않으면 쿠키를 정리하고 null을 반환한다")
		void emptyAccessToken_invalidRefreshToken_clearsCookiesAndReturnsNull() {
			request.setCookies(new Cookie("RefreshToken", "bad-refresh"));
			given(jwtTokenProvider.validateToken(null)).willReturn(TokenStatus.EMPTY);
			given(jwtTokenProvider.validateToken("bad-refresh")).willReturn(TokenStatus.INVALID);

			Authentication result = jwtService.getAuthentication(new Cookie[0], request, response);

			assertThat(result).isNull();
			assertThat(response.getCookie("Authorization").getMaxAge()).isZero();
			assertThat(response.getCookie("RefreshToken").getMaxAge()).isZero();
		}

		@Test
		@DisplayName("accessToken이 EXPIRED이고 refreshToken이 유효하지만 autoLogin=false면 쿠키를 정리하고 null을 반환한다")
		void expiredAccessToken_validRefreshToken_autoLoginFalse_clearsCookiesAndReturnsNull() {
			request.setCookies(new Cookie("Authorization", "access-token"), new Cookie("RefreshToken", "refresh-token"));
			given(jwtTokenProvider.validateToken("access-token")).willReturn(TokenStatus.EXPIRED);
			given(jwtTokenProvider.validateToken("refresh-token")).willReturn(TokenStatus.VALID);
			given(jwtTokenProvider.getAutoLoginFromRefreshToken("refresh-token")).willReturn(false);

			Authentication result = jwtService.getAuthentication(new Cookie[0], request, response);

			assertThat(result).isNull();
			assertThat(response.getCookie("Authorization").getMaxAge()).isZero();
			verify(memberService, never()).getRefreshToken(any());
		}

		@Test
		@DisplayName("accessToken이 EXPIRED이고 refreshToken이 유효하며 autoLogin=true, DB와 일치하면 토큰을 재발급한다")
		void expiredAccessToken_validRefreshToken_autoLoginTrue_matchesDb_renewsTokens() {
			String refreshToken = "refresh-token";
			request.setCookies(new Cookie("Authorization", "access-token"), new Cookie("RefreshToken", refreshToken));
			given(jwtTokenProvider.validateToken("access-token")).willReturn(TokenStatus.EXPIRED);
			given(jwtTokenProvider.validateToken(refreshToken)).willReturn(TokenStatus.VALID);
			given(jwtTokenProvider.getAutoLoginFromRefreshToken(refreshToken)).willReturn(true);
			given(jwtTokenProvider.extractUserId(refreshToken)).willReturn("user1");
			given(jwtTokenProvider.extractJti(refreshToken)).willReturn("jti-1");
			given(jwtTokenProvider.extractLoginType(refreshToken)).willReturn("local");
			given(memberService.getRefreshToken("user1")).willReturn(SecurityUtil.hashToken(refreshToken));

			MemberDto userInfo = memberOf("user1", "닉네임", null, List.of("ROLE_USER"));
			given(memberService.findByUserInfo("user1")).willReturn(userInfo);

			TokenInfo newToken = TokenInfo.builder().accessToken("new-access").refreshToken("new-refresh").build();
			given(jwtTokenProvider.generateToken(any(Authentication.class), eq(true), eq("local"))).willReturn(newToken);

			ValueOperations<String, String> valueOperations = org.mockito.Mockito.mock(ValueOperations.class);
			given(redisTemplate.opsForValue()).willReturn(valueOperations);

			Authentication result = jwtService.getAuthentication(new Cookie[0], request, response);

			assertThat(result.getPrincipal()).isSameAs(userInfo);
			verify(memberService).addRefreshToken(eq("user1"), eq(SecurityUtil.hashToken("new-refresh")));
			assertThat(response.getCookie("Authorization").getValue()).isEqualTo("new-access");
			assertThat(response.getCookie("RefreshToken").getValue()).isEqualTo("new-refresh");
			verify(valueOperations).set(eq("refresh:prev:user1:jti-1"), eq("1"), eq(10L), eq(TimeUnit.SECONDS));
		}

		@Test
		@DisplayName("DB의 RefreshToken과 불일치하지만 동시 요청으로 이미 갱신된 토큰이면(Redis grace period) 인증을 허용한다")
		void mismatchedDbToken_gracePeriodInRedis_returnsAuthentication() {
			String refreshToken = "refresh-token";
			request.setCookies(new Cookie("Authorization", "access-token"), new Cookie("RefreshToken", refreshToken));
			given(jwtTokenProvider.validateToken("access-token")).willReturn(TokenStatus.EXPIRED);
			given(jwtTokenProvider.validateToken(refreshToken)).willReturn(TokenStatus.VALID);
			given(jwtTokenProvider.getAutoLoginFromRefreshToken(refreshToken)).willReturn(true);
			given(jwtTokenProvider.extractUserId(refreshToken)).willReturn("user1");
			given(jwtTokenProvider.extractJti(refreshToken)).willReturn("jti-1");
			given(memberService.getRefreshToken("user1")).willReturn("different-hash");
			given(redisTemplate.hasKey("refresh:prev:user1:jti-1")).willReturn(true);

			MemberDto userInfo = memberOf("user1", "닉네임", null, List.of("ROLE_USER"));
			given(memberService.findByUserInfo("user1")).willReturn(userInfo);

			Authentication result = jwtService.getAuthentication(new Cookie[0], request, response);

			assertThat(result.getPrincipal()).isSameAs(userInfo);
			verify(memberService, never()).addRefreshToken(any(), any());
		}

		@Test
		@DisplayName("DB의 RefreshToken과 불일치하고 Redis에도 유예 기록이 없으면 null을 반환한다")
		void mismatchedDbToken_noGracePeriod_returnsNull() {
			String refreshToken = "refresh-token";
			request.setCookies(new Cookie("Authorization", "access-token"), new Cookie("RefreshToken", refreshToken));
			given(jwtTokenProvider.validateToken("access-token")).willReturn(TokenStatus.EXPIRED);
			given(jwtTokenProvider.validateToken(refreshToken)).willReturn(TokenStatus.VALID);
			given(jwtTokenProvider.getAutoLoginFromRefreshToken(refreshToken)).willReturn(true);
			given(jwtTokenProvider.extractUserId(refreshToken)).willReturn("user1");
			given(jwtTokenProvider.extractJti(refreshToken)).willReturn("jti-1");
			given(memberService.getRefreshToken("user1")).willReturn("different-hash");
			given(redisTemplate.hasKey("refresh:prev:user1:jti-1")).willReturn(false);

			Authentication result = jwtService.getAuthentication(new Cookie[0], request, response);

			assertThat(result).isNull();
		}

		@Test
		@DisplayName("refreshToken에서 userId를 추출할 수 없으면 재발급을 시도하지 않고 null을 반환한다")
		void refreshTokenWithoutUserId_returnsNull() {
			String refreshToken = "refresh-token";
			request.setCookies(new Cookie("Authorization", "access-token"), new Cookie("RefreshToken", refreshToken));
			given(jwtTokenProvider.validateToken("access-token")).willReturn(TokenStatus.EXPIRED);
			given(jwtTokenProvider.validateToken(refreshToken)).willReturn(TokenStatus.VALID);
			given(jwtTokenProvider.getAutoLoginFromRefreshToken(refreshToken)).willReturn(true);
			given(jwtTokenProvider.extractUserId(refreshToken)).willReturn(null);

			Authentication result = jwtService.getAuthentication(new Cookie[0], request, response);

			assertThat(result).isNull();
			verify(memberService, never()).getRefreshToken(any());
		}

		@Test
		@DisplayName("INVALID 토큰이면 쿠키를 정리하고 null을 반환한다")
		void invalidToken_clearsCookiesAndReturnsNull() {
			request.setCookies(new Cookie("Authorization", "tampered-token"));
			given(jwtTokenProvider.validateToken("tampered-token")).willReturn(TokenStatus.INVALID);

			Authentication result = jwtService.getAuthentication(new Cookie[0], request, response);

			assertThat(result).isNull();
			assertThat(response.getCookie("Authorization").getMaxAge()).isZero();
		}

		@Test
		@DisplayName("내부 처리 중 예외가 발생하면 쿠키를 정리하고 null을 반환한다")
		void internalException_clearsCookiesAndReturnsNull() {
			request.setCookies(new Cookie("Authorization", "access-token"));
			given(jwtTokenProvider.validateToken("access-token")).willThrow(new RuntimeException("boom"));

			Authentication result = jwtService.getAuthentication(new Cookie[0], request, response);

			assertThat(result).isNull();
			assertThat(response.getCookie("Authorization").getMaxAge()).isZero();
		}
	}

	@Nested
	@DisplayName("extractUserId")
	class ExtractUserId {

		@Test
		@DisplayName("토큰이 없으면 쿠키를 정리하고 null을 반환한다")
		void noToken_clearsCookiesAndReturnsNull() {
			String result = jwtService.extractUserId(request, response, new Cookie[0]);

			assertThat(result).isNull();
			assertThat(response.getCookie("Authorization").getMaxAge()).isZero();
		}

		@Test
		@DisplayName("토큰이 유효하면 userId를 반환한다")
		void validToken_returnsUserId() {
			request.setCookies(new Cookie("Authorization", "access-token"));
			given(jwtTokenProvider.validateToken("access-token")).willReturn(TokenStatus.VALID);
			given(jwtTokenProvider.extractUserId("access-token")).willReturn("user1");

			String result = jwtService.extractUserId(request, response, new Cookie[0]);

			assertThat(result).isEqualTo("user1");
			assertThat(response.getCookie("Authorization")).isNull();
		}
	}

	@Nested
	@DisplayName("extractLoginType")
	class ExtractLoginType {

		@Test
		@DisplayName("토큰이 유효하지 않으면 쿠키를 정리하고 null을 반환한다")
		void invalidToken_clearsCookiesAndReturnsNull() {
			request.setCookies(new Cookie("Authorization", "bad-token"));
			given(jwtTokenProvider.validateToken("bad-token")).willReturn(TokenStatus.INVALID);

			String result = jwtService.extractLoginType(request, response, new Cookie[0]);

			assertThat(result).isNull();
			assertThat(response.getCookie("Authorization").getMaxAge()).isZero();
		}

		@Test
		@DisplayName("토큰이 유효하면 loginType을 반환한다")
		void validToken_returnsLoginType() {
			request.setCookies(new Cookie("Authorization", "access-token"));
			given(jwtTokenProvider.validateToken("access-token")).willReturn(TokenStatus.VALID);
			given(jwtTokenProvider.extractLoginType("access-token")).willReturn("kakao");

			String result = jwtService.extractLoginType(request, response, new Cookie[0]);

			assertThat(result).isEqualTo("kakao");
		}
	}

	@Nested
	@DisplayName("extractUserRole")
	class ExtractUserRole {

		@Test
		@DisplayName("accessToken이 null이면 쿠키를 정리하고 null을 반환한다")
		void nullAccessToken_clearsCookiesAndReturnsNull() {
			List<String> result = jwtService.extractUserRole(request, response, null);

			assertThat(result).isNull();
			assertThat(response.getCookie("Authorization").getMaxAge()).isZero();
		}

		@Test
		@DisplayName("accessToken이 유효하면 권한 목록을 반환한다")
		void validAccessToken_returnsRoles() {
			given(jwtTokenProvider.validateToken("access-token")).willReturn(TokenStatus.VALID);
			given(jwtTokenProvider.extractUserRole("access-token")).willReturn(List.of("ROLE_USER", "ROLE_ADMIN"));

			List<String> result = jwtService.extractUserRole(request, response, "access-token");

			assertThat(result).containsExactly("ROLE_USER", "ROLE_ADMIN");
		}
	}

	@Nested
	@DisplayName("extractAutoLogin")
	class ExtractAutoLogin {

		@Test
		@DisplayName("RefreshToken이 유효하지 않으면 쿠키를 정리하고 null을 반환한다")
		void invalidRefreshToken_clearsCookiesAndReturnsNull() {
			request.setCookies(new Cookie("RefreshToken", "bad-refresh"));
			given(jwtTokenProvider.validateToken("bad-refresh")).willReturn(TokenStatus.EXPIRED);

			Boolean result = jwtService.extractAutoLogin(request, response, new Cookie[0]);

			assertThat(result).isNull();
			assertThat(response.getCookie("RefreshToken").getMaxAge()).isZero();
		}

		@Test
		@DisplayName("RefreshToken이 유효하면 autoLogin 여부를 반환한다")
		void validRefreshToken_returnsAutoLogin() {
			request.setCookies(new Cookie("RefreshToken", "refresh-token"));
			given(jwtTokenProvider.validateToken("refresh-token")).willReturn(TokenStatus.VALID);
			given(jwtTokenProvider.extractAutoLogin("refresh-token")).willReturn(true);

			Boolean result = jwtService.extractAutoLogin(request, response, new Cookie[0]);

			assertThat(result).isTrue();
		}
	}

	@Nested
	@DisplayName("generateTokenFromAuthentication / validateToken")
	class Delegates {

		@Test
		@DisplayName("generateTokenFromAuthentication은 JwtTokenProvider에 그대로 위임한다")
		void generateTokenFromAuthentication_delegatesToProvider() {
			Authentication auth = org.mockito.Mockito.mock(Authentication.class);
			TokenInfo expected = TokenInfo.builder().accessToken("a").refreshToken("r").build();
			given(jwtTokenProvider.generateToken(auth, true, "local")).willReturn(expected);

			TokenInfo result = jwtService.generateTokenFromAuthentication(auth, true, "local");

			assertThat(result).isSameAs(expected);
		}

		@Test
		@DisplayName("validateToken은 JwtTokenProvider에 그대로 위임한다")
		void validateToken_delegatesToProvider() {
			given(jwtTokenProvider.validateToken("token")).willReturn(TokenStatus.VALID);

			TokenStatus result = jwtService.validateToken("token");

			assertThat(result).isEqualTo(TokenStatus.VALID);
		}
	}
}
