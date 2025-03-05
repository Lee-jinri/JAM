package com.jam.client.member.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.jam.client.member.service.MemberService;
import com.jam.client.member.vo.MemberVO;
import com.jam.security.JwtTokenProvider;
import com.jam.security.TokenInfo;
import com.jam.security.TokenInfo.TokenStatus;

import io.jsonwebtoken.Claims;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j;

@RestController
@RequestMapping("/api/member")
@AllArgsConstructor
@Log4j
public class MemberRestController {

	@Autowired
	private MemberService memberService;

	private final PasswordEncoder encoder;

    @Autowired
    public MemberRestController(PasswordEncoder encoder) {
        this.encoder = encoder;
    }
    
	@Autowired
	private JwtTokenProvider jwtTokenProvider;

	@Autowired
	private AuthenticationManager authenticationManager;
	

	/**
	 * 회원 가입
	 *
	 * @param member 회원 정보 객체
	 * @return 회원 가입 결과와 HTTP 상태 코드
	 * @throws Exception 회원 가입 처리 중 예외 발생 시
	 */
	@PostMapping(value = "/join", produces = "application/json")
	public ResponseEntity<String> join(@RequestBody MemberVO member , Model model, HttpServletRequest request) throws Exception {

		String user_id = member.getUser_id();
		
		String user_name = member.getUser_name();
		
		// 영문자, 숫자 포함 8~20자
		String idLegExp = "^(?=.*[a-zA-Z])(?=.*\\d)[a-zA-Z\\d]{8,20}$";
		// 영문자, 한글, 숫자 2~11자
		String nameLegExp = "^[a-zA-Z가-힣0-9]{2,11}$";
		
		if (!user_id.matches(idLegExp)) {
		    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("INVALID_USER_ID");
		}

		if (!user_name.matches(nameLegExp)) {
		    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("INVALID_USERNAME");
		}

		String rawPw = member.getUser_pw(); // 인코딩 전 비밀번호
		String encodePw = encoder.encode(rawPw); // 비밀번호 인코딩
		
		member.setUser_pw(encodePw);

		try {
			memberService.join(member);
			
			String prevPage = (String) request.getSession().getAttribute("prevPage");

			return ResponseEntity.ok().header("prev-page", prevPage).body(null);
			
		}catch(Exception e) {
			
			return ResponseEntity.status(500).body("Internal Server Error: " + e.getMessage());
		}

	}
	
