package com.jam.global.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import com.jam.global.jwt.TokenInfo.TokenStatus;
import com.jam.member.dto.MemberDto;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * JwtTokenProvider에 대한 순수 유닛테스트. Spring 컨텍스트 없이 실제 비밀키로 토큰을 발급/검증한다.
 */
class JwtTokenProviderTest {

	private static final String SECRET =
			Base64.getEncoder().encodeToString("test-secret-key-for-jwt-unit-tests-1234567890".getBytes(StandardCharsets.UTF_8));

	private JwtTokenProvider provider;

	@BeforeEach
	void setUp() {
		provider = new JwtTokenProvider(SECRET);
	}

	private Authentication authenticationOf(String userId, String userName, String companyName, List<String> roles) {
		MemberDto member = new MemberDto();
		member.setUser_id(userId);
		member.setUser_name(userName);
		member.setCompany_name(companyName);
		member.setRoles(roles);
		return new UsernamePasswordAuthenticationToken(member, null, member.getAuthorities());
	}

	// 프로덕션 키와 동일한 비밀키로, provider의 private key 없이 직접 토큰을 만들어 엣지케이스를 재현하기 위한 헬퍼
	private String rawToken(String subject, Date expiration, java.util.Map<String, Object> claims) {
		byte[] keyBytes = io.jsonwebtoken.io.Decoders.BASE64.decode(SECRET);
		var key = Keys.hmacShaKeyFor(keyBytes);
		var builder = Jwts.builder().subject(subject).expiration(expiration);
		if (claims != null) {
			claims.forEach(builder::claim);
		}
		return builder.signWith(key).compact();
	}

	@Nested
	@DisplayName("generateToken")
	class GenerateToken {

		@Test
		@DisplayName("인증 정보로 발급한 accessToken에 사용자 정보가 담긴다")
		void generateToken_accessTokenContainsUserInfo() {
			Authentication auth = authenticationOf("user1", "닉네임", "회사명", List.of("ROLE_USER"));

			TokenInfo token = provider.generateToken(auth, false, "local");

			Claims claims = provider.getClaims(token.getAccessToken());
			assertThat(claims.getSubject()).isEqualTo("user1");
			assertThat(claims.get("userName")).isEqualTo("닉네임");
			assertThat(claims.get("companyName")).isEqualTo("회사명");
			assertThat(claims.get("loginType")).isEqualTo("local");
			assertThat((List<String>) claims.get("auth")).containsExactly("ROLE_USER");
			assertThat(token.getRefreshToken()).isNotBlank();
		}

		@Test
		@DisplayName("권한이 없으면 ROLE_USER를 기본값으로 사용한다")
		void generateToken_noAuthorities_defaultsToRoleUser() {
			MemberDto member = new MemberDto();
			member.setUser_id("user1");
			// roles를 세팅하지 않으면 MemberDto.getAuthorities()가 이미 ROLE_USER를 반환하므로,
			// Authentication 자체에서 권한이 비어있는 극단 케이스를 별도 Authentication 구현으로 재현한다.
			Authentication emptyAuth = new UsernamePasswordAuthenticationToken(member, null, List.of());

			TokenInfo token = provider.generateToken(emptyAuth, false, "local");

			Claims claims = provider.getClaims(token.getAccessToken());
			assertThat((List<String>) claims.get("auth")).containsExactly("ROLE_USER");
		}
	}

	@Nested
	@DisplayName("generateRefreshToken")
	class GenerateRefreshToken {

		@Test
		@DisplayName("자동 로그인이면 일반 로그인보다 만료 시간이 훨씬 길다")
		void generateRefreshToken_autoLogin_longerExpiry() {
			String shortLived = provider.generateRefreshToken("user1", false, "local");
			String longLived = provider.generateRefreshToken("user1", true, "local");

			Date shortExp = provider.getClaims(shortLived).getExpiration();
			Date longExp = provider.getClaims(longLived).getExpiration();

			assertThat(longExp).isAfter(shortExp);
		}

