package com.jam.member.controller;

import java.time.Duration;
import java.util.Map;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jam.global.exception.BadRequestException;
import com.jam.global.exception.ConflictException;
import com.jam.global.exception.ForbiddenException;
import com.jam.global.exception.NotFoundException;
import com.jam.global.exception.UnauthorizedException;
import com.jam.global.jwt.JwtService;
import com.jam.global.jwt.TokenInfo;
import com.jam.global.util.AuthClearUtil;
import com.jam.global.util.HtmlSanitizer;
import com.jam.global.util.ValidationUtils;
import com.jam.member.dto.MemberDto;
import com.jam.member.service.MemberService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
@Slf4j
public class MemberRestController {
	private final MemberService memberService;
	private final PasswordEncoder encoder;
	private final JwtService jwtService;
	private final StringRedisTemplate redisTemplate;
	
	/**
	 * 회원 가입
	 *
	 * @param member 회원 정보 객체
	 * @return 회원 가입 결과와 HTTP 상태 코드
	 * @throws Exception 회원 가입 처리 중 예외 발생 시
	 */
	@PostMapping(value = "/join", produces = "application/json")
	public ResponseEntity<String> join(@RequestBody MemberDto member, HttpServletRequest request) throws Exception {
		
		ValidationUtils.validateUserInfo(member);
		
		String rawPw = member.getUser_pw(); // 인코딩 전 비밀번호
		String encodePw = encoder.encode(rawPw); // 비밀번호 인코딩
		
		member.setUser_pw(encodePw);
		memberService.join(member);
		
		return ResponseEntity.ok().body(null);
	}
	
	@GetMapping(value="/loginType")
	public ResponseEntity<String> getLoginType(HttpServletRequest request, HttpServletResponse response) {
		Cookie[] cookies = request.getCookies();
    	String loginType = jwtService.extractLoginType(request, response, cookies);
    	
    	return ResponseEntity.ok(loginType);
	}
	
	/**
	 * 사용자가 입력한 아이디의 중복을 확인합니다.
	 * 
	 * @param userId 사용자가 입력한 아이디
	 * @return 200 OK: 아이디 사용 가능
	 * @throws BadRequestException 아이디가 비어있거나 형식이 올바르지 않은 경우
	 * @throws ConflictException 이미 사용 중인 아이디인 경우
	 * 
	 * 오류 메시지는 GlobalExceptionHandler에서 JSON 응답의 detail 필드로 전달됨.
	 */
	@GetMapping(value = "/userId/check")
	public ResponseEntity<String> idChk(@RequestParam String userId) throws Exception {
		if (userId == null || userId.isBlank()) {
			throw new BadRequestException("아이디를 입력하세요.");
		}
		
		if(HtmlSanitizer.hasHtmlTag(userId)) throw new BadRequestException("HTML 태그는 허용되지 않습니다.");
		if(!ValidationUtils.validateUserId(userId)) throw new BadRequestException("아이디는 8~20자 이내로 영문, 숫자를 혼용하여 입력해 주세요");
		
		int result = memberService.idCheck(userId);
		if(result != 0 )throw new ConflictException("이미 사용중인 아이디 입니다.");

		return ResponseEntity.ok().build();
	}

	/**
	 * 사용자가 입력한 닉네임의 중복을 확인합니다.
	 * 
	 * @param 사용자가 입력한 닉네임
	 * @return 200 OK: 닉네임 사용 가능
	 * @throws BadRequestException 닉네임이 비어있거나 형식이 올바르지 않은 경우
	 * @throws ConflictException 이미 사용 중인 닉네임인 경우
	 * 
	 * 오류 메시지는 GlobalExceptionHandler에서 JSON 응답의 detail 필드로 전달됨.
	 */
	@GetMapping(value="/userName/check")
	public ResponseEntity<String> nameChk(@RequestParam String userName) throws Exception {
		if (userName == null || userName.trim().isEmpty()) {
			throw new BadRequestException("닉네임을 입력하세요.");
		}

		if(HtmlSanitizer.hasHtmlTag(userName)) throw new BadRequestException("HTML 태그는 허용되지 않습니다.");
		if(!ValidationUtils.validateNickname(userName)) throw new BadRequestException("닉네임은 3~10자 이내로 입력해주세요.");

		int result = memberService.nameCheck(userName);
		if (result != 0) {
			throw new ConflictException("이미 사용 중인 닉네임입니다.");
		}

		return ResponseEntity.ok().build();
	}
	