	/**
	 * 사용자의 로그인을 처리합니다.
	 * 
	 * @param member 사용자가 입력한 아이디와 비밀번호를 포함한 객체
	 * @return HTTP 응답 상태 코드와 이전 페이지 URI
	 */
	@PostMapping(value = "/login-process", produces = MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8")
	@ResponseBody
	public ResponseEntity<String> login(@RequestBody Map<String, Object> member, HttpServletRequest request, HttpServletResponse response) {
		try {
			String user_id = (String)member.get("user_id");
			String user_pw = (String)member.get("user_pw");
			
			Boolean autoLogin = (Boolean) member.get("autoLogin");

			// 사용자 이름과 비밀번호로 인증 객체 생성
			Authentication authentication = authenticationManager
					.authenticate(new UsernamePasswordAuthenticationToken(user_id, user_pw));

			// SecurityContextHolder에 현재 사용자의 정보를 설정합니다.
			SecurityContextHolder.getContext().setAuthentication(authentication);
	    
			String user_name = memberService.getUserName(user_id);
			
			// JWT 토큰 생성
			TokenInfo token = jwtTokenProvider.generateToken(authentication, user_name, autoLogin);

			String refreshToken = token.getRefreshToken();

			try {
				// refresh 토큰 저장
				int addRefreshToken = memberService.addRefreshToken(user_id, refreshToken);
				if (addRefreshToken != 1) {
					return ResponseEntity.status(500).body("Internal Server Error: Refresh Token 저장 중 오류 발생");
				}
			} catch (Exception e) {
				log.error("Failed to add refresh token", e);
				return ResponseEntity.status(500).body("Internal Server Error: " + e.getMessage());
			}
			
			// http 세션에 로그인 여부 저장
			HttpSession session = request.getSession();
			session.setAttribute("isLogin", "true");
			session.setMaxInactiveInterval(3 * 60 * 60); // 30분 동안 비활성 시 세션 만료
			
			// 쿠키에 jwt 토큰 저장
		    Cookie jwtCookie = new Cookie("Authorization", token.getAccessToken());
		    jwtCookie.setHttpOnly(true);  // 자바스크립트에서 접근 불가능
		    jwtCookie.setPath("/");       // 해당 쿠키의 유효 경로 설정
		    jwtCookie.setMaxAge(24 * 60 * 60);	  // 세션 쿠키로 설정 (브라우저 끄면 쿠키 삭제됨)

		    Cookie refreshTokenCookie  = new Cookie("RefreshToken", token.getRefreshToken());
		    refreshTokenCookie .setHttpOnly(true);
		    refreshTokenCookie.setPath("/");
		    
		    int maxAge = autoLogin? 365 * 24 * 60 * 60 : 24 * 60 * 60;
		    refreshTokenCookie.setMaxAge(maxAge);
		    
		    // 쿠키를 응답에 추가
		    response.addCookie(jwtCookie);
		    response.addCookie(refreshTokenCookie);

			// 로그인 이전 페이지
			String prevPage = (String) request.getSession().getAttribute("prevPage");

			return ResponseEntity.ok().header("prev-page", prevPage).body("success");
			
		} catch (AuthenticationException e) {
			//유효하지 않은 로그인 정보
			log.error("Authentication failed: " + e.getMessage(), e);
			return ResponseEntity.status(401).body("Authentication failed: " + e.getMessage());
		} catch (Exception e) {
			log.error("An error occurred during login", e);
			return ResponseEntity.status(500).body("Internal Server Error: " + e.getMessage());
		}
	}
	
	/**
	 * 사용자의 로그아웃을 처리합니다.
	 * 
	 * @return HTTP 응답 상태 코드
	 */
	@GetMapping(value = "/logout")
	public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) throws Exception {

        try {
    		String userId = (String) request.getAttribute("userId");
        	
    		memberService.deleteRefreshToken(userId);
    		
    		deleteCookies(response);
    	    
    	    // http 세션에 저장된 로그인 여부 삭제
    	    request.getSession().removeAttribute("isLogin");
    	    
    		return ResponseEntity.ok().body("Logout success");
    	}catch(Exception e) {
    		return ResponseEntity.status(500).body("Internal Server Error: Refresh Token 삭제 중 오류 발생 : " + e.getMessage());
    	}
	}

	// 쿠키에서 Access Token 추출
	private String extractAccessToken(HttpServletRequest request) {
	    String accessToken = "";
	    Cookie[] cookies = request.getCookies();

	    if (cookies != null) {
	        for (Cookie cookie : cookies) {
	            if (cookie.getName().equals("Authorization")) {
	                accessToken = cookie.getValue();
	            }
	        }
	    }
	    return accessToken;
	}
	
	private void deleteCookies(HttpServletResponse response) {
		// Authorization 쿠키 삭제
	    Cookie cookie = new Cookie("Authorization", null);
	    cookie.setHttpOnly(true);
	    cookie.setSecure(true);
	    cookie.setPath("/");
	    cookie.setMaxAge(0);  // 쿠키 만료 시간 0으로 설정 (즉시 삭제)
	    
	    response.addCookie(cookie);
	    
	    // refreshToken 쿠키 삭제
	    Cookie refreshTokenCookie = new Cookie("RefreshToken", null);
	    refreshTokenCookie.setMaxAge(0);
	    refreshTokenCookie.setPath("/");
	    
	    response.addCookie(refreshTokenCookie);
	}
	