		@Test
		@DisplayName("고유한 jti가 발급된다")
		void generateRefreshToken_uniqueJti() {
			String token1 = provider.generateRefreshToken("user1", false, "local");
			String token2 = provider.generateRefreshToken("user1", false, "local");

			assertThat(provider.extractJti(token1)).isNotEqualTo(provider.extractJti(token2));
		}
	}

	@Nested
	@DisplayName("validateToken")
	class ValidateToken {

		@Test
		@DisplayName("null이면 EMPTY")
		void nullToken_empty() {
			assertThat(provider.validateToken(null)).isEqualTo(TokenStatus.EMPTY);
		}

		@Test
		@DisplayName("빈 문자열이면 EMPTY")
		void blankToken_empty() {
			assertThat(provider.validateToken("   ")).isEqualTo(TokenStatus.EMPTY);
		}

		@Test
		@DisplayName("정상 발급한 토큰이면 VALID")
		void validToken_valid() {
			String token = provider.generateRefreshToken("user1", false, "local");

			assertThat(provider.validateToken(token)).isEqualTo(TokenStatus.VALID);
		}

		@Test
		@DisplayName("만료된 토큰이면 EXPIRED")
		void expiredToken_expired() {
			String token = rawToken("user1", new Date(System.currentTimeMillis() - 1000), null);

			assertThat(provider.validateToken(token)).isEqualTo(TokenStatus.EXPIRED);
		}

		@Test
		@DisplayName("다른 키로 서명된 토큰이면 INVALID")
		void wrongSignature_invalid() {
			String otherSecret = Base64.getEncoder().encodeToString("a-completely-different-secret-key-value".getBytes(StandardCharsets.UTF_8));
			byte[] keyBytes = io.jsonwebtoken.io.Decoders.BASE64.decode(otherSecret);
			var otherKey = Keys.hmacShaKeyFor(keyBytes);
			String token = Jwts.builder().subject("user1")
					.expiration(new Date(System.currentTimeMillis() + 60000))
					.signWith(otherKey).compact();

			assertThat(provider.validateToken(token)).isEqualTo(TokenStatus.INVALID);
		}

		@Test
		@DisplayName("형식이 깨진 문자열이면 INVALID")
		void malformedToken_invalid() {
			assertThat(provider.validateToken("not-a-real-jwt-token")).isEqualTo(TokenStatus.INVALID);
		}
	}

	@Nested
	@DisplayName("getAuthentication")
	class GetAuthenticationTest {

		@Test
		@DisplayName("유효한 토큰이면 사용자 정보와 권한이 채워진 Authentication을 반환한다")
		void validToken_returnsAuthentication() {
			Authentication auth = authenticationOf("user1", "닉네임", null, List.of("ROLE_USER", "ROLE_COMPANY"));
			TokenInfo token = provider.generateToken(auth, false, "local");

			Authentication result = provider.getAuthentication(token.getAccessToken());

			MemberDto principal = (MemberDto) result.getPrincipal();
			assertThat(principal.getUser_id()).isEqualTo("user1");
			assertThat(principal.getUser_name()).isEqualTo("닉네임");
			assertThat(result.getAuthorities()).extracting(Object::toString)
					.containsExactlyInAnyOrder("ROLE_USER", "ROLE_COMPANY");
		}

		@Test
		@DisplayName("유효하지 않은 토큰이면 null을 반환한다")
		void invalidToken_returnsNull() {
			assertThat(provider.getAuthentication("garbage")).isNull();
		}

		@Test
		@DisplayName("auth 클레임이 없는 토큰이면 예외를 던진다")
		void noAuthClaim_throws() {
			String token = rawToken("user1", new Date(System.currentTimeMillis() + 60000), null);

			assertThatThrownBy(() -> provider.getAuthentication(token))
					.isInstanceOf(RuntimeException.class)
					.hasMessage("권한 정보가 없는 토큰입니다.");
		}
	}

	@Nested
	@DisplayName("getClaims")
	class GetClaimsTest {