	/**
	 * 사용자가 입력한 전화번호의 중복을 확인합니다.
	 *
	 * @param phone 사용자가 입력한 전화번호
	 * @return 200 OK: 전화번호 사용 가능
	 * @throws BadRequestException 전화번호가 비어있거나 형식이 올바르지 않은 경우
	 * @throws ConflictException 이미 사용 중인 전화번호인 경우
	 * 
	 * 오류 메시지는 GlobalExceptionHandler에서 JSON 응답의 detail 필드로 전달됨.
	 */
	@GetMapping(value = "/phone/check")
	public ResponseEntity<String> phoneChk(@RequestParam String phone) throws Exception {
		if (phone == null || phone.trim().isEmpty()) {
			throw new BadRequestException("전화번호를 입력하세요.");
		}

		if(HtmlSanitizer.hasHtmlTag(phone)) throw new BadRequestException("HTML 태그는 허용되지 않습니다.");
		if(!ValidationUtils.validatePhone(phone)) throw new BadRequestException("전화번호 형식이 올바르지 않습니다.");

		int result = memberService.phoneCheck(phone);
		if (result != 0) {
			throw new ConflictException("이미 사용 중인 전화번호입니다.");
		}

		return ResponseEntity.ok().build();
	}

	/**
	 * 사용자가 입력한 이메일의 중복을 확인합니다.
	 * 
	 * @param email 사용자가 입력한 이메일
	 * @return 200 OK: 이메일 사용 가능
	 * @throws BadRequestException 이메일이 비어있거나 형식이 올바르지 않은 경우
	 * @throws ConflictException 이미 사용 중인 이메일인 경우
	 * 
	 * 오류 메시지는 GlobalExceptionHandler에서 JSON 응답의 detail 필드로 전달됨.
	 */
	@GetMapping(value = "/email/check")
	public ResponseEntity<String> emailChk(@RequestParam String email) throws Exception {

		if (email == null || email.trim().isEmpty()) {
			throw new BadRequestException("이메일을 입력하세요.");
		}

		if(HtmlSanitizer.hasHtmlTag(email)) throw new BadRequestException("HTML 태그는 허용되지 않습니다.");
		if(!ValidationUtils.validateEmail(email)) throw new BadRequestException("올바른 이메일 형식이 아닙니다.");

		int result = memberService.emailCheck(email);
		if (result != 0) {
			throw new ConflictException("이미 사용 중인 이메일입니다.");
		}

		return ResponseEntity.ok().build();
	}
	
	/**
	 * 이메일과 전화번호를 이용하여 사용자의 아이디를 찾습니다.
	 * 
	 * @param String email 사용자가 입력한 이메일 
	 * @param String phone 사용자가 입력한 전화번호
	 * 
	 * @return HTTP 응답 상태코드와 사용자의 아이디
	 **/
	@PostMapping(value = "/id/find")
	public ResponseEntity<String> findId(@RequestBody MemberDto req) throws Exception {
		
		String email = req.getEmail();
		String phone = req.getPhone();
		
		if(email == null || email.trim().isEmpty()) throw new BadRequestException("이메일을 입력하세요.");
		if(phone == null || phone.trim().isEmpty()) throw new BadRequestException("전화번호를 입력하세요.");
		
		if (!ValidationUtils.validateEmail(email)) throw new BadRequestException("올바른 이메일 형식이 아닙니다.");
		if (!ValidationUtils.validatePhone(phone)) throw new BadRequestException("전화번호 형식이 올바르지 않습니다.");

		String userId = memberService.FindId(email, phone);

		if (userId != null && !userId.isEmpty()) {
	        return ResponseEntity.ok(maskId(userId));
	    }
		throw new NotFoundException("회원정보를 찾을 수 없습니다.");
	}
	