	/**
	 * JWT 토큰 검증 후 사용자의 아이디와 닉네임, 권한 정보를 반환합니다.
	 * 
	 * @return HTTP 응답 상태 코드와 사용자 정보를 포함한 객체
	 */
	@GetMapping(value = "/getUserInfo", produces = MediaType.APPLICATION_JSON_VALUE)
	@CrossOrigin
	public ResponseEntity<MemberVO> getUserInfo(HttpServletResponse response, HttpServletRequest request) {
		try {
			MemberVO member = new MemberVO();
			
			if(response.getStatus() == 200) {
				String userId = (String) request.getAttribute("userId");
				String auth = (String) request.getAttribute("auth");
				String userName = (String) request.getAttribute("userName");
				
				if(userId != null && userName != null && auth != null) {
					
					member.setUser_id(userId);
					member.setRole(auth);
					member.setUser_name(userName);
					
					return ResponseEntity.ok().body(member);
				}
			}else {
				deleteCookies(response);
			}
			
			return ResponseEntity.ok().body(member);
			
		} catch (Exception e) {
			log.error("사용자 정보를 가져올 수 없습니다. : " + e.getMessage());
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	/**
	 * JWT 토큰 검증하지 않고 사용자의 아이디, 닉네임, 권한을 반환합니다.
	 * 
	 * @param request
	 * @return HTTP 응답 상태 코드와 사용자 정보
	 */
	@GetMapping("/decode-token")
    public ResponseEntity<Map<String, String>> decodeToken(HttpServletRequest request, HttpServletResponse res) {
		Cookie[] cookies = request.getCookies();
		
		String token = jwtTokenProvider.getAccessTokenFromCookies(cookies);
		
		if(token == null) return ResponseEntity.ok(Collections.emptyMap());

		Claims claims = jwtTokenProvider.getClaims(token); 
        
		if (claims == null) {
			deleteCookies(res);
			return ResponseEntity.ok(Collections.emptyMap());
		}
		
        Map<String, String> response = new HashMap<>();
        response.put("userId", claims.getSubject());
        response.put("userName", claims.get("userName", String.class));
        response.put("role", claims.get("auth", String.class));

        return ResponseEntity.ok(response);
    }
	
	/**
	 * JWT 토큰을 이용해 사용자의 로그인 여부를 확인합니다.
	 * 
	 * @return 인증 여부를 나타내는 맵과 HTTP 응답 상태 코드
	 */
	@GetMapping("/checkAuthentication")
	public ResponseEntity<Map<String, Boolean>> checkAuthentication(HttpServletRequest request) {
	    
		Map<String, Boolean> response = new HashMap<>();
		
		String accessToken ="";
		
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("Authorization")) {  
                	accessToken = cookie.getValue(); 
                }
            }
        }
        
        TokenStatus tokenStatus = jwtTokenProvider.validateToken(accessToken);
        
		if (tokenStatus == TokenStatus.VALID) {
			// JWT가 유효한 경우 인증된 상태로 처리
            response.put("authenticated", true);
        } else {
            // JWT가 유효하지 않거나 만료된 경우
            response.put("authenticated", false);
        }

