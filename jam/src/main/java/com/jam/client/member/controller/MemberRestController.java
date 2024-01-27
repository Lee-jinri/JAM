package com.jam.client.member.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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

	@Autowired
	private BCryptPasswordEncoder encoder;

	@Autowired
	private JwtTokenProvider jwtTokenProvider;

	@Autowired
	private AuthenticationManager authenticationManager;
	

	/******************************************************
	 * 회원 가입
	 * 
	 * @param MemberVO member
	 * @return HTTP 상태코드
	 * @throws Exception
	 *****************************************************/
	@PostMapping(value = "/join", produces = "application/json")
	public ResponseEntity<String> join(@RequestBody MemberVO member , Model model, HttpServletRequest request) throws Exception {

		String user_id = member.getUser_id();
		
		log.info("join : " + user_id);
		String user_name = member.getUser_name();
		
		// 아이디, 닉네임 정규식
		String idLegExp = "^(?=.*[a-zA-Z])(?=.*\\d)[a-zA-Z\\d]{8,20}$";
		String nameLegExp = "^[a-zA-Z가-힣0-9]{2,11}$";
		
		if (!user_id.matches(idLegExp)) {
		    
		    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("user_id");
		}

		if (!user_name.matches(nameLegExp)) {
		    
		    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("user_name");
		}

		String rawPw = member.getUser_pw();// 인코딩 전 비밀번호
		String encodePw = encoder.encode(rawPw); // 비밀번호 인코딩
		
		member.setUser_pw(encodePw); // 인코딩된 비밀번호 member객체에 다시 저장

		try {
			memberService.join(member);
			
			String prevPage = (String) request.getSession().getAttribute("prevPage");

			return ResponseEntity.ok().header("prev-page", prevPage).body(null);
			
		}catch(Exception e) {
			
			return ResponseEntity.status(500).body("Internal Server Error: " + e.getMessage());
		}

	}
	
	/***************************************************************
	 * 로그인
	 * @param member 사용자가 입력한 아이디와 비밀번호
	 * @return HTTP 응답의 상태 코드, 이전 페이지 uri, JWT 토큰
	 ****************************************************************/
	@PostMapping(value = "/login-process", produces = MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8")
	@ResponseBody
	public ResponseEntity<String> login(@RequestBody Map<String, String> member, HttpServletRequest request, HttpServletResponse response) {
		try {
			String user_id = member.get("user_id");
			String user_pw = member.get("user_pw");

			// 사용자 이름과 비밀번호로 인증 객체 생성
			Authentication authentication = authenticationManager
					.authenticate(new UsernamePasswordAuthenticationToken(user_id, user_pw));

			// SecurityContextHolder에 현재 사용자의 정보를 설정합니다.
			SecurityContextHolder.getContext().setAuthentication(authentication);
	    
			// 사용자 닉네임 가져오기
			String user_name = memberService.getUserName(user_id);

			// JWT 토큰 생성
			TokenInfo token = jwtTokenProvider.generateToken(authentication, user_name);

			// refresh 토큰
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


			// 로그인 이전 페이지
			String prevPage = (String) request.getSession().getAttribute("prevPage");

			return ResponseEntity.ok().header("prev-page", prevPage).header("Authorization", token.getAccessToken())
					.body("success");

		} catch (AuthenticationException e) {
			/*유효하지 않은 로그인 정보*/
			log.error("Authentication failed: " + e.getMessage(), e);
			return ResponseEntity.status(401).body("Authentication failed: " + e.getMessage());
		} catch (Exception e) {
			log.error("An error occurred during login", e);
			return ResponseEntity.status(500).body("Internal Server Error: " + e.getMessage());
		}
	}
	


	/********************************
	 * 로그아웃
	 * @param accessToken 사용자의 jwt 토큰
	 * @return HTTP 응답의 상태 코드
	 ********************************/
	@GetMapping(value = "/logout")
	public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response,
			@RequestHeader(value = "Authorization", required = true) String accessToken) throws Exception {


		Authentication user = jwtTokenProvider.getAuthentication(accessToken);
		UserDetails userDetails = (UserDetails) user.getPrincipal();
		
		String user_id = userDetails.getUsername();
		try {
			memberService.deleteRefreshToken(user_id);
			return ResponseEntity.ok().body("success");
		}catch(Exception e) {
			return ResponseEntity.status(500).body("Internal Server Error: Refresh Token 삭제 중 오류 발생 : " + e.getMessage());
		}

	}

	/******************************************************
	 * jwt토큰을 이용해서 사용자의 정보를 가져오는 메서드
	 * @param accessToken 사용자의 jwt 토큰
	 * @return HTTP 응답의 상태코드, 사용자 아이디, 닉네임, 권한
	 *****************************************************/
	@GetMapping(value = "/getUserInfo", produces = MediaType.APPLICATION_JSON_VALUE)
	@CrossOrigin
	public ResponseEntity<MemberVO> getUserInfo(HttpServletResponse response, @RequestHeader(value = "Authorization", required = false) String accessToken) {

		if (!accessToken.equals("null")) {
			try {

				// 새로운 accessToken을 발급 받았는지 여부
				boolean isAccessTokenUpdated = false;
        	    
				// Access Token의 유효성을 확인
		        if (!jwtTokenProvider.validateToken(accessToken)) {
		        	
		        	try {
		        		// SecurityContextHolder에 저장된 사용자의 정보 
			        	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			        	
			        	UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		   
		        	    String user_id = userDetails.getUsername();
		        	    
		        	    String refreshToken = memberService.getRefreshToken(user_id);
		        	    
		        	    // refresh Token의 유효성 확인
		        	    if(!jwtTokenProvider.validateToken(refreshToken)) {
		        	    	
		        	    	String user_name = memberService.getUserName(user_id);
			        	    
		        	    	// 사용자 인증 불가(401) - localStorage의 Authorization 삭제
		        	    	if(user_name == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
			        	    
			        	    // 새로운 access Token과 refresh Token 발급
			        	    accessToken = generateToken(authentication, user_id, user_name);
			        	    
			        	    if(accessToken == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
			        	    
			        	    isAccessTokenUpdated = true;
		        	    }else {
		        	    	// 사용자 인증 불가(401) - localStorage의 Authorization 삭제 
		        	    	log.error("refreshToken is not validate");
		        	    	
		        	    	return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
		        	    }
		        	}catch (Exception e) {
		        		// 사용자 인증 불가(401) - localStorage의 Authorization 삭제 
		        		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
		        	}
		        	
		        }
		        
				Claims claim = jwtTokenProvider.getClaims(accessToken);
				
				String user_id = claim.get("sub", String.class);
		        String auth = claim.get("auth", String.class);
		        String username = claim.get("username", String.class);

		        MemberVO member = new MemberVO();
		        member.setUser_id(user_id);
		        member.setUser_name(username);
		        member.setRole(auth);
		        
				return isAccessTokenUpdated
				        ? ResponseEntity.ok()
				        	.header("Authorization", accessToken)
				        	.body(member)
				        : ResponseEntity.ok().body(member);
				
			}
			catch (Exception e) {
				log.error("사용자 정보를 가져올 수 없습니다. : " + e.getMessage());
				return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}

		return ResponseEntity.ok().body(new MemberVO());
	}


	/****************************************
	 * access Token이 만료됐을 때 refreshToken이 유효한지 확인하고 새로운 accessToken 발급
	 * @param authentication 
	 * @param user_id
	 * @param user_name
	 * @return accessToken
	 ***************************************/
	private String generateToken(Authentication authentication, String user_id, String user_name) {
		
		TokenInfo token = jwtTokenProvider.generateToken(authentication, user_name);
		String refreshToken = token.getRefreshToken();

		try {
			// refresh 토큰 저장
			memberService.addRefreshToken(user_id, refreshToken);
			
			return token.getAccessToken();
			
		} catch (Exception e) {
			
			log.error("Failed to add refresh token");
			return null;
		}
	}
	


	/**************************
	 * 아이디 중복 확인
	 * @param userId
	 * @return HTTP 응답 상태코드
	 **************************/
	@RequestMapping(value = "/userIdChk", method = RequestMethod.POST)
	public ResponseEntity<String> idChk(String userId) throws Exception {
		if(userId == null || userId == "") return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("user id is null.");

		String idLegExp =  "^(?=.*[a-zA-Z])(?=.*\\d)[a-zA-Z\\d]{8,20}$";
		if (!userId.matches(idLegExp)) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid user id.");
		
		try {
			int result = memberService.idCheck(userId);

			if(result != 0 )return ResponseEntity.status(HttpStatus.CONFLICT).body("The user id is already in use.");
			
			return new ResponseEntity<>(HttpStatus.OK);
				
		}catch(Exception e) {
			log.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("");
		}
		
	}

	/******************************
	 * 닉네임 중복 확인
	 * 
	 * @param user_name
	 * @return HTTP 응답 상태코드
	 ******************************/
	@RequestMapping(value = "/userNameChk", method = RequestMethod.POST)
	public ResponseEntity<String> nameChk(String user_name) throws Exception {

		if(user_name == null || user_name == "") return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("user name is null.");
		
		String nameLegExp = "^[a-zA-z가-힣0-9]{2,10}$";
		if (!user_name.matches(nameLegExp)) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid user name.");
		
		try {
			int result = memberService.nameCheck(user_name);

			if(result != 0 )return ResponseEntity.status(HttpStatus.CONFLICT).body("The user name is already in use.");
			
			return new ResponseEntity<>(HttpStatus.OK);
		}catch(Exception e) {
			log.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
		
	}

	/***********************************
	 * 핸드폰 번호 중복 확인
	 * @param phone
	 * @return HTTP 응답 상태코드
	 ***********************************/
	@RequestMapping(value = "/phoneChk", method = RequestMethod.POST)
	public ResponseEntity<String> phoneChk(String phone) throws Exception {

		if(phone == null || phone == "") return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("phone is null.");

		String phoneLegExp = "^01([016789])([0-9]{3,4})([0-9]{4})$";
		if (!phone.matches(phoneLegExp)) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid phone.");
		
		try {
			int result = memberService.phoneCheck(phone);
			
			log.info("phone : " +phone);
			log.info("result : " + result);
			if(result != 0)return ResponseEntity.status(HttpStatus.CONFLICT).body("The phone number is already in use.");
			
			return new ResponseEntity<>(HttpStatus.OK);
			
		}catch(Exception e) {
			log.error(e.getMessage());
			
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
		
	}

	/****************************
	 * 이메일 중복 확인
	 * 
	 * @param email
	 * @return HTTP 응답 상태코드
	 ****************************/
	@RequestMapping(value = "/emailChk", method = RequestMethod.POST)
	public ResponseEntity<String> emailChk(String email) throws Exception {

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
	

	/*************************************
	 * 아이디 찾기
	 * 
	 * @param String email 이메일 
	 * @param String phone 전화번호
	 * @return 회원ID
	 *****************************/
	@RequestMapping(value = "/findId", method = RequestMethod.POST)
	public String findId(@RequestParam("email") String email, @RequestParam("phone") String phone) throws Exception {

		MemberVO result = memberService.FindId(email, phone);

		if (result != null)
			return result.getUser_id();
		else
			return "";

	}

	/*********************************
	 * 사용자 정보 확인 후 임시 비밀번호 발급
	 * 
	 * @param String user_id
	 * @param String email
	 * @param String phone
	 * @return HTTP 응답 상태코드
	 *********************************/
	@PostMapping("/findPw")
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
			
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
		
	}


	/********************************
	 * 카카오 로그인
	 * 
	 * @param code 인가코드
	 * @return 메인 페이지 or 로그인 이전 페이지
	 *********************************/
	@PostMapping(value = "/kakao_login")
	public ResponseEntity<String> kakaoLogin(@RequestBody MemberVO member, HttpServletRequest request, HttpServletResponse response) {
		
		try {
			
			member.setUser_pw(encoder.encode("kakaoLoginPassword"));
			int kakaoUser = memberService.socialLoginOrRegister(member);
	
			if(kakaoUser != 1)return ResponseEntity.internalServerError().body("Unable to complete membership");
			
			
			log.info("member : " + member);
			
			// 사용자 이름과 비밀번호로 인증 객체 생성
			Authentication authentication = authenticationManager
					.authenticate(new UsernamePasswordAuthenticationToken(member.getUser_id(), "kakaoLoginPassword"));

			// JWT 토큰 생성
			TokenInfo token = jwtTokenProvider.generateToken(authentication, member.getUser_name());
			
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


			// 로그인 이전 페이지
			String prevPage = (String) request.getSession().getAttribute("prevPage");

			return ResponseEntity.ok().header("prev-page", prevPage).header("Authorization", token.getAccessToken())
					.body("success");

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
	 * 
	 * @param code    인가코드
	 * @param state
	 * @param request
	 * @return 메인 페이지 or 로그인 이전 페이지
	 ****************************************/
	@PostMapping(value = "/naver_login")
	public ResponseEntity<String> naverLogin(@RequestBody MemberVO member, HttpServletRequest request) {

		try {
			
			member.setUser_pw(encoder.encode("naverLoginPassword"));
			
			int naverUser = memberService.socialLoginOrRegister(member);

			if(naverUser != 1)return ResponseEntity.internalServerError().body("Unable to complete membership");
			
			// 사용자 이름과 비밀번호로 인증 객체 생성
			Authentication authentication = authenticationManager
					.authenticate(new UsernamePasswordAuthenticationToken(member.getUser_id(), "naverLoginPassword"));

			// JWT 토큰 생성
			TokenInfo token = jwtTokenProvider.generateToken(authentication, member.getUser_name());
			
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
			
			// 로그인 이전 페이지
			String prevPage = (String) request.getSession().getAttribute("prevPage");

			return ResponseEntity.ok().header("prev-page", prevPage).header("Authorization", token.getAccessToken())
					.body("success");
			
		} catch (AuthenticationException e) {
			log.error("Authentication failed: " + e.getMessage(), e);
			return ResponseEntity.internalServerError().body(e.getMessage());
		} catch (Exception e) {
			log.error("An error occurred during login", e);
			return ResponseEntity.internalServerError().body(e.getMessage());
		}
	}
	
	/*****************************************
	 * 마이페이지 - 사용자 정보 조회
	 * @param user_id 조회할 사용자의 아이디 
	 * @return HTTP 응답의 상태코드, 사용자 아이디, 닉네임, 주소, 전화번호, 소셜로그인 여부 
	 *******************************************/
	@GetMapping(value = "/account/{user_id}" , produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<MemberVO> getAccount(@PathVariable(value = "user_id") String user_id){
		
		if(user_id == null || user_id == "") {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
		}
		
		try {
			MemberVO account = memberService.account(user_id);
			
			return ResponseEntity.ok().body(account);
		}catch(Exception e) {
			log.error(e.getMessage());
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/************************************
	 * 전화번호 변경
	 * 
	 * @param String phone 변경할 전화번호
	 * @return HTTP 응답 상태코드
	 *******************************/
	// 이거 responseBody아니였음
	@RequestMapping(value = "/updatePhone", method = RequestMethod.POST)
	public ResponseEntity<String> updatePhone(@RequestBody MemberVO member) throws Exception {

		if(member.getUser_id() == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("user id is required.");
		if(member.getPhone() == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("phone is required.");
		
		String phoneLegExp = "^01([016789])([0-9]{3,4})([0-9]{4})$";
		if (!member.getPhone().matches(phoneLegExp)) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid phone.");
		
		try {
			memberService.updatePhone(member);
			
			return new ResponseEntity<>(HttpStatus.OK);
		}catch(Exception e) {
			log.error(e.getMessage());
			
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
		
	}

	/*********************************
	 * 비밀번호 확인
	 * 
	 * @param user_pw 사용자 비밀번호
	 * @return HTTP 응답 상태코드
	 ********************************/
	@RequestMapping(value = "/pwConfirm", method = RequestMethod.POST)
	public ResponseEntity<String> pwConfirm(String user_id, String user_pw, HttpServletRequest request) throws Exception {

		// 비밀번호 유효성 확인
		MemberVO m_vo = new MemberVO();
		m_vo.setUser_id(user_id);

		String encodePw = memberService.pwConfirm(m_vo);

		if (encoder.matches(user_pw, encodePw)) { // 비밀번호 일치여부 판단
			return new ResponseEntity<>(HttpStatus.OK);
		} else {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Wrong user password.");
		}
	}

	/***********************************
	 * 비밀번호 변경
	 * 
	 * @param user_pw 변경할 비밀번호
	 * @return HTTP 응답 상태코드
	 ***********************************/
	@RequestMapping(value = "/updatePw", method = RequestMethod.POST)
	public ResponseEntity<String> updatePw(@RequestBody MemberVO member) throws Exception {

		// 아이디, 비밀번호 유효성 확인
		if(member.getUser_id() == null) {
			log.error("user_id is required.");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("user id is required.");
		}
		
		String pwLegExp = "^(?=.*[a-zA-Z])(?=.*\\d)[a-zA-Z\\d]{8,20}$";
		
		if (!member.getUser_pw().matches(pwLegExp)) {
		    
		    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid password.");
		}
		
		if(member.getUser_pw() == null) {
			log.error("user_pw is required.");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("user password is required.");
		}
		
		
		try {
			String encodePw = encoder.encode(member.getUser_pw()); // 비밀번호 인코딩

			memberService.updatePw(member.getUser_id(), encodePw);
				
			return new ResponseEntity<>(HttpStatus.OK);
				
		}catch(Exception e) {
			log.error(e.getMessage());
			
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/***********************************
	 * 주소 변경
	 * 
	 * @param user_id 사용자 id
	 * @param address 변경할 주소
	 * @return HTTP 응답 상태 코드
	 ***********************************/
	@PostMapping("/updateAddress")
	public ResponseEntity<String> updateAddress(@RequestParam(value = "user_id") String user_id, String address) throws Exception {

		if(user_id == null || user_id.isEmpty()) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("user id is required.");
		if(address == null || address.isEmpty()) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("address is required.");
		
		try {
			MemberVO m_vo = new MemberVO();
			
			m_vo.setUser_id(user_id);
			m_vo.setAddress(address);
			
			memberService.updateAddress(m_vo);
			
			return new ResponseEntity<>(HttpStatus.OK);
		}catch(Exception e) {
			log.error(e.getMessage(),e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}

	/******************************************
	 * 회원 탈퇴
	 * @param user_id 탈퇴할 회원의 아이디
	 * @return HTTP 응답 상태 코드
	 *****************************************/
	
	@PostMapping("/withDraw")
    public ResponseEntity<Void> withDraw(@RequestParam(value = "user_id") String user_id) {
        try {
        	// 회원 정보 삭제
            memberService.withDraw(user_id);

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