	// 아이디 마스킹 
	public String maskId(String s) {
		if (s == null || s.isEmpty()) return "";
		
		int n = s.length();
		
		if(n == 1) return s;
		if(n == 2) return s.substring(0, 1) + "*";
		if(n == 3) return s.substring(0, 1) + "*" + s.substring(2);
		if(4 <= n && n <= 7) return s.substring(0, 1) + "**" + s.substring(3);
		
		return s.substring(0, 3) + "*".repeat(n-5) + s.substring(n-2);
	}
	
	/**
	 * 사용자 정보를 확인 후 임시 비밀번호 발급합니다.
	 * 
	 * @param member 사용자의 아이디와 이메일, 전화번호를 포함한 객체
	 * @return HTTP 응답 상태코드
	 */
	@PostMapping("/password/temp")
	public ResponseEntity<String> issueTempPassword(@RequestBody MemberDto member) throws Exception {
		// TODO: 현재는 임시비밀번호 발급 방식 사용 중
		// 비밀번호 재설정 링크 방식으로 개선 예정

		String user_id = member.getUser_id();
		String email = member.getEmail();
		String phone = member.getPhone();
		
		if (user_id == null || user_id.isEmpty()) throw new BadRequestException("아이디를 입력하세요.");
	    if (email == null || email.isEmpty()) throw new BadRequestException("이메일을 입력하세요.");
	    if (phone == null || phone.isEmpty()) throw new BadRequestException("전화번호를 입력하세요.");

		if (!ValidationUtils.validateEmail(email) || HtmlSanitizer.hasHtmlTag(email)) throw new BadRequestException("올바른 이메일 형식이 아닙니다.");
		if (!ValidationUtils.validatePhone(phone) || HtmlSanitizer.hasHtmlTag(phone)) throw new BadRequestException("전화번호 형식이 올바르지 않습니다.");
		
		// 임시 비밀번호로 변경, 임시 비밀번호 메일로 전송
		memberService.updatePwAndSendEmail(user_id, email, phone);
		
		// 검증 플래그 삭제
		String key = "auth:mypage:" + user_id;
    	redisTemplate.delete(key);
    	
		return ResponseEntity.ok().build();
	}
	
	/**
     * 사용자의 닉네임을 변경하고 JWT 토큰을 갱신합니다.
     * 
     * @param user         인증 컨텍스트에서 가져온 현재 로그인 사용자 정보
     * @param request      HTTP 요청 객체 (쿠키 추출용)
     * @param response     HTTP 응답 객체 (쿠키 설정용)
     * @param member       변경할 닉네임 정보가 담긴 요청 본문
     * 
     * @throws UnauthorizedException 인증 정보가 없거나 유효하지 않은 경우
     * @throws ForbiddenException    잘못된 검증 플래그
     * @throws BadRequestException   입력된 닉네임이 null 또는 형식이 올바르지 않거나 HTML 태그가 포함된 경우
     * 
     * @return 성공 시 200 OK
     */
	@PutMapping(value ="/userName")
	public ResponseEntity<Void> updateUserName(
			@AuthenticationPrincipal MemberDto user, 
			HttpServletRequest request,
			HttpServletResponse response, 
			@RequestBody MemberDto member) {
		if(user == null || user.getUser_id() == null) throw new UnauthorizedException("로그인이 필요한 서비스 입니다.");
		
		String userId = user.getUser_id();
		String key = "auth:mypage:" + userId;
		String status = redisTemplate.opsForValue().get(key);
	    
		if (!"true".equals(status)) {
			 throw new ForbiddenException("유효하지 않은 인증 정보입니다.");
        }
		
		String userName = member.getUser_name();
		
		if (userName == null || userName.trim().isEmpty()) {
			throw new BadRequestException("닉네임을 입력하세요.");
		}
		if (HtmlSanitizer.hasHtmlTag(userName)) {
			throw new BadRequestException("HTML 태그는 허용되지 않습니다.");
		}
		if (!ValidationUtils.validateNickname(userName)) {
			throw new BadRequestException("닉네임은 3~10자 이내로 입력해주세요.");
		}
		
    	// 변경할 닉네임 세팅
    	user.setUser_name(member.getUser_name());
    	Cookie[] cookies = request.getCookies();

        if(cookies == null) {
        	log.error("쿠키가 없습니다."); 
        	throw new UnauthorizedException("인증 정보가 유효하지 않습니다. 다시 로그인해주세요.");
        }
        
    	String loginType = jwtService.extractLoginType(request, response, cookies);
    	boolean autoLogin = jwtService.extractAutoLogin(request, response, cookies);

    	Authentication authentication = memberService.updateUserNameAndTokens(user, autoLogin, loginType, response);
    	TokenInfo token = jwtService.generateTokenFromAuthentication(authentication, autoLogin, loginType);
    	setJwtCookies(token, response, autoLogin);
    	
    	// 검증 플래그 삭제
    	redisTemplate.delete(key);
    	
        return new ResponseEntity<>(HttpStatus.OK);
	}
	