        return ResponseEntity.ok(response);
	}


	/**
	 * 사용자가 입력한 아이디의 중복을 확인합니다.
	 * 
	 * @param userId 사용자가 입력한 아이디
	 * @return HTTP 응답 상태 코드와 중복 여부를 나타내는 메시지
	 *         - 200 OK: 아이디 사용 가능
	 *         - 400 BAD REQUEST: 입력된 아이디가 null이거나 형식이 올바르지 않음
	 *         - 409 CONFLICT: 아이디가 이미 사용 중
	 *         - 500 INTERNAL SERVER ERROR: 서버 내부 오류 발생
	 * @throws Exception 서버 처리 중 발생한 예외
	 */
	@GetMapping(value = "/userId/check")
	public ResponseEntity<String> idChk(@RequestParam String userId) throws Exception {
		if(userId == null || userId == "") return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User ID is null.");

		String idLegExp =  "^(?=.*[a-zA-Z])(?=.*\\d)[a-zA-Z\\d]{8,20}$";
		if (!userId.matches(idLegExp)) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("INVALID_USER_ID.");
		
		try {
			int result = memberService.idCheck(userId);

			if(result != 0 )return ResponseEntity.status(HttpStatus.CONFLICT).body("The User ID is already in use.");
			
			return new ResponseEntity<>(HttpStatus.OK);
				
		}catch(Exception e) {
			log.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred." + e.getMessage());
		}
	}

	/**
	 * 사용자가 입력한 닉네임의 중복을 확인합니다.
	 * 
	 * @param 사용자가 입력한 닉네임
	 * @return HTTP 응답 상태 코드와 중복 여부를 나타내는 메시지
	 *         - 200 OK: 닉네임 사용 가능
	 *         - 400 BAD REQUEST: 입력된 닉네임이 null이거나 형식이 올바르지 않음
	 *         - 409 CONFLICT: 닉네임이 이미 사용 중
	 *         - 500 INTERNAL SERVER ERROR: 서버 내부 오류 발생
	 * @throws Exception 서버 처리 중 발생한 예외
	 */
	@GetMapping(value="/userName/check")
	public ResponseEntity<String> nameChk(@RequestParam String userName) throws Exception {

		log.info(userName);
		
		if(userName == null || userName == "") return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User name is null.");
		
		String nameLegExp = "^.{3,10}$";

		if (!userName.matches(nameLegExp)) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("INVALID_USERNAME.");
		
		try {
			int result = memberService.nameCheck(userName);

			if(result != 0 )return ResponseEntity.status(HttpStatus.CONFLICT).body("The User name is already in use.");
			
			return new ResponseEntity<>(HttpStatus.OK);
		}catch(Exception e) {
			log.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred." + e.getMessage());
		}
		
	}
	
	/**
	 * 사용자가 입력한 핸드폰 번호의 중복을 확인합니다.
	 * 
	 * @param 사용자가 입력한 핸드폰 번호
	 * @return HTTP 응답 상태 코드와 중복 여부를 나타내는 메시지
	 *         - 200 OK: 핸드폰 번호 사용 가능
	 *         - 400 BAD REQUEST: 입력된 핸드폰 번호가 null이거나 형식이 올바르지 않음
	 *         - 409 CONFLICT: 핸드폰 번호가 이미 사용 중
	 *         - 500 INTERNAL SERVER ERROR: 서버 내부 오류 발생
	 * @throws Exception 서버 처리 중 발생한 예외
	 */
	@GetMapping(value = "/phone/check")
	public ResponseEntity<String> phoneChk(@RequestParam String phone) throws Exception {

		if(phone == null || phone == "") return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Phone is null.");

		String phoneLegExp = "^01([016789])([0-9]{3,4})([0-9]{4})$";
		if (!phone.matches(phoneLegExp)) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("INVALID_PHONE.");
		
		try {
			int result = memberService.phoneCheck(phone);
		
			if(result != 0)return ResponseEntity.status(HttpStatus.CONFLICT).body("The phone number is already in use.");
			
			return new ResponseEntity<>(HttpStatus.OK);
			
		}catch(Exception e) {
			log.error(e.getMessage());
			
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred." + e.getMessage());
		}
		
	}

	/**
	 * 사용자가 입력한 이메일의 중복을 확인합니다.
	 * 
	 * @param 사용자가 입력한 이메일
	 * @return HTTP 응답 상태 코드와 중복 여부를 나타내는 메시지
	 *         - 200 OK: 이메일 사용 가능
	 *         - 400 BAD REQUEST: 입력된 이메일이 null이거나 형식이 올바르지 않음
	 *         - 409 CONFLICT: 이메일이 이미 사용 중
	 *         - 500 INTERNAL SERVER ERROR: 서버 내부 오류 발생
	 * @throws Exception 서버 처리 중 발생한 예외
	 */
	@GetMapping(value = "/email/check")
	public ResponseEntity<String> emailChk(@RequestParam String email) throws Exception {

		if(email == null || email == "") return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("email is null.");
		
		String emailLegExp = "^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*\\.[a-zA-Z]{2,3}$";
		
		if (!email.matches(emailLegExp)) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid email.");
		
		try {
			int result = memberService.emailCheck(email);
			
			if(result != 0 )return ResponseEntity.status(HttpStatus.CONFLICT).body("The email is already in use.");
			
			return new ResponseEntity<>(HttpStatus.OK);
			
		}catch(Exception e) {
			log.error(e.getMessage());
			
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}
	

	/**
	 * 이메일과 전화번호를 이용하여 사용자의 아이디를 찾습니다.
	 * 
	 * @param String email 사용자가 입력한 이메일 
	 * @param String phone 사용자가 입력한 전화번호
	 * 
	 * @return HTTP 응답 상태코드와 사용자의 아이디
	 **/
	@GetMapping(value = "/findId")
	public ResponseEntity<String> findId(@RequestParam("email") String email, @RequestParam("phone") String phone) throws Exception {

		String userId = memberService.FindId(email, phone);

		if (userId != null && !userId.isEmpty()) {
	        return ResponseEntity.ok(userId);
	    }

	    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User ID not found.");
	}

	/**
	 * 사용자 정보를 확인 후 임시 비밀번호 발급합니다.
	 * 
	 * @param member 사용자의 아이디와 이메일, 닉네임을 포함한 객체
	 * @return HTTP 응답 상태코드
	 */
	@GetMapping("/findPw")
	public ResponseEntity<String> findPw(@RequestBody MemberVO member) throws Exception {

		String user_id = member.getUser_id();
		String email = member.getEmail();
		String phone = member.getPhone();
		
		if (user_id == null || user_id.isEmpty()) {
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("user id is required.");
	    }
	    if (email == null || email.isEmpty()) {
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("email is required.");
	    }
	    if (phone == null || phone.isEmpty()) {
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("phone is required.");
	    }
		
		try {
			// 입력한 정보와 일치하는 사용자가 있는지 확인
			int user = memberService.FindPw(user_id, email, phone);

			if (user == 1) {

				// 임시 비밀번호 메일로 전송, 임시 비밀번호로 변경
				ResponseEntity<String> response = memberService.updatePwAndSendEmail(user_id, email);

	            if (response.getStatusCode() == HttpStatus.OK) {
	            	log.info("Password update and email sent successfully.");
	                return new ResponseEntity<>(HttpStatus.OK);
	            } else {
	                return new ResponseEntity<>(response.getBody(), response.getStatusCode());
	            }
	            
			} else {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid user information.");
			}
		}catch(Exception e) {
			
			log.error(e.getMessage());
			
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
		}
	}


	/**
	 * 카카오 로그인
	 * @param code 인가코드
	 * @return 메인 페이지 or 로그인 이전 페이지
	 */
	@PostMapping(value = "/kakao_login")
	public ResponseEntity<String> kakaoLogin(@RequestBody MemberVO member, HttpServletRequest request, HttpServletResponse response) {
		
		try {
			member.setUser_pw(encoder.encode("kakaoLoginPassword"));
			int kakaoUser = memberService.socialLoginOrRegister(member);
	
			if(kakaoUser != 1)return ResponseEntity.internalServerError().body("Unable to complete membership");
			
			// 사용자 이름과 비밀번호로 인증 객체 생성
			Authentication authentication = authenticationManager
					.authenticate(new UsernamePasswordAuthenticationToken(member.getUser_id(), "kakaoLoginPassword"));

			/********* 이거 잘되는지 확인해야 됨 
			 * 아래 오토 로그인도 클라이언트 측 코드 추가해야됨!!
			 * 
			 * 
			 * */
			String userName = member.getUser_name();
			
			boolean autoLogin = false;
			
			// JWT 토큰 생성
			TokenInfo token = jwtTokenProvider.generateToken(authentication, userName, autoLogin);
			
			// refresh 토큰
			String refreshToken = token.getRefreshToken();

			try {
				// refresh 토큰 저장
				int addRefreshToken = memberService.addRefreshToken(member.getUser_id(), refreshToken);
				if (addRefreshToken != 1) {
					return ResponseEntity.status(500).body("Internal Server Error: Refresh Token 저장 중 오류 발생");
				}
			} catch (Exception e) {
				log.error("Failed to add refresh token", e);
				return ResponseEntity.status(500).body("Internal Server Error: " + e.getMessage());
			}
			
			request.getSession().setAttribute("username", member.getUser_name());

			// 쿠키에 jwt 토큰 저장
		    Cookie jwtCookie = new Cookie("Authorization", token.getAccessToken());
		    jwtCookie.setHttpOnly(true);  // 자바스크립트에서 접근 불가능
		    jwtCookie.setPath("/");       // 해당 쿠키의 유효 경로 설정
		    jwtCookie.setMaxAge(24 * 60 * 60); // 쿠키 유효 기간 설정 (1일)

		    // 쿠키를 응답에 추가
		    response.addCookie(jwtCookie);
		    
			// 로그인 이전 페이지
			String prevPage = (String) request.getSession().getAttribute("prevPage");

			return ResponseEntity.ok().header("prev-page", prevPage).body("success");

		} catch (AuthenticationException e) {
			/*유효하지 않은 로그인 정보*/
			log.error("Authentication failed: " + e.getMessage(), e);
			return ResponseEntity.status(401).body("Authentication failed: " + e.getMessage());
		} catch (Exception e) {
			log.error("An error occurred during login", e);
			return ResponseEntity.status(500).body("Internal Server Error: " + e.getMessage());
		}
	}


	/*****************************************
	 * 네이버 로그인
	 * @param code    인가코드
	 * @param state
	 * @param request
	 * @return 메인 페이지 or 로그인 이전 페이지
	 ****************************************/
	@PostMapping(value = "/naver_login")
	public ResponseEntity<String> naverLogin(@RequestBody MemberVO member, HttpServletRequest request, HttpServletResponse response) {

		try {
			
			member.setUser_pw(encoder.encode("naverLoginPassword"));
			
			int naverUser = memberService.socialLoginOrRegister(member);

			if(naverUser != 1)return ResponseEntity.internalServerError().body("Unable to complete membership");
			
			// 사용자 이름과 비밀번호로 인증 객체 생성
			Authentication authentication = authenticationManager
					.authenticate(new UsernamePasswordAuthenticationToken(member.getUser_id(), "naverLoginPassword"));

			/******************카카오랑 네이버 이거 잘되는지 확인*/
			String userName = member.getUser_name();
			
			boolean autoLogin = false;
			
			// JWT 토큰 생성
			TokenInfo token = jwtTokenProvider.generateToken(authentication, userName, autoLogin);
			
			// refresh 토큰
			String refreshToken = token.getRefreshToken();

			try {
				// refresh 토큰 db에 저장
				int addRefreshToken = memberService.addRefreshToken(member.getUser_id(), refreshToken);
				if (addRefreshToken != 1) {
					log.error("Internal Server Error: Refresh Token 저장 중 오류 발생");
					return ResponseEntity.internalServerError().body("Internal Server Error: Refresh Token 저장 중 오류 발생");
				}
			} catch (Exception e) {
				log.error("Failed to add refresh token", e);
				log.error(e.getMessage());
				return ResponseEntity.internalServerError().body(e.getMessage());
			}
			
			request.getSession().setAttribute("username",member.getUser_name());
			
			// 쿠키에 jwt 토큰 저장
		    Cookie jwtCookie = new Cookie("Authorization", token.getAccessToken());
		    jwtCookie.setHttpOnly(true);  // 자바스크립트에서 접근 불가능
		    jwtCookie.setPath("/");       // 해당 쿠키의 유효 경로 설정
		    jwtCookie.setMaxAge(24 * 60 * 60); // 쿠키 유효 기간 설정 (1일)

		    // 쿠키를 응답에 추가
		    response.addCookie(jwtCookie);
		    
			// 로그인 이전 페이지
			String prevPage = (String) request.getSession().getAttribute("prevPage");

			return ResponseEntity.ok().header("prev-page", prevPage).body("success");
			
		} catch (AuthenticationException e) {
			log.error("Authentication failed: " + e.getMessage(), e);
			return ResponseEntity.internalServerError().body(e.getMessage());
		} catch (Exception e) {
			log.error("An error occurred during login", e);
			return ResponseEntity.internalServerError().body(e.getMessage());
		}
	}
	
	/**
	 * 마이페이지 - 사용자 정보 조회
	 * 
	 * @return HTTP 응답 상태코드, 사용자 아이디, 닉네임, 주소, 전화번호, 소셜로그인 여부 
	 */
	@GetMapping(value = "/account", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<MemberVO> getAccount(HttpServletRequest request, HttpServletResponse response){
		try {
			boolean isPasswordVerified = isPasswordVerified(request);
			
			MemberVO account = new MemberVO();
			
			if(isPasswordVerified) {
				if(response.getStatus() == 200) {
					String userId = (String) request.getAttribute("userId");
					
					account = memberService.account(userId);
				}
			}
			return ResponseEntity.ok().body(account);
		}catch(Exception e) {
			log.error(e.getMessage());
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * 사용자의 닉네임을 변경합니다.
	 * 
	 * @param user_name 변경할 닉네임
	 * @return HTTP 응답 상태코드
	 */
	@PutMapping(value ="/userName", produces = "application/json")
	public ResponseEntity<String> updateUserName(HttpServletRequest request, @RequestBody MemberVO member) throws Exception {
		try {
		    String userId = (String) request.getAttribute("userId");
		    
		    member.setUser_id(userId);
			if (member.getUser_name() == null)
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("userName is required.");
		
			int isUpdated = memberService.updateUserName(member);
			
	        if (isUpdated == 1) {
	            // 세션에 저장된 닉네임 갱신
	            request.getSession().setAttribute("userName", member.getUser_name());
	            
	            return new ResponseEntity<>(HttpStatus.OK);
	        } else {
	            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("닉네임 변경에 실패했습니다.");
	        }

		} catch (Exception e) {
			log.error(e.getMessage());

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
		}

	}
	
	/**
	 * 사용자의 전화번호를 변경합니다.
	 * 
	 * @param phone 변경할 전화번호
	 * @return HTTP 응답 상태코드
	 */
	@PutMapping(value = "/phone")
	public ResponseEntity<String> updatePhone(HttpServletRequest request, @RequestBody MemberVO member) throws Exception {

		String userId = (String)request.getAttribute("userId");
		
		if(member.getPhone() == null || member.getPhone().equals("")) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("phone is required.");
		
		member.setUser_id(userId);
		
		try {
			memberService.updatePhone(member);
			
			return new ResponseEntity<>(HttpStatus.OK);
		}catch(Exception e) {
			log.error(e.getMessage());
			
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
		}
		
	}
	
	/**
	 * 비밀번호 검증 여부를 확인합니다.
	 * @return boolean 
	 */
	@GetMapping(value = "/isPasswordVerified")
	public boolean isPasswordVerified(HttpServletRequest request) {
		HttpSession session = request.getSession();
		String tempToken = (String)session.getAttribute("passwordChecked");
		
		boolean passwordChecked = false;
				
		if(tempToken != null) {
			String purpose = jwtTokenProvider.extractPurpose(tempToken);
			passwordChecked = purpose.equals("passwordChecked") ? true : false;
		}
		
		return passwordChecked;
	}

	/**
	 * 사용자의 비밀번호를 확인합니다.
	 * 
	 * @param user_pw 사용자가 입력한 비밀번호
	 * @return HTTP 응답 상태코드
	 * 		- 200 OK: 비밀번호 확인 완료
	 *      - 400 BAD REQUEST: 입력된 비밀번호가 null
	 *      - 500 INTERNAL SERVER ERROR: 서버 내부 오류 발생
	 */
	@RequestMapping(value = "/password/confirm", method = RequestMethod.POST)
	public ResponseEntity<String> pwConfirm(@RequestBody MemberVO member, HttpServletRequest request, HttpSession session) throws Exception {

		if(member.getUser_pw() == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("user password is required.");
		
		String user_id = (String)request.getAttribute("userId");
		member.setUser_id(user_id);
		
		String encodePw = memberService.pwConfirm(member);

		String user_pw = member.getUser_pw();
		
		if (encoder.matches(user_pw, encodePw)) { // 비밀번호 일치여부 판단
			String tempToken = jwtTokenProvider.generatePasswordToken(user_id, "passwordChecked");
	        
			session.setAttribute("passwordChecked", tempToken);
			
			return new ResponseEntity<>(HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
	}
	
	/**
	 * 사용자가 계정 페이지에서 이동하면 세션에 저장된 비밀번호 인증 토큰 삭제
	 */
	@PostMapping("/clearPasswordToken")
	public void clearPasswordToken(HttpServletRequest request) {
		request.getSession().setAttribute("passwordChecked",null);
	}

	/**
	 * 사용자의 비밀번호를 변경합니다.
	 * 
	 * @param user_pw 변경할 비밀번호
	 * @return HTTP 응답 상태코드
	 * 		- 200 OK: 비밀번호 변경 완료
	 *      - 400 BAD REQUEST: 입력된 비밀번호가 null
	 *      - 500 INTERNAL SERVER ERROR: 서버 내부 오류 발생
	 */
	@PutMapping(value = "/password")
	public ResponseEntity<String> updatePw(HttpServletRequest request, @RequestBody MemberVO member) throws Exception {

		if(member.getUser_pw() == null) {
			log.error("user_pw is required.");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("user password is required.");
		}
		
		try {
			HttpSession session = request.getSession();
			
			String token = (String)session.getAttribute("passwordChecked");
			if (token == null) {
	            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Password confirmation is required.");
	        }
			
			TokenStatus tokenStatus = jwtTokenProvider.validateToken(token);
			

	        // 토큰 유효성 검증
	        if (tokenStatus != TokenStatus.VALID) {
	            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid token.");
	        }

	        // 토큰에서 정보 추출
	        String purpose = jwtTokenProvider.extractPurpose(token);
	        String tokenUserId = jwtTokenProvider.extractUserId(token);

	        String userId = (String) request.getAttribute("userId");

	        // 토큰 정보와 세션 사용자 정보 일치 여부 확인
	        if (!tokenUserId.equals(userId) || !purpose.equals("passwordChecked")) {
	            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Password confirmation is required.");
	        }
			
			member.setUser_id(userId);
			
			String encodePw = encoder.encode(member.getUser_pw()); // 비밀번호 인코딩

			memberService.updatePw(member.getUser_id(), encodePw);
			
			// 비밀번호 변경 후 세션에서 인증 토큰 제거
	        session.removeAttribute("passwordChecked");
				
			return new ResponseEntity<>(HttpStatus.OK);
				
		}catch(Exception e) {
			log.error(e.getMessage());
			
			return new ResponseEntity<>("An unexpected error occurred.", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * 사용자의 주소를 변경합니다.
	 * 
	 * @param address 변경할 주소
	 * @return HTTP 응답 상태 코드
	 * 		- 200 OK: 주소 변경 완료
	 *      - 400 BAD REQUEST: 입력된 주소가 null
	 *      - 500 INTERNAL SERVER ERROR: 서버 내부 오류 발생
	 */
	@PutMapping("/address")
	public ResponseEntity<String> updateAddress(@RequestBody MemberVO member, HttpServletRequest request) throws Exception {
		
		String address = member.getAddress();
		if(address == null || address.isEmpty()) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("address is required.");
		
		try {
    		String userId = (String) request.getAttribute("userId");
        	
    		member.setUser_id(userId);
    		
			memberService.updateAddress(member);
			
			return new ResponseEntity<>(HttpStatus.OK);
		}catch(Exception e) {
			log.error(e.getMessage(),e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
		}
	}

	/**
	 * 회원의 탈퇴를 처리합니다.
	 * 
	 * @return HTTP 응답 상태 코드
	 * 		- 200 OK: 회원 탈퇴 완료
	 *      - 500 INTERNAL SERVER ERROR: 서버 내부 오류 발생
	 */
	
	@DeleteMapping("/withDraw")
    public ResponseEntity<Void> withDraw(HttpServletRequest request, HttpServletResponse response) {
        try {
    		String userId = (String) request.getAttribute("userId");
        	
        	// 회원 정보 삭제
            memberService.withDraw(userId);
            
            // Authorization 쿠키 삭제
    	    Cookie cookie = new Cookie("Authorization", null);
    	    cookie.setHttpOnly(true);
    	    cookie.setSecure(true);
    	    cookie.setPath("/");
    	    cookie.setMaxAge(0);  // 쿠키 만료 시간 0으로 설정 (즉시 삭제)
    	    
    	    response.addCookie(cookie);
    	    
    	    // refreshToken 쿠키 삭제
    	    Cookie refreshCookie = new Cookie("RefreshToken", null);
    	    refreshCookie.setHttpOnly(true);
    	    refreshCookie.setSecure(true);
    	    refreshCookie.setPath("/");
    	    refreshCookie.setMaxAge(0);  // 쿠키 만료 시간 0으로 설정 (즉시 삭제)
    	    
    	    response.addCookie(refreshCookie);
    	    
    	    request.getSession().removeAttribute("isLogin");

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
