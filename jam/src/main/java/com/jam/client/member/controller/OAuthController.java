package com.jam.client.member.controller;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jam.client.member.service.MemberService;
import com.jam.global.jwt.JwtService;
import com.jam.global.jwt.TokenInfo;

import io.jsonwebtoken.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;

@Controller
@RequestMapping("/oauth")
@RequiredArgsConstructor
@Log4j
public class OAuthController {
	
	//TODO: 카카오/네이버 access token 발급 메서드 통합 예정
	//TODO: getUserInfo()도 공통화하여 provider 기반으로 처리하도록 개선

	private final MemberService memberService;
	private final JwtService jwtService;
	
	@Value("${oauth.kakao.clientId}")
    private String kakao_clientId;
	
	@Value("${oauth.naver.clientId}")
    private String naver_clientId;
	
	@Value("${oauth.naver.naver_client_secret}")
	private String naver_client_secret;
	
	/**
	 * 카카오 OAuth 로그인 요청을 시작하는 메서드.
	 * - 카카오 인증 페이지로 리다이렉트시켜 사용자가 로그인하게 함
	 * - CSRF 방어를 위해 state 생성 후 세션에 저장
	 * - 로그인 완료 후 카카오에서 redirect_uri로 code, state 전달됨
	 * @throws java.io.IOException 
	 */
	@GetMapping("/kakao")
	public void  redirectToKakaoAuth(HttpServletResponse response, HttpSession session) throws java.io.IOException {
	    
		try {
			String clientId = kakao_clientId;
	        String redirectUri = "http://localhost:8080/oauth/kakao/callback";
	        String state = URLEncoder.encode(UUID.randomUUID().toString(), StandardCharsets.UTF_8.toString());

	        // CSRF 방어용 state 저장
	        session.setAttribute("kakao_oauth_state", state);
	        
	        String requestUrl = UriComponentsBuilder
	            .fromHttpUrl("https://kauth.kakao.com/oauth/authorize")
	            .queryParam("response_type", "code")
	            .queryParam("client_id", clientId)
	            .queryParam("redirect_uri", redirectUri)
	            .queryParam("state", state)
	            .build()
	            .toUriString();

	        response.sendRedirect(requestUrl);
	        
		}catch(Exception e) {
			log.error("카카오 로그인 리다이렉트 실패" + e.getMessage());
			response.sendRedirect("/member/login?error=oauth");
			try {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "카카오 로그인 실패");
			} catch (IOException ioException) {
				log.error("오류 응답 실패", ioException);
			}
		}
	}
	

	/***********************************
	 * 카카오 콜백
	 * 1. state 검증
	 * 2. access token 발급 및 쿠키 저장
	 * 3. 사용자 정보 조회 및 회원 처리
	 * 4. JWT 쿠키 저장 및 세션 로그인 표시
	 * 5. 로그인 이전 페이지로 리다이렉트
	 * @param code 인가코드
	 * @param state CSRF 공격을 방지하기 위해 애플리케이션에서 생성한 상태 토큰값
	 * @return 메인 페이지 or 로그인 이전 페이지
	 ************************************/
	@GetMapping(value = "/kakao/callback")
	public String handleKakaoCallback(
			@RequestParam("code") String code,
			@RequestParam("state") String state,
			HttpSession session,
			HttpServletRequest request, 
			HttpServletResponse response) {
		
		// 1. state 검증
		String expectedState = (String)session.getAttribute("kakao_oauth_state");
		
		if (!state.equals(expectedState)) {
		    return "/member/login?error=invalid_state";
		}

		session.removeAttribute("kakao_oauth_state");
		
		String accessToken = getKakaoAccessToken(code);
		
		// 2. access token 발급 및 쿠키 저장
		Cookie kakaoAccessTokenCookie = new Cookie("kakaoAccessToken", accessToken);
		kakaoAccessTokenCookie.setHttpOnly(true);   
		kakaoAccessTokenCookie.setPath("/");       
		kakaoAccessTokenCookie.setMaxAge(3 * 60 * 60);

		response.addCookie(kakaoAccessTokenCookie);
		
		// 3. 사용자 정보 조회 및 회원 처리
		Map<String, Object> userInfo = getKakaoUserInfo(accessToken);
		
		if(userInfo.isEmpty()) return "/member/login?error=oauth";

		memberService.socialLoginOrRegister(userInfo, "kakao");
		
		// 4. 서비스 로그인
		Authentication authentication = memberService.authenticateSocialUser((String) userInfo.get("user_id"), (String) userInfo.get("user_name"));

		TokenInfo token = jwtService.generateTokenFromAuthentication(authentication, false, "kakao");
		
		// RefreshToken DB에 저장
		memberService.addRefreshToken((String)userInfo.get("user_id"), token.getRefreshToken());
		
		// 5. JWT 쿠키, 세션 저장
		setCookies(response, token.getAccessToken(), token.getRefreshToken());
		
		String userId = (String)userInfo.get("user_id");
		
		request.getSession().setAttribute("userId", userId);
		request.getSession().setAttribute("userName", memberService.getUserName(userId));
		request.getSession().setMaxInactiveInterval(3 * 60 * 60);
		
		// 6. 로그인 이전 페이지로 리다이렉트
		String prevPage = (String) request.getSession().getAttribute("prevPage");
		
		if (prevPage != null && !prevPage.isBlank()) {
		    URI uri = URI.create(prevPage);
		    return "redirect:" + uri.toString();
		}
		
		return "/";
	}
	
	/**
	 * 카카오 AccessToken 가져옴
	 * @param code
	 * @return accessToken
	 */
	private String getKakaoAccessToken(String code){
	    try {
	    	String requestUrl = "https://kauth.kakao.com/oauth/token";
	        
	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

	        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
	        params.add("grant_type", "authorization_code");
	        params.add("client_id", kakao_clientId);
	        params.add("redirect_uri", "http://localhost:8080/oauth/kakao/callback");
	        params.add("code", code);

	        HttpEntity<MultiValueMap<String, String>> httpRequest = new HttpEntity<>(params, headers);

	        // Post 요청 전송
	        RestTemplate restTemplate = new RestTemplate();
	        ResponseEntity<String> tokenResponse = restTemplate.postForEntity(requestUrl, httpRequest, String.class);

	        // accessToken 파싱
	        ObjectMapper mapper = new ObjectMapper();
	        JsonNode node = mapper.readTree(tokenResponse.getBody());
		        
	        return node.get("access_token").asText();
	    }catch(Exception e) {
	    	log.error(e.getMessage());
	    	return null;
	    }
	}
	
	/**
	 * accessToken으로 카카오 사용자 정보 가져옴
	 * @param accessToken
	 * @return 사용자 정보
	 */
	private Map<String, Object> getKakaoUserInfo(String accessToken) {
		
		try {
			String requestUrl = "https://kapi.kakao.com/v2/user/me";

		    HttpHeaders headers = new HttpHeaders();
		    headers.set("Authorization", "Bearer " + accessToken);

		    HttpEntity<Void> request = new HttpEntity<>(headers);
		    RestTemplate restTemplate = new RestTemplate();
		    ResponseEntity<String> response = restTemplate.exchange(
		            requestUrl,
		            HttpMethod.POST,
		            request,
		            String.class
		    );
		    
		    ObjectMapper mapper = new ObjectMapper();
		    JsonNode root = mapper.readTree(response.getBody());
		    
		    Map<String, Object> userInfo = new HashMap<>();
		    
		    userInfo.put("user_id", "kakao_" + root.get("id").asText());
		    userInfo.put("user_name", root.get("kakao_account").get("profile").get("nickname").asText());
		    
		    JsonNode emailNode = root.get("kakao_account").get("email");
		    userInfo.put("email", emailNode != null ? emailNode.asText() : null);
		    
		    return userInfo;
		    
		}catch(Exception e) {
			log.error("Kakao 사용자 정보 요청 실패!");
		    log.error("accessToken: {}"+ accessToken == null ? "null" : accessToken);
		    log.error("예외 메시지: {}"+ e.getMessage(), e);
		    
		    return new HashMap<>();
		}
	}
	
	/**
	 * 카카오 소셜 로그인 사용자의 로그아웃을 처리합니다.
	 * 
	 * 1. 사용자 인증 정보 확인 (userId)
	 * 2. 서비스 refreshToken 삭제 및 세션 무효화
	 * 3. kakaoAccessToken 쿠키에서 추출
	 * 4. 카카오 로그아웃 요청 (kakaoAccessToken이 있을 경우에만)
	 * 5. kakaoAccessToken 쿠키 삭제
	 * 
	 * @param request  HTTP 요청 객체
	 * @param response HTTP 응답 객체
	 * @return 200 OK - 로그아웃 성공<br>
	 *         401 Unauthorized - userId가 없을 경우
	 *         500 Internal Server Error - 카카오 로그아웃 실패 시
	 */
	@PostMapping("/kakao/logout")
	public ResponseEntity<Void> kakaoLogout(HttpServletRequest request, HttpServletResponse response) throws java.io.IOException{
		
		// 1. 서비스 로그아웃 
        String userId = (String) request.getAttribute("userId");
    	
		if (userId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		
		memberService.deleteRefreshToken(userId);
		
		// 모든 세션 만료
		request.getSession().invalidate();
		
		// 2. kakaoAccessToken 추출
		Cookie[] cookies = request.getCookies();
		String kakaoAccessToken = null;
		
		if (cookies != null) {
		    for (Cookie cookie : cookies) {
		        if ("kakaoAccessToken".equals(cookie.getName())) {
		            kakaoAccessToken = cookie.getValue();
		            break;
		        }
		    }
		}
		
		// 3. 카카오 로그아웃
		if(kakaoAccessToken != null) {
			try {
		        String logoutUrl = "https://kapi.kakao.com/v1/user/logout";

		        HttpHeaders headers = new HttpHeaders();
		        headers.set("Authorization", "Bearer " + kakaoAccessToken);

		        HttpEntity<Void> httpEntity = new HttpEntity<>(headers);

		        RestTemplate restTemplate = new RestTemplate();
		        ResponseEntity<String> res = restTemplate.postForEntity(logoutUrl, httpEntity, String.class);

		        if (!res.getStatusCode().is2xxSuccessful()) {
		            log.warn("카카오 로그아웃 실패 - 응답 코드: " + res.getStatusCodeValue());
		        }
		        
		        
		    } catch (Exception e) {
		        log.error("카카오 로그아웃 실패: " + e.getMessage());
		        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		    }
		}
        deleteCookies(response, "kakaoAccessToken");
        
		return ResponseEntity.ok().build();
	}
	
	/********
	 * 네이버 OAuth 로그인 요청을 시작하는 메서드.
	 * - 네이버 인증 페이지로 리다이렉트시켜 사용자가 로그인하게 함
	 * - CSRF 방어를 위해 state 생성 후 세션에 저장
	 * - 로그인 완료 후 네이버에서 redirect_uri로 code, state 전달됨
	 */
	@GetMapping("/naver")
	public void redirectToNaverAuth(HttpServletResponse response, HttpSession session) throws java.io.IOException {
        try {
			String clientId = naver_clientId;
			String redirectUri = "http://localhost:8080/oauth/naver/callback";
			String state = URLEncoder.encode(UUID.randomUUID().toString(), StandardCharsets.UTF_8.toString());

	        // CSRF 방어용 state 저장
	        session.setAttribute("naver_oauth_state", state);

	        String requestUrl = UriComponentsBuilder
	            .fromHttpUrl("https://nid.naver.com/oauth2.0/authorize")
	            .queryParam("response_type", "code")
	            .queryParam("client_id", clientId)
	            .queryParam("redirect_uri", redirectUri)
	            .queryParam("state", state)
	            .build()
	            .toUriString();

	        response.sendRedirect(requestUrl);
	        
		}catch(Exception e) {
			log.error("네이버 로그인 리다이렉트 실패", e);
			response.sendRedirect("/member/login?error=oauth");
			try {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "네이버 로그인 실패");
			} catch (IOException ioException) {
				log.error("오류 응답 실패", ioException);
			}
		}
   	}
	
	
	/**
	 * 네이버 콜백	 
	 * 1. state 검증
	 * 2. access token 발급 및 쿠키 저장
	 * 3. 사용자 정보 조회 및 회원 처리
	 * 4. JWT 쿠키 저장 및 세션 로그인 표시
	 * 5. 로그인 이전 페이지로 리다이렉트
	 * @param code 인가코드
	 * @param state CSRF 공격을 방지하기 위해 애플리케이션에서 생성한 상태 토큰값
	 * @return 메인 페이지 or 로그인 이전 페이지
	 */
	@GetMapping(value = "/naver/callback")
	public String handleNaverCallback(
			@RequestParam("code") String code,
			@RequestParam("state") String state,
			HttpSession session,
			HttpServletRequest request, 
			HttpServletResponse response) {
		
		// 1. state 검증
		String expectedState = (String)session.getAttribute("naver_oauth_state");
		
		if (!state.equals(expectedState)) {
		    return "/member/login?error=invalid_state";
		}

		session.removeAttribute("naver_oauth_state");
		
		String accessToken = getNaverAccessToken(code, state);
		
		// 2. access token 발급 및 쿠키 저장 (사용자 정보 조회 및 로그아웃 등에 사용) 
		Cookie naverAccessTokenCookie = new Cookie("naverAccessToken", accessToken);
		naverAccessTokenCookie.setHttpOnly(true);   
		naverAccessTokenCookie.setPath("/");       
		naverAccessTokenCookie.setMaxAge(3 * 60 * 60);

		response.addCookie(naverAccessTokenCookie);
		
		// 3. 사용자 정보 조회 및 회원 처리
		Map<String, Object> userInfo = getNaverUserInfo(accessToken);
		
		if(userInfo.isEmpty()) return "/member/login?error=oauth";
		
		// 사용자 정보가 DB에 없으면 회원가입
		memberService.socialLoginOrRegister(userInfo, "naver");
		
		// 4.서비스 로그인
		
		Authentication authentication = memberService.authenticateSocialUser((String) userInfo.get("user_id"), (String) userInfo.get("user_name"));

		TokenInfo token = jwtService.generateTokenFromAuthentication(authentication, false, "naver");
		
		// 5. JWT 쿠키, 세션 저장
		setCookies(response, token.getAccessToken(), token.getRefreshToken());
		
		String userId = (String)userInfo.get("user_id");
		
		request.getSession().setAttribute("userId", userId);
		request.getSession().setAttribute("userName", memberService.getUserName(userId));
		request.getSession().setMaxInactiveInterval(3 * 60 * 60);
		
		// 6. 로그인 이전 페이지로 리다이렉트
		String prevPage = (String) request.getSession().getAttribute("prevPage");
		
		if (prevPage != null && !prevPage.isBlank()) {
		    URI uri = URI.create(prevPage);
		    return "redirect:" + uri.toString();
		}
		return "/";
	}
	
	
	private Map<String, Object> getNaverUserInfo(String accessToken) {
		try {
			String requestUrl = "https://openapi.naver.com/v1/nid/me";

		    HttpHeaders headers = new HttpHeaders();
		    headers.set("Authorization", "Bearer " + accessToken);

		    HttpEntity<Void> request = new HttpEntity<>(headers);
		    RestTemplate restTemplate = new RestTemplate();
		    ResponseEntity<String> response = restTemplate.exchange(
		            requestUrl,
		            HttpMethod.GET,
		            request,
		            String.class
		    );
		    
		    ObjectMapper mapper = new ObjectMapper();
		    JsonNode root = mapper.readTree(response.getBody());
		    
		    String resultCode = root.get("resultcode").asText();
		    
		    if (!"00".equals(resultCode)) {
		        throw new IllegalStateException("네이버 사용자 정보 요청 실패: " + root.toString());
		    }

		    JsonNode data = root.get("response");
		    if (data == null) {
		        throw new IllegalStateException("네이버 응답에 사용자 정보가 없음");
		    }

		    Map<String, Object> userInfo = new HashMap<>();
		    userInfo.put("user_id", "naver_" + data.get("id").asText());
		    userInfo.put("user_name", data.get("name").asText());
		    userInfo.put("email", data.has("email") ? data.get("email").asText() : null);
		    
		    return userInfo;
		    
		}catch(Exception e) {
			log.error("Naver 사용자 정보 요청 실패!");
		    log.error("accessToken: {}"+ accessToken == null ? "null" : accessToken);
		    log.error("예외 메시지: {}"+ e.getMessage(), e);
		    return new HashMap<>();
		}
	}


	private String getNaverAccessToken(String code, String state) {
		try {
	    	String requestUrl = "https://nid.naver.com/oauth2.0/token";
	        
	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

	        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
	        params.add("grant_type", "authorization_code");
	        params.add("client_id", naver_clientId);
	        params.add("client_secret", naver_client_secret);
	        
	        params.add("redirect_uri", "http://localhost:8080/oauth/naver/callback");

	        params.add("code", code);
	        params.add("state", state);
	        
	        HttpEntity<MultiValueMap<String, String>> httpRequest = new HttpEntity<>(params, headers);

	        // Post 요청 전송
	        RestTemplate restTemplate = new RestTemplate();
	        ResponseEntity<String> tokenResponse = restTemplate.postForEntity(requestUrl, httpRequest, String.class);

	        // accessToken 파싱
	        ObjectMapper mapper = new ObjectMapper();
	        JsonNode node = mapper.readTree(tokenResponse.getBody());
		        
	        return node.get("access_token").asText();
	    }catch(Exception e) {
	    	log.error(e.getMessage());
	    	return null;
	    }
	}
	

	/**
	 * 네이버 소셜 로그인 사용자의 로그아웃을 처리합니다.
	 * 
	 * 1. 사용자 인증 정보 확인 (userId)
	 * 2. 서비스 refreshToken 삭제
	 * 3. naverAccessToken 쿠키 삭제
	 * 4. 세션 무효화
	 * 
	 * 네이버는 별도의 서버 로그아웃 API를 제공하지 않으므로,
	 * 클라이언트 측 토큰 삭제와 세션 종료만 수행합니다.
	 *
	 * @param request  HTTP 요청 객체
	 * @param response HTTP 응답 객체
	 * @return 200 OK - 로그아웃 성공
	 *         500 INTERNAL_SERVER_ERROR - userId 없거나 처리 중 오류 발생
	 */
	@PostMapping("/naver/logout")
	public ResponseEntity<Void> naverLogout(HttpServletRequest request, HttpServletResponse response) throws java.io.IOException{
		try {
	        // 서비스 로그아웃 
	        String userId = (String) request.getAttribute("userId");
	    	
			if (userId == null) {
			    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
			}
			
			memberService.deleteRefreshToken(userId);
			deleteCookies(response, "naverAccessToken");
			
			// 모든 세션 만료
			request.getSession().invalidate();

			return ResponseEntity.ok().build();
	        
	    } catch (Exception e) {
	        log.error("네이버 로그아웃 실패: " + e.getMessage());
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
	    }
	}
	
	private void setCookies(HttpServletResponse response, String accessToken, String refreshToken) {
		
		// 쿠키에 jwt 토큰 저장
		Cookie accessTokenCookie = new Cookie("Authorization", accessToken);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(3 * 60 * 60);
        response.addCookie(accessTokenCookie);

        Cookie refreshTokenCookie = new Cookie("RefreshToken", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(24 * 60 * 60);
        response.addCookie(refreshTokenCookie);
	}
	
	private void deleteCookies(HttpServletResponse response, String tokenKeyName) {
	    String[] cookieNames = { tokenKeyName, "Authorization", "RefreshToken" };

	    for (String name : cookieNames) {
	    	log.info(name);
	        Cookie cookie = new Cookie(name, null);
	        cookie.setHttpOnly(true);
	        cookie.setMaxAge(0);
	        cookie.setPath("/");
	        
	        response.addCookie(cookie);
	    }
	}
	
}