	/**
	 * 사용자의 전화번호를 변경합니다.
	 * 
     * @param user		인증 컨텍스트에서 가져온 현재 로그인 사용자 정보
	 * @param member 	변경할 전화번호 정보가 담긴 요청 본문
	 * 
     * @throws UnauthorizedException 인증 정보가 없거나 유효하지 않은 경우
     * @throws ForbiddenException    잘못된 검증 플래그
     * @throws BadRequestException   입력된 전화번호가 null 또는 형식이 올바르지 않거나 HTML 태그가 포함된 경우
     * 
     * @return 성공 시 200 OK
	 */
	@PutMapping(value = "/phone")
	public ResponseEntity<Void> updatePhone(
			@AuthenticationPrincipal MemberDto user, 
			@RequestBody MemberDto member) {
		if(user == null || user.getUser_id() == null) throw new UnauthorizedException("로그인이 필요한 서비스 입니다.");
		
		String userId = user.getUser_id();
		String key = "auth:mypage:" + userId;
		String status = redisTemplate.opsForValue().get(key);
	    
		if (!"true".equals(status)) {
			 throw new ForbiddenException("유효하지 않은 인증 정보입니다.");
        }
		
		String phone = member.getPhone();
		
		if (phone == null || phone.trim().isEmpty()) {
			throw new BadRequestException("전화번호를 입력하세요.");
		}

		if(HtmlSanitizer.hasHtmlTag(phone)) throw new BadRequestException("HTML 태그는 허용되지 않습니다.");
		if(!ValidationUtils.validatePhone(phone)) throw new BadRequestException("전화번호 형식이 올바르지 않습니다.");

		member.setUser_id(userId);
		memberService.updatePhone(member);

		// 검증 플래그 삭제
		redisTemplate.delete("auth:mypage:" + userId);
		
		return ResponseEntity.ok().build();
	}