		@Test
		@DisplayName("유효하지 않은 토큰이면 예외를 던지지 않고 null을 반환한다")
		void invalidToken_returnsNullWithoutThrowing() {
			assertThat(provider.getClaims("garbage")).isNull();
		}

		@Test
		@DisplayName("만료된 토큰이어도 예외를 던지지 않고 null을 반환한다")
		void expiredToken_returnsNull() {
			String token = rawToken("user1", new Date(System.currentTimeMillis() - 1000), null);

			assertThat(provider.getClaims(token)).isNull();
		}
	}

	@Nested
	@DisplayName("extractUserId - 다른 extract 계열과 다르게 예외를 그대로 전파한다")
	class ExtractUserId {

		@Test
		@DisplayName("유효한 토큰이면 subject를 반환한다")
		void validToken_returnsSubject() {
			String token = provider.generateRefreshToken("user1", false, "local");

			assertThat(provider.extractUserId(token)).isEqualTo("user1");
		}

		@Test
		@DisplayName("만료된 토큰이면 (getClaims와 달리) 예외를 그대로 던진다")
		void expiredToken_throwsUnlikeGetClaims() {
			String token = rawToken("user1", new Date(System.currentTimeMillis() - 1000), null);

			assertThatThrownBy(() -> provider.extractUserId(token))
					.isInstanceOf(io.jsonwebtoken.ExpiredJwtException.class);
		}
	}

	@Nested
	@DisplayName("extract 계열 (loginType/autoLogin/role/jti)")
	class ExtractOthers {

		@Test
		@DisplayName("extractLoginType - accessToken에서 loginType을 추출한다")
		void extractLoginType() {
			Authentication auth = authenticationOf("user1", "닉네임", null, List.of("ROLE_USER"));
			TokenInfo token = provider.generateToken(auth, false, "kakao");

			assertThat(provider.extractLoginType(token.getAccessToken())).isEqualTo("kakao");
		}

		@Test
		@DisplayName("getAutoLoginFromRefreshToken - refreshToken에서 autoLogin 여부를 추출한다")
		void getAutoLoginFromRefreshToken() {
			String token = provider.generateRefreshToken("user1", true, "local");

			assertThat(provider.getAutoLoginFromRefreshToken(token)).isTrue();
		}

		@Test
		@DisplayName("extractUserRole - accessToken에서 권한 목록을 추출한다")
		void extractUserRole() {
			Authentication auth = authenticationOf("user1", "닉네임", null, List.of("ROLE_USER", "ROLE_ADMIN"));
			TokenInfo token = provider.generateToken(auth, false, "local");

			assertThat(provider.extractUserRole(token.getAccessToken())).containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
		}

		@Test
		@DisplayName("extractJti - refreshToken에서 jti를 추출한다")
		void extractJti() {
			String token = provider.generateRefreshToken("user1", false, "local");

			assertThat(provider.extractJti(token)).isNotBlank();
		}

		@Test
		@DisplayName("유효하지 않은 토큰으로 호출하면 NPE가 발생한다 (getClaims가 null을 반환하고 그 위에서 바로 .get()을 호출하기 때문) "
				+ "- 현재 모든 실제 호출부는 validateToken으로 먼저 걸러내므로 실제로 도달하진 않지만, 직접 호출 시의 동작을 문서화해둔다")
		void invalidToken_throwsNpe() {
			assertThatThrownBy(() -> provider.extractLoginType("garbage"))
					.isInstanceOf(NullPointerException.class);
		}
	}

	@Nested
	@DisplayName("generateAuthFlagToken")
	class GenerateAuthFlagToken {

		@Test
		@DisplayName("purpose 클레임과 함께 유효한 토큰을 생성한다")
		void generatesValidTokenWithPurpose() {
			String token = provider.generateAuthFlagToken("user1", "isAuthVerified");

			assertThat(provider.validateToken(token)).isEqualTo(TokenStatus.VALID);
			Claims claims = provider.getClaims(token);
			assertThat(claims.getSubject()).isEqualTo("user1");
			assertThat(claims.get("purpose")).isEqualTo("isAuthVerified");
		}
	}
}
