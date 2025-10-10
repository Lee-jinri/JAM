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
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jam.client.member.service.MemberService;
import com.jam.client.member.vo.MemberVO;
import com.jam.global.jwt.JwtService;
import com.jam.global.jwt.TokenInfo;
import com.jam.global.util.AuthClearUtil;

import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
@Log4j
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
		try {
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
			
		} catch (Exception e) {
			log.error("사용자 정보를 가져올 수 없습니다. : " + e.getMessage());
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
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
	    
	    if(userId == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok().build();
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
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}
		
		if (member.getUser_name() == null) {
			log.error("userName is required.");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}
		
	    try {        	
        	Map<String, Object> data = resolveAuthenticatedUser(request, response);
        	
			if(data == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
			
        	MemberVO user = (MemberVO)data.get("user");
        	
        	if(user == null || user.isEmpty()) {
    	    	log.error("Unauthorized user.");
    	    	return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    	    }
        	
        	boolean autoLogin = (boolean)data.get("autoLogin");
        	String loginType = (String)data.get("loginType");
        	
        	// 변경할 닉네임 세팅
        	user.setUser_name(member.getUser_name());
        	
        	Authentication authentication = memberService.updateUserNameAndTokens(user, autoLogin, loginType, response);
	    	
        	TokenInfo token = jwtService.generateTokenFromAuthentication(authentication, autoLogin, loginType);
			
        	setJwtCookies(token, response, autoLogin);
	    	
	        return new ResponseEntity<>(HttpStatus.OK);
	    } catch (JwtException e) {
	        log.error("JWT 처리 중 오류 발생", e);
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

	    } catch (AuthenticationException e) {
	        log.error("인증 객체 갱신 실패", e);
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
	    } catch (IllegalStateException e) {
	    	log.error("닉네임 변경 로직 실패", e);
	    	return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
	    } catch (Exception e) {
	        log.error("닉네임 변경 처리 중 알 수 없는 오류 발생", e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
	    }
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
			log.error("");
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}
		
		if(member.getPhone() == null || member.getPhone().equals("")) {
			log.error("phone is required.");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}
		
		String userId = (String)request.getAttribute("userId");
		if(userId == null) {
			log.error("UnAuthorized user.");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		
		member.setUser_id(userId);
		
		boolean isUpdate = memberService.updatePhone(member);
		
		if(isUpdate) {
			return ResponseEntity.ok().build();
		}else {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
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
	@RequestMapping(value = "/verify-password", method = RequestMethod.POST)
	public ResponseEntity<String> verifyPassword(@RequestBody MemberVO member, HttpServletRequest request, HttpServletResponse response) throws Exception {

		if(member.getUser_pw() == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("user password is required.");
		
		String user_id = (String)request.getAttribute("userId");
		
		if(user_id == null || user_id.equals("")) {
			AuthClearUtil.clearAuth(request, response);
			
			log.error("VerifyPassword: userId is null.");
			return ResponseEntity.status(440).body("Login required or token expired.");
		}
		
		member.setUser_id(user_id);
		
		String encodePw = memberService.pwConfirm(member);

		String user_pw = member.getUser_pw();
		
		// 비밀번호 일치여부 판단
		if (encoder.matches(user_pw, encodePw)) { 
			return new ResponseEntity<>(HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
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
		request.getSession().invalidate()
		쿠키 삭제 (Authorization, RefreshToken)
		로그인 페이지로 리다이렉트
		*/
		
		if(member.getUser_pw() == null) {
			log.error("user password is required.");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("user password is required.");
		}
		
		try {
			HttpSession session = request.getSession();
			
			Boolean verifyStatus = (Boolean) session.getAttribute("verifyStatus");
			
			if (verifyStatus == null || !verifyStatus) {
	            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("not verified.");
	        }

	        String userId = (String) request.getAttribute("userId");
	        if (userId == null) {
	            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("user Id is missing.");
	        }
	        
			member.setUser_id(userId);
			
			String encodePw = encoder.encode(member.getUser_pw()); // 비밀번호 인코딩

			memberService.updatePw(member.getUser_id(), encodePw);
			
			// 민감한 정보 변경 후에는 검증 플래그 삭제
			session.removeAttribute("verifyStatus");
			
			return new ResponseEntity<>(HttpStatus.OK);
				
		}catch(Exception e) {
			log.error("Error updating password", e);
			
			return new ResponseEntity<>("An unexpected error occurred.", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * 일반 회원을 기업 회원으로 전환합니다.
	 * 
	 * @param request  HttpServletRequest (userId attribute 포함)
	 * @param response HttpServletResponse (JWT 쿠키 세팅/삭제에 사용)
	 * @param member   요청 본문에 포함된 MemberVO (company_name 필수)
	 * @return 성공 시 "기업회원 전환 성공" 메시지 반환,
	 *         실패 시 상황에 맞는 HTTP 상태 코드와 에러 메시지 반환
	 */
	@PostMapping(value="/convertBusiness")
	public ResponseEntity<String> convertBusiness(HttpServletRequest request, HttpServletResponse response, @RequestBody MemberVO member){
		String userId = (String)request.getAttribute("userId");
		
		if (userId == null || userId.isEmpty()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
		}
		
		String company_name = member.getCompany_name();
		if (company_name == null || company_name.trim().isEmpty()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("회사명을 입력해주세요.");
		}
		
		try {
			Map<String, Object> data = resolveAuthenticatedUser(request, response);

			if(data == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
			
			MemberVO user = (MemberVO)data.get("user");
			if(!user.getUser_id().equals(userId)) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증 정보가 유효하지 않습니다.");
			}
			
			user.setCompany_name(company_name);	
        	Authentication authentication = memberService.convertBusiness(userId, company_name, user);
        	
        	TokenInfo token = jwtService.generateTokenFromAuthentication(authentication, (boolean)data.get("autoLogin"), (String)data.get("loginType"));
			
        	setJwtCookies(token, response, (boolean)data.get("autoLogin"));

    		return ResponseEntity.ok("기업회원 전환 성공");
	    } catch (JwtException e) {
	        log.error("JWT 처리 중 오류 발생", e);
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
	    } catch (AuthenticationException e) {
	        log.error("인증 객체 갱신 실패", e);
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
	    } catch (IllegalStateException e) {
	    	log.error("기업회원 전환 로직 실패", e);
	    	return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
	    } catch (Exception e) {
	        log.error("기업회원 전환 처리 중 알 수 없는 오류 발생", e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
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
	 * 회원 탈퇴
	 * 
	 * @return HTTP 응답 상태 코드
	 * 		- 200 OK: 회원 탈퇴 완료
	 *      - 500 INTERNAL SERVER ERROR: 서버 내부 오류 발생
	 */
	@DeleteMapping("/me")
    public ResponseEntity<Void> deleteAccount(HttpServletRequest request, HttpServletResponse response) {
        try {
        	// 1. 인증 플래그 확인
        	Boolean verifyStatus = (Boolean)request.getSession().getAttribute("verifyStatus");
        	if(verifyStatus == null || !verifyStatus) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        	
        	// 2. loginType 확인 (local, kakao, naver)
        	Cookie[] cookies = request.getCookies();
        	
        	String loginType = jwtService.extractLoginType(request, response, cookies);
        	
        	if (loginType == null) log.error("loginType 확인 실패 : (무시하고 회원 탈퇴 진행):");
        	
        	// 3. 소셜 연결 끊기
            try {
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
                			
                    	if(cookies != null)	deleteCookie(cookies, response, "kakaoAccessToken");
                		break;
                }
            } catch (Exception e) {
                log.warn("소셜 연결 끊기 실패 (무시하고 회원 탈퇴 진행): " + e.getMessage());
            }
            
            // 4. 로컬 회원 탈퇴 
        	String userId = jwtService.extractUserId(request, response, cookies);
        	
        	if(userId == null) userId = (String) request.getSession().getAttribute("userId");
            
        	AuthClearUtil.clearAuth(request, response);
        	
            if (userId == null) {
            	log.error("회원 탈퇴 오류: 사용자 아이디 없음.");
    	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    	    }
            
            memberService.deleteAccount(userId);
        	
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
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
	 * return:
	 * - 인증 성공 시: loginType, autoLogin, user 정보를 포함한 Map
	 * - 인증 실패 시: null
	 *
	 * @param request HttpServletRequest (쿠키와 세션 접근)
	 * @return Map<String, Object> 인증 데이터 또는 null
	 */
	private Map<String, Object> resolveAuthenticatedUser(HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> data = new HashMap<>();
		
		Cookie[] cookies = request.getCookies();
    	if(cookies == null) {
    		log.error("쿠키가 없습니다."); 
    		return null;
    	}
    	
    	String loginType = jwtService.extractLoginType(request, response, cookies);
    	boolean autoLogin = jwtService.extractAutoLogin(request, response, cookies);

		data.put("loginType", loginType);
		data.put("autoLogin", autoLogin);
		
    	String accessToken = jwtService.extractToken(cookies, "Authorization");
    	if (accessToken == null || accessToken.isEmpty()) {
    		log.error("인증 토큰이 없습니다.");
			return null;
		}

    	MemberVO user = jwtService.extractUserInfoFromToken(accessToken);
    	
    	if (user == null) {
    		AuthClearUtil.clearAuth(request, response);
    		
			log.error("유효하지 않은 사용자입니다.");
			return null;
		}
    	
    	data.put("user", user);
    	
    	return data;
	}
}