	/**
     * 사용자의 비밀번호 확인 후, verify-password 플래그를 레디스에 저장합니다.
	 * (마이페이지 > 계정정보(Account)에서 인증 확인 용도)
     * 
     * @param user         인증 컨텍스트에서 가져온 현재 로그인 사용자 정보
     * @param request      HTTP 요청 객체 (쿠키 및 세션 확인용)
     * @param response     HTTP 응답 객체 (갱신된 JWT 쿠키 설정용)
     * @param member       사용자가 입력한 비밀번호 정보가 담긴 요청 본문
     * 
     * @throws UnauthorizedException 인증 정보가 없거나 유효하지 않은 경우
     * @throws ForbiddenException    잘못된 검증 플래그
     * @throws BadRequestException   입력된 닉네임이 null 또는 형식이 올바르지 않거나 HTML 태그가 포함된 경우
     * 
     * @return 성공 시 200 OK
     */
	@PostMapping("/verify-password")
	public ResponseEntity<String> verifyPassword(
			@RequestBody MemberDto member, 
			@AuthenticationPrincipal MemberDto user, 
			HttpServletRequest request, 
			HttpServletResponse response) {
		
		if(member.getUser_pw() == null || member.getUser_pw().isBlank()) throw new BadRequestException("비밀번호를 입력하세요.");
		
		if(user == null || user.getUser_id().equals("")) {
			AuthClearUtil.clearAuth(request, response);
			
			log.error("VerifyPassword: userId is null.");
			throw new UnauthorizedException("로그인 정보가 만료되었습니다. 다시 로그인 후 시도해주세요.");
		}
		
		String encodePw = memberService.getPassword(user);
		
		if (encodePw == null || encodePw.isBlank()) {
			throw new UnauthorizedException("비밀번호 정보가 올바르지 않습니다.");
		}
		
		String user_pw = member.getUser_pw();
		
		// 비밀번호 일치 여부 판단
		if (encoder.matches(user_pw, encodePw)) { 
			String key = "auth:mypage:" + user.getUser_id();
	        redisTemplate.opsForValue().set(key, "true", Duration.ofMinutes(10));
			return new ResponseEntity<>(HttpStatus.OK);
		} else {
			throw new UnauthorizedException("비밀번호가 일치하지 않습니다.");
		}
	}
	
	/**
	 * 사용자의 비밀번호를 변경 후 로그아웃 처리 합니다.
	 * 
     * @param user		인증 컨텍스트에서 가져온 현재 로그인 사용자 정보
	 * @param member 	변경할 비밀번호 정보가 담긴 요청 본문
     * @param request   HTTP 요청 객체 
     * @param response  HTTP 응답 객체 
     * 
     * @throws UnauthorizedException 인증 정보가 없거나 유효하지 않은 경우
     * @throws ForbiddenException    잘못된 검증 플래그
     * @throws BadRequestException   입력된 비밀번호가 null 또는 형식이 올바르지 않은 경우
     * 
     * @return 성공 시 200 OK
	 */
	@PutMapping(value = "/password")
	public ResponseEntity<String> updatePw(
			@AuthenticationPrincipal MemberDto user, 
			@RequestBody MemberDto member,
			HttpServletRequest request,
			HttpServletResponse response){
        if (user == null || user.getUser_id() == null) {
        	throw new UnauthorizedException("로그인 정보가 만료되었습니다. 다시 로그인 후 시도해주세요.");
        }
        
		String password = member.getUser_pw();
		
		if(password == null || password.isEmpty()) throw new BadRequestException("비밀번호를 입력하세요.");
		if(!ValidationUtils.validatePassword(password)) throw new BadRequestException("잘못된 비밀번호입니다.");
		
		String userId = user.getUser_id();
		String key = "auth:mypage:" + userId;
		String status = redisTemplate.opsForValue().get(key);
	    
		if (!"true".equals(status)) {
			 throw new ForbiddenException("유효하지 않은 인증 정보입니다.");
        }
		
		String encodePw = encoder.encode(password); // 비밀번호 인코딩

		memberService.updatePw(userId, encodePw);
		//RefreshToken 삭제
		
		// 검증 플래그 삭제
		redisTemplate.delete("auth:mypage:" + userId);
		
		// 로그아웃 처리
		AuthClearUtil.clearAuth(request, response);
		
		return new ResponseEntity<>(HttpStatus.OK);
	}

