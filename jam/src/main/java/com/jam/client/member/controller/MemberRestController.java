package com.jam.client.member.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jam.client.member.service.MemberService;
import com.jam.client.member.vo.MemberVO;
import com.jam.global.exception.BadRequestException;
import com.jam.global.exception.ConflictException;
import com.jam.global.exception.ForbiddenException;
import com.jam.global.exception.NotFoundException;
import com.jam.global.exception.UnauthorizedException;
import com.jam.global.jwt.JwtService;
import com.jam.global.jwt.TokenInfo;
import com.jam.global.jwt.TokenInfo.TokenStatus;
import com.jam.global.util.AuthClearUtil;
import com.jam.global.util.HtmlSanitizer;
import com.jam.global.util.ValidationUtils;

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
	
	/**
	 * 회원 가입
	 *
	 * @param member 회원 정보 객체
	 * @return 회원 가입 결과와 HTTP 상태 코드
	 * @throws Exception 회원 가입 처리 중 예외 발생 시
	 */
	@PostMapping(value = "/join", produces = "application/json")
	public ResponseEntity<String> join(@RequestBody MemberVO member , Model model, HttpServletRequest request) throws Exception {
		
		ValidationUtils.validateUserInfo(member);
		
		String rawPw = member.getUser_pw(); // 인코딩 전 비밀번호
		String encodePw = encoder.encode(rawPw); // 비밀번호 인코딩
		
		member.setUser_pw(encodePw);
		memberService.join(member);
		String prevPage = (String) request.getSession().getAttribute("prevPage");

		return ResponseEntity.ok().header("prev-page", prevPage).body(null);
	}
	
	@GetMapping(value="/loginType")
	public ResponseEntity<String> getLoginType(HttpServletRequest request, HttpServletResponse response) {
		Cookie[] cookies = request.getCookies();
    	String loginType = jwtService.extractLoginType(request, response, cookies);
    	
    	return ResponseEntity.ok(loginType);
	}
	
	/**
	 * JWT 토큰 검증 후 JWT 토큰에 저장된 사용자의 아이디와 닉네임, 권한 정보를 반환합니다.
	 * 
	 * @return HTTP 응답 상태 코드와 사용자 정보를 포함한 객체
	 */
	@GetMapping(value = "/me/token", produces = MediaType.APPLICATION_JSON_VALUE)
	@CrossOrigin
	public ResponseEntity<MemberVO> getUserInfo(HttpServletResponse response, HttpServletRequest request) {
		
		MemberVO member = new MemberVO();
		
		if(response.getStatus() == 200) {
			String userId = (String) request.getAttribute("userId");
			String auth = (String) request.getAttribute("auth");
			String userName = (String)request.getSession().getAttribute("userName");
			
			if(userId != null && userName != null && auth != null) {
				
				member.setUser_id(userId);
				member.setRole(auth);
				member.setUser_name(userName);
				
				return ResponseEntity.ok().body(member);
			}
		}else {
			Cookie[] cookies = request.getCookies();
			if(cookies != null) deleteJwtCookies(cookies, response);
		}
		
		return ResponseEntity.ok().body(member);
	}
	
	/**
	 * 인증 여부 확인용 엔드포인트
	 *
	 * - 이 엔드포인트는 실제 로직은 없고, 인터셉터를 통해 인증 상태를 확인합니다.
	 * - 인증된 사용자라면 200 OK를 반환하고, 인증되지 않은 사용자는 401 Unauthorized를 반환합니다.
	 *
	 * @return 200 OK (정상적으로 인증된 경우)
	 */
	@GetMapping("/auth/check")
	public ResponseEntity<Void> checkAuthentication(HttpServletRequest request) {
	    String userId = (String)request.getAttribute("userId");
	    
	    if(userId == null) throw new UnauthorizedException("인증되지 않은 사용자입니다.");
        return ResponseEntity.ok().build();
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
	public ResponseEntity<String> findId(@RequestBody MemberVO req) throws Exception {
		
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
	public ResponseEntity<String> issueTempPassword(@RequestBody MemberVO member) throws Exception {
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
		
		return ResponseEntity.ok().build();
	}
	
	/**
	 * 사용자의 닉네임을 변경합니다.
	 * 
	 * @param user_name 변경할 닉네임
	 * @return HTTP 응답 상태코드
	 */
	@PutMapping(value ="/userName")
	public ResponseEntity<Void> updateUserName(HttpServletRequest request, HttpServletResponse response, @RequestBody MemberVO member) throws Exception {
		Boolean verifyStatus = (Boolean)request.getSession().getAttribute("verifyStatus");
		
		if (verifyStatus == null || !verifyStatus) {
			log.error("not verified.");
			throw new ForbiddenException("본인 확인이 필요합니다. 다시 비밀번호를 입력해주세요.");
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
		
    	Map<String, Object> data = resolveAuthenticatedUser(request, response);
    	
    	if (data == null || data.get("user") == null) {
    		throw new UnauthorizedException("인증되지 않은 사용자입니다.");
    	}
    	
    	MemberVO user = (MemberVO) data.get("user");
    	boolean autoLogin = (boolean) data.get("autoLogin");
    	String loginType = (String) data.get("loginType");
    	
    	// 변경할 닉네임 세팅
    	user.setUser_name(member.getUser_name());
    	
    	Authentication authentication = memberService.updateUserNameAndTokens(user, autoLogin, loginType, response);
    	TokenInfo token = jwtService.generateTokenFromAuthentication(authentication, autoLogin, loginType);
    	setJwtCookies(token, response, autoLogin);
    	
        return new ResponseEntity<>(HttpStatus.OK);
	}
	
	/**
	 * 사용자의 전화번호를 변경합니다.
	 * 
	 * @param phone 변경할 전화번호
	 * @return HTTP 응답 상태코드
	 */
	@PutMapping(value = "/phone")
	public ResponseEntity<Void> updatePhone(HttpServletRequest request, @RequestBody MemberVO member) throws Exception {
		Boolean verifyStatus = (Boolean)request.getSession().getAttribute("verifyStatus");
		if(verifyStatus == null || !verifyStatus) {
			log.error("not verified.");
			throw new ForbiddenException("유효하지 않은 인증 정보 입니다.");
		}
		
		String phone = member.getPhone();
		
		if (phone == null || phone.trim().isEmpty()) {
			throw new BadRequestException("전화번호를 입력하세요.");
		}

		if(HtmlSanitizer.hasHtmlTag(phone)) throw new BadRequestException("HTML 태그는 허용되지 않습니다.");
		if(!ValidationUtils.validatePhone(phone)) throw new BadRequestException("전화번호 형식이 올바르지 않습니다.");

		
		String userId = (String)request.getAttribute("userId");
		if(userId == null) {
			log.error("UnAuthorized user.");
			throw new UnauthorizedException("로그인 정보가 만료되었습니다. 다시 로그인 후 시도해주세요.");
		}
		
		member.setUser_id(userId);
		
		boolean isUpdate = memberService.updatePhone(member);
		
		if(isUpdate) {
			return ResponseEntity.ok().build();
		}else {
			throw new Exception("오류가 발생했습니다. 잠시 후 다시 시도하세요.");
		}
	}
	
	 
	/**
	 * 사용자의 비밀번호 확인 후, verify-password 플래그를 세션에 저장합니다.
	 * (마이페이지 > 계정정보(Account)에서 인증 확인 용도)
	 * 
	 * @param user_pw 사용자가 입력한 비밀번호
	 * @return HTTP 응답 상태코드
	 * 		- 200 OK: 비밀번호 확인 완료
	 *      - 400 BAD REQUEST: 입력된 비밀번호가 null
	 *      - 401 UNAUTHORIZED: 잘못된 비밀번호
	 *      - 440 (Custom): JWT 토큰 인증되지 않음
	 *      - 500 INTERNAL SERVER ERROR: 서버 내부 오류 발생
	 */
	@PostMapping("/verify-password")
	public ResponseEntity<String> verifyPassword(@RequestBody MemberVO member, HttpServletRequest request, HttpServletResponse response) throws Exception {

		if(member.getUser_pw() == null) throw new BadRequestException("변경할 비밀번호를 입력하세요.");
		
		String user_id = (String)request.getAttribute("userId");
		
		if(user_id == null || user_id.equals("")) {
			AuthClearUtil.clearAuth(request, response);
			
			log.error("VerifyPassword: userId is null.");
			throw new UnauthorizedException("로그인 정보가 만료되었습니다. 다시 로그인 후 시도해주세요.");
		}
		
		member.setUser_id(user_id);
		
		String encodePw = memberService.pwConfirm(member);

		String user_pw = member.getUser_pw();
		
		// 비밀번호 일치여부 판단
		if (encoder.matches(user_pw, encodePw)) { 
			HttpSession session = request.getSession(false);
			if (session == null) throw new UnauthorizedException("로그인 정보가 만료되었습니다. 다시 로그인 후 시도해주세요.");
			session.setAttribute("verifyStatus", true);
			return new ResponseEntity<>(HttpStatus.OK);
		} else {
			throw new UnauthorizedException("비밀번호가 일치하지 않습니다.");
		}
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

		/* FIXME: 비밀번호 변경 후 로그아웃 처리, 재로그인 하도록 함 
		DB에서 비밀번호 변경 완료
		RefreshToken 삭제 (DB에서 해당 유저 토큰 전부 제거)
		SecurityContextHolder.clearContext()
		request.getSession(false).invalidate()
		쿠키 삭제 (Authorization, RefreshToken)
		로그인 페이지로 리다이렉트
		*/
		
		String password = member.getUser_pw();
		
		if(password == null || password.isEmpty()) throw new BadRequestException("비밀번호를 입력하세요.");
		if(!ValidationUtils.validatePassword(password)) throw new BadRequestException("잘못된 비밀번호입니다.");
			
		HttpSession session = request.getSession();
		Boolean verifyStatus = (Boolean) session.getAttribute("verifyStatus");
		
		if (verifyStatus == null || !verifyStatus) {
			 throw new ForbiddenException("유효하지 않은 인증 정보입니다.");
        }
		
        String userId = (String) request.getAttribute("userId");
        if (userId == null) {
        	throw new UnauthorizedException("로그인 정보가 만료되었습니다. 다시 로그인 후 시도해주세요.");
        }
        
		member.setUser_id(userId);
		
		String encodePw = encoder.encode(password); // 비밀번호 인코딩

		memberService.updatePw(member.getUser_id(), encodePw);
		
		// 민감한 정보 변경 후에는 검증 플래그 삭제
		session.removeAttribute("verifyStatus");
		
		return new ResponseEntity<>(HttpStatus.OK);
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
		if(address == null || address.isEmpty()) throw new BadRequestException("주소를 입력하세요.");
		
		String userId = (String) request.getAttribute("userId");
    	
		member.setUser_id(userId);
		
		memberService.updateAddress(member);
		
		return new ResponseEntity<>(HttpStatus.OK);
	}

	/**
	 * 회원 탈퇴
	 * 
	 * @return HTTP 응답 상태 코드
	 * 		- 200 OK: 회원 탈퇴 완료
	 *      - 500 INTERNAL SERVER ERROR: 서버 내부 오류 발생
	 */
	@DeleteMapping("/me")
    public ResponseEntity<Void> deleteAccount(HttpServletRequest request, HttpServletResponse response) {
		
    	// 1. 인증 플래그 확인
		HttpSession session = request.getSession(false);
		Boolean verifyStatus = session != null ? (Boolean) session.getAttribute("verifyStatus") : null;
    	if(verifyStatus == null || !verifyStatus) throw new ForbiddenException("인증 정보가 유효하지 않습니다.");
    	
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
    	String userId = (String)request.getAttribute("userId");
    	
        if (userId == null) {
        	AuthClearUtil.clearAuth(request, response);
        	log.error("회원 탈퇴 오류: 사용자 아이디 없음.");
        	throw new UnauthorizedException("로그인 정보가 만료되었습니다. 다시 로그인 후 시도해주세요.");
	    }
        
        // 5. 세션 userId 일관성 검증
        String sessionUserId = session != null ? (String) session.getAttribute("userId") : null;
        if (sessionUserId != null && !sessionUserId.equals(userId)) {
        	AuthClearUtil.clearAuth(request, response);
        	log.error("회원 탈퇴 오류: 토큰 정보와 세션 정보 불일치");
        	throw new UnauthorizedException("인증 정보가 유효하지 않습니다. 다시 로그인 후 시도해주세요.");
        }
        
        // 6. DB 삭제
        memberService.deleteAccount(userId);
        
        // 6. 인증정보 삭제 (로그아웃 처리)
    	AuthClearUtil.clearAuth(request, response);
    	
        return ResponseEntity.ok().build();
    }
	
	/**
	 * 일반 회원을 기업 회원으로 전환합니다.
	 * 
	 * 1. 요청 본문의 회사명 유효성 검사 (필수값, HTML 태그 제한)
     * 2. 현재 로그인된 사용자 정보 확인 (resolveAuthenticatedUser)
     * 3. DB 권한 변경 및 새로운 인증 정보(Authentication) 생성
     * 4. 변경된 권한이 반영된 새로운 JWT 토큰 재발급 및 쿠키 설정
     * 
	 * @param request  HttpServletRequest 
	 * @param response HttpServletResponse
	 * @param member   회사명(company_name)을 포함한 요청 객체
	 * 
	 * @return ResponseEntity<String> - "기업회원 전환 성공" 메시지
     * @throws BadRequestException 회사명이 없거나 부적절한 경우
     * @throws UnauthorizedException 로그인이 되어 있지 않은 경우
	 */
	@PostMapping(value="/convertBusiness")
	public ResponseEntity<String> convertBusiness(HttpServletRequest request, HttpServletResponse response,  MemberVO member){
		
		String company_name = member.getCompany_name();
		if (company_name == null || company_name.trim().isEmpty()) {
			throw new BadRequestException("회사명을 입력하세요.");
		}

		if (HtmlSanitizer.hasHtmlTag(company_name)) {
			throw new BadRequestException("HTML 태그는 허용되지 않습니다.");
		}
		
		Map<String, Object> data = resolveAuthenticatedUser(request, response);

		if(data == null || data.get("user") == null) throw new UnauthorizedException("로그인이 필요한 서비스입니다.");
		
		MemberVO user = (MemberVO)data.get("user");
		
		user.setCompany_name(company_name);	
    	Authentication authentication = memberService.convertBusiness(user.getUser_id(), company_name, user);
    	
    	TokenInfo token = jwtService.generateTokenFromAuthentication(authentication, (boolean)data.get("autoLogin"), (String)data.get("loginType"));
		
    	setJwtCookies(token, response, (boolean)data.get("autoLogin"));

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
	
	/**
	 * HttpServletRequest와 쿠키 정보를 기반으로 사용자 인증을 검증합니다.
	 * 
	 * @param request HttpServletRequest (쿠키와 세션 접근)
	 * @return 
	 * - 인증 성공 시: loginType, autoLogin, user 정보를 포함한 Map
	 * - 인증 실패 시: UnauthorizedException
	 */
	private Map<String, Object> resolveAuthenticatedUser(HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> data = new HashMap<>();
		
		Cookie[] cookies = request.getCookies();
    	if(cookies == null) {
    		log.error("쿠키가 없습니다."); 
			throw new UnauthorizedException("인증 정보가 유효하지 않습니다. 다시 로그인해주세요.");
    	}

    	String accessToken = jwtService.extractToken(cookies, "Authorization");
    	if (accessToken == null || accessToken.isEmpty()) {
    		log.error("인증 토큰이 없습니다.");
			throw new UnauthorizedException("인증 정보가 유효하지 않습니다. 다시 로그인해주세요.");
		}
    	
    	TokenStatus status = jwtService.validateToken(accessToken);
		
		if(status != TokenStatus.VALID) {
	        AuthClearUtil.clearAuth(request, response);
	        
			log.error("유효하지 않은 사용자입니다.");
			throw new UnauthorizedException("인증 정보가 유효하지 않습니다. 다시 로그인해주세요.");
		}

    	MemberVO user = jwtService.extractUserInfoFromToken(accessToken);

    	if (user == null) {
    		AuthClearUtil.clearAuth(request, response);
    		
			log.error("유효하지 않은 사용자입니다.");
			throw new UnauthorizedException("인증 정보가 유효하지 않습니다. 다시 로그인해주세요.");
		}
    	
    	String loginType = jwtService.extractLoginType(request, response, cookies);
    	boolean autoLogin = jwtService.extractAutoLogin(request, response, cookies);

		data.put("loginType", loginType);
		data.put("autoLogin", autoLogin);
    	data.put("user", user);
    	
    	return data;
	}
}