	/**
	 * 사용자의 주소를 변경합니다.
	 * 
	 * @param payload  주소("address") 정보를 담은 JSON 객체
	 * @param authUser 인증 컨텍스트의 현재 사용자 정보
	 * 
	 * @return 200 OK: 주소 변경 완료
	 * @throws BadRequestException 	주소값이 비어있거나 누락된 경우
	 * @throws UnauthorizedException 로그인이 되어 있지 않은 경우
	 */
	@PutMapping("/address")
	public ResponseEntity<String> updateAddress(
			@RequestBody Map<String, String> payload,
			@AuthenticationPrincipal MemberDto user){
		
		String address = payload.get("address");
		if(address == null || address.isEmpty()) throw new BadRequestException("주소를 입력하세요.");
		
		if(user == null || user.getUser_id() == null) throw new UnauthorizedException("로그인이 필요한 서비스 입니다.");
		
		memberService.updateAddress(address, user.getUser_id());
		
		return new ResponseEntity<>(HttpStatus.OK);
	}

	/**
	 * 회원 탈퇴를 진행 후 모든 인증 정보를 삭제합니다.
	 * 
	 * 1. Redis 인증 플래그(auth:mypage) 확인 및 삭제
	 * 2. 로그인 타입에 따른 소셜 연동 해제 (카카오/네이버)
	 * 3. 로컬 DB 회원 데이터 삭제 및 인증 쿠키 초기화
	 * 
	 * @param user     인증 컨텍스트에서 가져온 현재 사용자 정보
	 * @param request  HTTP 요청 객체 (쿠키 및 세션 접근용)
	 * @param response HTTP 응답 객체 (쿠키 삭제용)
	 * @return 성공 시 200 OK
	 * @throws UnauthorizedException 로그인이 되어 있지 않은 경우
	 * @throws ForbiddenException    잘못된 검증 플래그
	 */
	@DeleteMapping("/me")
    public ResponseEntity<Void> deleteAccount(
    		@AuthenticationPrincipal MemberDto user,
    		HttpServletRequest request, 
    		HttpServletResponse response) {
		
		if(user == null || user.getUser_id() == null) throw new UnauthorizedException("로그인이 필요한 서비스 입니다.");
		
		String userId = user.getUser_id();
		
    	// 1. 인증 플래그 확인
		String key = "auth:mypage:" + userId;
		String status = redisTemplate.opsForValue().get(key);
	    
		if (!"true".equals(status)) {
			 throw new ForbiddenException("유효하지 않은 인증 정보입니다.");
        }
		redisTemplate.delete(key);
		
    	// 2. loginType 확인 (local, kakao, naver)
    	Cookie[] cookies = request.getCookies();
    	String loginType = jwtService.extractLoginType(request, response, cookies);
    	
    	if (loginType != null) {
    		// 3. 소셜 연결 끊기
            switch (loginType) {
                case "kakao":
                	String kakaoAccessToken = getCookieValue(cookies, "kakaoAccessToken");
                	
            		if (kakaoAccessToken != null) {
            	        memberService.kakaoDeleteAccount(kakaoAccessToken);
            	    }
            		
            		if(cookies != null)	deleteCookie(cookies, response, "kakaoAccessToken");
            		break;
            		
                case "naver":
                	String naverAccessToken = getCookieValue(cookies, "naverAccessToken");

                	if(naverAccessToken != null)
            			memberService.naverDeleteAccount(naverAccessToken);
            			
                	if(cookies != null)	deleteCookie(cookies, response, "naverAccessToken");
            		break;
            }
    	}
    	
        // 4. 로컬 회원 탈퇴 
    	AuthClearUtil.clearAuth(request, response);
        memberService.deleteAccount(userId);
        
        return ResponseEntity.ok().build();
    }
	
	/**
	 * 일반 회원을 기업 회원으로 전환하고 권한이 갱신된 토큰을 재발급합니다.
	 * 
	 * 1. 요청 본문의 회사명 유효성 검사 (필수값, HTML 태그 제한)
	 * 2. DB 권한 변경 및 새로운 인증 정보(Authentication) 생성
	 * 3. 변경된 권한이 반영된 새로운 JWT 토큰 재발급 및 쿠키 설정
	 * 
	 * @param request  HTTP 요청 객체 (기존 쿠키 정보 추출용)
	 * @param response HTTP 응답 객체 (새 토큰 쿠키 설정용)
	 * @param user     요청 본문에서 전달된 회사명 정보 객체
	 * @param authUser 인증 컨텍스트의 실제 로그인 사용자 정보
	 * @return 성공 시 "기업회원 전환 성공" 메시지
	 * @throws BadRequestException   회사명이 비어있거나 형식이 올바르지 않은 경우
	 * @throws UnauthorizedException 인증 정보가 없거나 쿠키가 누락된 경우
	 */
	@PostMapping(value="/convertBusiness")
	public ResponseEntity<String> convertBusiness(
			HttpServletRequest request, 
			HttpServletResponse response, 
			@RequestBody MemberDto user,
			@AuthenticationPrincipal MemberDto authUser){
		if(authUser == null) throw new UnauthorizedException("로그인이 필요한 서비스 입니다.");
		
		String company_name = user.getCompany_name();
		if (company_name == null || company_name.trim().isEmpty()) {
			throw new BadRequestException("회사명을 입력하세요.");
		}

		if (HtmlSanitizer.hasHtmlTag(company_name)) {
			throw new BadRequestException("HTML 태그는 허용되지 않습니다.");
		}
		
		Cookie[] cookies = request.getCookies();
		if(cookies == null) {
        	log.error("쿠키가 없습니다."); 
        	throw new UnauthorizedException("인증 정보가 유효하지 않습니다. 다시 로그인해주세요.");
        }
        
    	String loginType = jwtService.extractLoginType(request, response, cookies);
    	boolean autoLogin = jwtService.extractAutoLogin(request, response, cookies);

    	Authentication authentication = memberService.convertBusiness(authUser.getUser_id(), company_name, authUser);
    	
    	TokenInfo token = jwtService.generateTokenFromAuthentication(authentication, autoLogin, loginType);
		
    	setJwtCookies(token, response, autoLogin);

		return ResponseEntity.ok("기업회원 전환 성공");
	}
	
	private String getCookieValue(Cookie[] cookies, String name) {
	    if (cookies == null) return null;
	    for (Cookie cookie : cookies) {
	        if (name.equals(cookie.getName())) {
	            return cookie.getValue();
	        }
	    }
	    return null;
	}
	
	private void setJwtCookies(TokenInfo tokenInfo, HttpServletResponse response, boolean autoLogin) {
		// 쿠키에 jwt 토큰 저장
		Cookie accessTokenCookie = new Cookie("Authorization", tokenInfo.getAccessToken());
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(3 * 60 * 60);
        response.addCookie(accessTokenCookie);

        int maxAge = autoLogin? 30 * 24 * 60 * 60  : 24 * 60 * 60;
        
        Cookie refreshTokenCookie = new Cookie("RefreshToken", tokenInfo.getRefreshToken());
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(maxAge);
        response.addCookie(refreshTokenCookie);
	}

	private void deleteJwtCookies(Cookie[] cookies, HttpServletResponse response) {

		// Authorization 쿠키 삭제
	    Cookie cookie = new Cookie("Authorization", null);
	    cookie.setHttpOnly(true);
	    cookie.setPath("/");
	    cookie.setMaxAge(0);  // 쿠키 만료 시간 0으로 설정
	    
	    response.addCookie(cookie);
	    
	    // refreshToken 쿠키 삭제
	    Cookie refreshTokenCookie = new Cookie("RefreshToken", null);
	    refreshTokenCookie.setHttpOnly(true); 
	    refreshTokenCookie.setMaxAge(0);
	    refreshTokenCookie.setPath("/");
	    
	    response.addCookie(refreshTokenCookie);
	}
	
	private void deleteCookie(Cookie[] cookies, HttpServletResponse response, String cookieName) {
		Cookie cookie = new Cookie(cookieName, null);
		cookie.setHttpOnly(true);	    
		cookie.setMaxAge(0);
		cookie.setPath("/");
	    
	    response.addCookie(cookie);
	}
}
