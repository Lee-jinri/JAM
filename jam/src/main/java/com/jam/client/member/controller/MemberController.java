package com.jam.client.member.controller;

import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.jam.client.community.vo.CommunityVO;
import com.jam.client.fleaMarket.vo.FleaMarketVO;
import com.jam.client.job.vo.JobVO;
import com.jam.client.member.service.MemberService;
import com.jam.client.member.vo.MemberVO;
import com.jam.client.roomRental.vo.RoomRentalVO;
import com.jam.common.vo.PageDTO;
import com.jam.security.JwtTokenProvider;
import com.jam.security.TokenInfo;

import io.jsonwebtoken.Claims;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j;

@Controller
@RequestMapping("/member")
@AllArgsConstructor
@Log4j
public class MemberController {

	@Autowired
	private MemberService memberService;

	@Autowired
	private BCryptPasswordEncoder encoder;

	@Autowired
	private JwtTokenProvider jwtTokenProvider;

	@Autowired
	private AuthenticationManager authenticationManager;

	
	/******************************
	 * @return 회원가입 페이지
	 ******************************/
	@RequestMapping(value = "/join", method = RequestMethod.GET)
	public ModelAndView joinPage(Model model) {

		ModelAndView mav = new ModelAndView();
		mav.setViewName("member/join");
		return mav;
	}

	/*******************************************************************
	 * 로그인 성공 시 로그인 이전 페이지로 이동하기 위해 이전 페이지 uri를 세션에 저장
	 *******************************************************************/
	@GetMapping("/login")
	public void loginPage(HttpServletRequest request) {
		
		String uri = request.getHeader("Referer");

		if (uri != null && !uri.contains("/login")) {
			request.getSession().setAttribute("prevPage", uri);
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

			// JWT Access Token을 쿠키에 저장
			/*
			 * Cookie accessToken = new Cookie("access_token", token.getAccessToken());
			 * accessToken.setPath("/"); // 쿠키 경로 설정 accessToken.setHttpOnly(true); // 클라이언트
			 * 스크립트에서 접근 불가능하도록 설정 (XSS 방지) accessToken.setSecure(true); // HTTPS 연결에서만 전송
			 * accessToken.setMaxAge(3600); // 토큰 만료 시간 설정 (예: 1 시간)
			 * 
			 * response.addCookie(accessToken);
			 */

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
	@ResponseBody
	public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response,
			@RequestHeader(value = "Authorization", required = true) String accessToken) throws Exception {

		/*
		 * Cookie[] cookies = request.getCookies(); String accessToken = null;
		 * 
		 * if (cookies != null) { for (Cookie cookie : cookies) { if
		 * ("access_token".equals(cookie.getName())) { accessToken = cookie.getValue();
		 * log.info("쿠키 값 " + cookie.getValue()); break; // 원하는 쿠키를 찾았으면 루프 종료 } } }
		 * Cookie cookie = new Cookie("access_token", null); cookie.setMaxAge(0); // 쿠키를
		 * 만료 response.addCookie(cookie);
		 */

		Authentication user = jwtTokenProvider.getAuthentication(accessToken);
		UserDetails userDetails = (UserDetails) user.getPrincipal();
		String user_id = userDetails.getUsername();
		int result = memberService.deleteRefreshToken(user_id);

		if (result != 1)
			return ResponseEntity.status(500).body("Internal Server Error: Refresh Token 삭제 중 오류 발생");
		else
			return ResponseEntity.ok().body("success");

	}

	/******************************************************
	 * jwt토큰을 이용해서 사용자의 정보를 가져오는 메서드
	 * @param accessToken 사용자의 jwt 토큰
	 * @return HTTP 응답의 상태코드, 사용자 아이디, 닉네임, 권한
	 *****************************************************/
	@GetMapping(value = "/getUserInfo", produces = "text/html;charset=UTF-8")
	@ResponseBody
	@CrossOrigin
	public ResponseEntity<String> getUserInfo(HttpServletResponse response, @RequestHeader(value = "Authorization", required = false) String accessToken) {

		if (accessToken != null) {
			try {
				Claims claim = jwtTokenProvider.getClaims(accessToken);
				
				String user_id = claim.get("sub", String.class);
		        String auth = claim.get("auth", String.class);
		        String username = claim.get("username", String.class);


				HttpHeaders headers = new HttpHeaders();
				headers.add("Content-Type", "text/html; charset=UTF-8");
				headers.add("user_id", user_id);
				headers.add("auth", auth);

				String responseBody = username;
				
				return new ResponseEntity<>(responseBody, headers, HttpStatus.OK);
			}
			catch (Exception e) {
				log.error("사용자 정보를 가져올 수 없습니다. : " + e.getMessage());
			}
		}

		return ResponseEntity.ok().body("");
	}


	/******************************************************
	 * 회원 가입
	 * 
	 * @param MemberVO member
	 * @return 성공 시 회원가입 완료 페이지 / 실패 시 회원가입 페이지
	 * @throws Exception
	 *****************************************************/
	@RequestMapping(value = "join", method = RequestMethod.POST)
	public String join(MemberVO member, Model model, HttpServletResponse response) throws Exception {

		int result = 0;

		String rawPw = ""; // 인코딩 전 비밀번호
		String encodePw = ""; // 인코딩 후 비밀번호

		rawPw = member.getUser_pw(); // 비밀번호 데이터 얻음
		encodePw = encoder.encode(rawPw); // 비밀번호 인코딩
		member.setUser_pw(encodePw); // 인코딩된 비밀번호 member객체에 다시 저장

		result = memberService.join(member);

		if (result == 1) {
			try {
				response.setContentType("text/html; charset=utf-8");
				PrintWriter writer = response.getWriter();
				writer.write(
						"<script>alert('" + member.getUser_name() + "님 환영합니다! \\n JAM에서 당신의 음악 친구를 찾아보세요.');</script>");
				writer.write("<script>location.href='/member/login';</script>");
				writer.flush();
				writer.close();
				return null;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		} else {
			return "member/joinPage";
		}

	}

	/**************************
	 * 아이디 중복 확인
	 * 
	 * @param userId
	 * @return 아이디 중복 여부
	 **************************/
	@RequestMapping(value = "/memberIdChk", method = RequestMethod.POST)
	@ResponseBody
	public String memberIdChkPOST(String userId) throws Exception {

		int result = memberService.idCheck(userId);

		if (result != 0) {
			return "fail"; // 중복 아이디가 존재
		} else {
			return "success"; // 중복 아이디 x
		}
	}

	/******************************
	 * 닉네임 중복 확인
	 * 
	 * @param user_name
	 * @return 닉네임 중복 여부
	 ******************************/
	@RequestMapping(value = "/memberNameChk", method = RequestMethod.POST)
	@ResponseBody
	public String memberNameChkPOST(String user_name) throws Exception {

		int result = memberService.nameCheck(user_name);

		if (result != 0) {
			return "fail"; // 중복 닉네임이 존재
		} else {
			return "success"; // 중복 닉네임 X
		}
	}

	/***********************************
	 * 핸드폰 번호 중복 확인
	 * 
	 * @param phone
	 * @return 핸드폰 번호 중복 여부
	 ***********************************/
	@RequestMapping(value = "/memberPhoneChk", method = RequestMethod.POST)
	@ResponseBody
	public String memberPhoneChkPOST(String phone) throws Exception {

		int result = memberService.phoneCheck(phone);
		log.info("핸드폰 번호 " + phone);
		log.info("핸드폰 중복 확인 : " + result);
		if (result != 0) {
			return "fail"; // 중복 핸드폰 번호 존재
		} else {
			return "success"; // 중복 핸드폰 번호 X
		}
	}

	/****************************
	 * 이메일 중복 확인
	 * 
	 * @param email
	 * @return 이메일 중복 여부
	 ****************************/
	@RequestMapping(value = "/memberEmailChk", method = RequestMethod.POST)
	@ResponseBody
	public String memberEmailChkPOST(String email) throws Exception {

		int result = memberService.emailCheck(email);

		if (result != 0) {
			return "fail";
		} else {
			return "success";
		}
	}

	/****************************************************
	 * 마이페이지 - 회원 정보
	 * @return 마이페이지 - 회원 정보 페이지 또는 로그인 페이지
	 ****************************************************/
	@RequestMapping(value = "/account", method = RequestMethod.GET)
	public String mypageUpdateForm(@RequestParam("user_id") String user_id, Model model) throws Exception {

		if (user_id != null && user_id != "") {
			MemberVO account = memberService.account(user_id);
			model.addAttribute("account", account);

			log.info(account.getSocial_login());
			return "mypage/account";
		}

		return "member/login";
	}

	/***********************************************
	 * 사용자가 작성한 커뮤니티 글
	 * @param CommunityVO com_vo
	 * @return 마이페이지 - 커뮤니티 작성 글 또는 로그인 페이지
	 ***********************************************/
	@RequestMapping(value = "/comMyWrite", method = RequestMethod.GET)
	public String comMyWrite(@RequestParam("user_id") String user_id,
			@RequestParam(value = "keyword", required = false) String keyword,
			@RequestParam(value = "search", required = false) String search, Model model,
			@ModelAttribute CommunityVO com_vo) throws Exception {

		String url = "";
		if (user_id != null && user_id != "") {
			url = "mypage/comMyWrite";

			com_vo.setUser_id(user_id);

			if (keyword != null && search != null) {
				com_vo.setKeyword(keyword);
				com_vo.setSearch(search);
			}

			List<CommunityVO> comMyWrite = memberService.comMyWrite(com_vo);
			model.addAttribute("comMyWrite", comMyWrite);

			int total = memberService.myComListCnt(com_vo);
			model.addAttribute("pageMaker", new PageDTO(com_vo, total));
		} else {
			url = "member/login";
		}

		return url;

	}

	/**********************************************************
	 * 사용자가 작성한 중고악기 글
	 * @param FleaMarketVO flea_vo
	 * @return 마이페이지 - 중고악기 작성 글 또는 로그인 페이지
	 **********************************************************/
	@RequestMapping(value = "/fleaMyWrite", method = RequestMethod.GET)
	public String fleaMyWrite(@RequestParam("user_id") String user_id,
			@RequestParam(value = "keyword", required = false) String keyword,
			@RequestParam(value = "search", required = false) String search, Model model,
			@ModelAttribute FleaMarketVO flea_vo) throws Exception {

		String url = "";
		if (user_id != null && user_id != "") {
			url = "mypage/fleaMyWrite";

			flea_vo.setUser_id(user_id);

			if (keyword != null && search != null) {
				flea_vo.setKeyword(keyword);
				flea_vo.setSearch(search);
			}

			List<FleaMarketVO> fleaMyWrite = memberService.fleaMyWrite(flea_vo);
			model.addAttribute("fleaMyWrite", fleaMyWrite);

			int total = memberService.myFleaListCnt(flea_vo);
			model.addAttribute("pageMaker", new PageDTO(flea_vo, total));
		} else {
			url = "member/login";
		}
		return url;

	}

	/*******************************************************
	 * 사용자가 작성한 구인구직 글
	 * @param JobVO jov_vo
	 * @return 마이페이지 - 구인구직 작성 글 또는 로그인 페이지
	 *******************************************************/
	@RequestMapping(value = "/jobMyWrite", method = RequestMethod.GET)
	public String jobMyWrite(@RequestParam("user_id") String user_id,
			@RequestParam(value = "keyword", required = false) String keyword,
			@RequestParam(value = "search", required = false) String search, Model model, @ModelAttribute JobVO jov_vo)
			throws Exception {

		String url = "";
		if (user_id != null && user_id != "") {
			url = "mypage/jobMyWrite";

			jov_vo.setUser_id(user_id);

			if (keyword != null && search != null) {
				jov_vo.setKeyword(keyword);
				jov_vo.setSearch(search);
			}

			List<JobVO> jobMyWrite = memberService.jobMyWrite(jov_vo);
			model.addAttribute("jobMyWrite", jobMyWrite);

			int total = memberService.myJobListCnt(jov_vo);
			model.addAttribute("pageMaker", new PageDTO(jov_vo, total));
		} else {
			url = "member/login";
		}
		return url;

	}

	/******************************************************
	 * 사용자가 작성한 합주실 글
	 * @param RoomRentalVO room_vo
	 * @return 마이페이지 - 합주실 작성 글 또는 로그인 페이지
	 ******************************************************/
	@RequestMapping(value = "/roomMyWrite", method = RequestMethod.GET)
	public String roomMyWrite(@RequestParam("user_id") String user_id,
			@RequestParam(value = "keyword", required = false) String keyword,
			@RequestParam(value = "search", required = false) String search, Model model,
			@ModelAttribute RoomRentalVO room_vo) throws Exception {

		String url = "";
		if (user_id != null && user_id != "") {
			url = "mypage/roomMyWrite";

			room_vo.setUser_id(user_id);

			if (keyword != null && search != null) {
				room_vo.setKeyword(keyword);
				room_vo.setSearch(search);
			}

			List<RoomRentalVO> roomMyWrite = memberService.roomMyWrite(room_vo);
			model.addAttribute("roomMyWrite", roomMyWrite);

			int total = memberService.myRoomListCnt(room_vo);
			model.addAttribute("pageMaker", new PageDTO(room_vo, total));
		} else {
			url = "member/login";
		}
		return url;

	}

	/***********************************
	 * @return 아이디/비밀번호 찾기 페이지
	 * @throws Exception
	 ***********************************/
	@RequestMapping(value = "/joinFind", method = RequestMethod.GET)
	public ModelAndView findIdPage() throws Exception {
		ModelAndView mav = new ModelAndView();
		mav.setViewName("member/joinFind");

		return mav;
	}

	/*************************************
	 * 아이디 찾기
	 * 
	 * @param String email 이메일 
	 * @param String phone 전화번호
	 * @return 회원ID
	 * @throws Exception
	 *****************************/
	@RequestMapping(value = "/memberFindId", method = RequestMethod.POST)
	@ResponseBody
	public String memberFindIdPOST(@RequestParam("email") String email, @RequestParam("phone") String phone,
			Model model) throws Exception {

		MemberVO result = memberService.FindId(email, phone);

		if (result != null)
			return result.getUser_id();
		else
			return "";

	}

	/*********************************
	 * 비밀번호 찾기
	 * 
	 * @param String user_id
	 * @param String email
	 * @param String phone
	 * @return 비밀번호 찾기 결과
	 * @throws Exception
	 *********************************/
	@RequestMapping(value = "/memberFindPw", method = RequestMethod.POST)
	@ResponseBody
	public String memberFindPwPOST(String user_id, String email, String phone) throws Exception {

		// 입력한 정보와 일치하는 사용자가 있는지 확인
		int user = memberService.FindPw(user_id, email, phone);

		if (user == 1) {

			int result = memberService.UpdatePw(user_id, email);

			if (result == 1) {
				return "success";
			} else {
				return "fail";
			}
		} else {
			return "notFound";
		}

	}

	/**************************************
	 * 카카오 소셜 로그인 권한 요청
	 * 
	 * @param response
	 * @return 카카오 인가 코드 요청 url
	 **************************************/
	@RequestMapping(value = "/kakao_oauth", method = RequestMethod.GET)
	public String kakaoOauth(HttpServletResponse response) {

		StringBuffer url = new StringBuffer();
		url.append("https://kauth.kakao.com/oauth/authorize?");
		url.append("client_id=5e18a572e50f01203a5cf31c55ec073d");
		url.append("&redirect_uri=http://localhost:8080/member/kakao_login");
		url.append("&response_type=code");

		return "redirect:" + url;
	}

	/********************************
	 * 카카오 로그인
	 * 
	 * @param code 인가코드
	 * @return 메인 페이지 or 로그인 이전 페이지
	 *********************************/
	@RequestMapping(value = "/kakao_login", method = RequestMethod.GET)
	public String kakaoCallback(@RequestParam String code, HttpServletRequest request) {

		// 인가코드 보내서 토큰 받기
		String access_Token = memberService.getAccessToken(code);
		// 토큰을 보내서 사용자 정보 받기
		MemberVO member = memberService.getUserInfo(access_Token);
		
		try {
			String user_id = member.getUser_id();
			String user_pw = "kakaoLoginPassword";
			String user_name = member.getUser_name();
			
			// 사용자 이름과 비밀번호로 인증 객체 생성
			Authentication authentication = authenticationManager
					.authenticate(new UsernamePasswordAuthenticationToken(user_id, user_pw));

			// JWT 토큰 생성
			TokenInfo token = jwtTokenProvider.generateToken(authentication, user_name);
			String access_token = token.getAccessToken();
			
			// refresh 토큰
			String refreshToken = token.getRefreshToken();

			try {
				// refresh 토큰 db에 저장
				int addRefreshToken = memberService.addRefreshToken(user_id, refreshToken);
				if (addRefreshToken != 1) {
					log.info("Internal Server Error: Refresh Token 저장 중 오류 발생");
					return "redirect:/member/login?error=error";
				}
			} catch (Exception e) {
				log.error("Failed to add refresh token", e);
				log.info(e.getMessage());
				return "redirect:/member/login?error=error";
			}
			
			// 로그인 이전 페이지
			String prevPage = (String) request.getSession().getAttribute("prevPage");

			if (prevPage != null && !prevPage.equals("")) {
				request.getSession().removeAttribute("prevPage");
				// 회원가입 - 로그인으로 넘어온 경우 "/"로 redirect
				if (prevPage.contains("/member/join")) {
					return "redirect:/" + "?token=" + access_token;
				} else {
					return "redirect:" + prevPage + "?token=" + access_token;
				}
			} else
				return "redirect:/" + "?token=" + access_token;

		} catch (AuthenticationException e) {
			// 
			log.error("Authentication failed: " + e.getMessage(), e);
			return "redirect:/member/login?error=error";
		} catch (Exception e) {
			log.error("An error occurred during login", e);
			return "redirect:/member/login?error=error";
		}


	}

	/**************************************
	 * 네이버 소셜 로그인 권한 요청
	 * 
	 * @param response
	 * @return 네이버 인가 코드 요청 url
	 **************************************/
	@RequestMapping(value = "/naver_oauth", method = RequestMethod.GET)
	public String naverOauth(HttpServletResponse response, HttpServletRequest request) {

		SecureRandom random = new SecureRandom();
		String state = new BigInteger(130, random).toString(32);

		// 네이버 로그인 연동 URL 생성
		StringBuffer url = new StringBuffer();
		url.append("https://nid.naver.com/oauth2.0/authorize?");
		url.append("client_id=TVknnflYlinxp0rriL8N");
		url.append("&response_type=code");
		url.append("&redirect_uri=http://localhost:8080/member/naver_login");
		// state : 사이트 간 요청 위조(cross-site request forgery) 공격을 방지하기 위해 애플리케이션에서 생성한 상태
		// 토큰값
		url.append("&state=" + state);

		return "redirect:" + url;
	}

	/*****************************************
	 * 네이버 로그인
	 * 
	 * @param code    인가코드
	 * @param state
	 * @param request
	 * @return 메인 페이지 or 로그인 이전 페이지
	 ****************************************/
	@RequestMapping(value = "/naver_login", method = RequestMethod.GET)
	public String naverLogin(@RequestParam(value = "code") String code, @RequestParam(value = "state") String state,
			HttpServletRequest request) {

		log.info("naver login " + code);
		// 토큰 받기
		String access_Token = memberService.getNaverToken(code);
		// 토큰을 보내서 사용자 정보 받기
		MemberVO member = memberService.getNaverInfo(access_Token);
		
		try {
			String user_id = member.getUser_id();
			String user_pw = "naverLoginPassword";
			String user_name = member.getUser_name();

			// 사용자 이름과 비밀번호로 인증 객체 생성
			Authentication authentication = authenticationManager
					.authenticate(new UsernamePasswordAuthenticationToken(user_id, user_pw));

			// JWT 토큰 생성
			TokenInfo token = jwtTokenProvider.generateToken(authentication, user_name);
			String access_token = token.getAccessToken();
			
			// refresh 토큰
			String refreshToken = token.getRefreshToken();

			try {
				// refresh 토큰 db에 저장
				int addRefreshToken = memberService.addRefreshToken(user_id, refreshToken);
				if (addRefreshToken != 1) {
					log.info("Internal Server Error: Refresh Token 저장 중 오류 발생");
					return "redirect:/member/login?error=error";
				}
			} catch (Exception e) {
				log.error("Failed to add refresh token", e);
				log.info(e.getMessage());
				return "redirect:/member/login?error=error";
			}
			
			// 로그인 이전 페이지
			String prevPage = (String) request.getSession().getAttribute("prevPage");

			log.info("네이버 로그인 이전페이지 " + prevPage);
			if (prevPage != null && !prevPage.equals("")) {
				request.getSession().removeAttribute("prevPage");
				// 회원가입 - 로그인으로 넘어온 경우 "/"로 redirect
				if (prevPage.contains("/member/join")) {
					return "redirect:/" + "?token=" + access_token;
				} else {
					return "redirect:" + prevPage + "?token=" + access_token;
				}
			} else
				return "redirect:/" + "?token=" + access_token;

		} catch (AuthenticationException e) {
			log.error("Authentication failed: " + e.getMessage(), e);
			return "redirect:/member/login?error=error";
		} catch (Exception e) {
			log.error("An error occurred during login", e);
			return "redirect:/member/login?error=error";
		}


	}

	/************************************
	 * 전화번호 변경
	 * 
	 * @param String phone 변경할 전화번호
	 * @return 전화번호 변경 결과와 마이페이지
	 * @throws Exception
	 *******************************/
	@RequestMapping(value = "/phoneModi", method = RequestMethod.POST)
	public String phoneModi(HttpServletRequest request, Model model, @RequestParam(value = "user_id") String user_id,
			@RequestParam("phone") String phone, RedirectAttributes rttr) throws Exception {

		log.info("phoneModi user_id : " + user_id + " phone : " + phone);

		int result = memberService.phoneModi(user_id, phone);

		log.info("phoneModi result : " + result);
		if (result == 1) {
			rttr.addFlashAttribute("phone_result", "success");
		} else {
			rttr.addFlashAttribute("phone_result", "fail");
		}

		return "redirect:/member/account?user_id=" + user_id;
	}

	/*********************************
	 * 비밀번호 확인
	 * 
	 * @param user_pw 사용자 비밀번호
	 * @return 비밀번호 일치 여부
	 * @throws Exception
	 ********************************/
	@RequestMapping(value = "/pwConfirm", method = RequestMethod.POST)
	@ResponseBody
	public String pwConfirm(String user_id, String user_pw, HttpServletRequest request) throws Exception {

		MemberVO m_vo = new MemberVO();
		m_vo.setUser_id(user_id);

		String encodePw = memberService.pwConfirm(m_vo);

		if (true == encoder.matches(user_pw, encodePw)) { // 비밀번호 일치여부 판단
			return "success"; // 비밀번호 일치
		} else {
			return "fail"; // 비밀번호 일치하지 않음
		}
	}

	/***********************************
	 * 비밀번호 변경
	 * 
	 * @param user_pw 변경 할 비밀번호
	 * @return 비밀번호 변경 결과 + 마이페이지
	 * @throws Exception
	 ***********************************/
	@RequestMapping(value = "/pwModi", method = RequestMethod.POST)
	public String pwModi(Model model, @RequestParam("user_id") String user_id, @RequestParam String user_pw,
			RedirectAttributes rttr) throws Exception {

		String url = "";

		if (user_id != null) {

			MemberVO m_vo = new MemberVO();

			String encodePw = encoder.encode(user_pw); // 비밀번호 인코딩

			m_vo.setUser_id(user_id);
			m_vo.setUser_pw(encodePw);

			int result = memberService.pwModi(m_vo);

			if (result == 1) {
				rttr.addFlashAttribute("pw_result", "success");
			} else {
				rttr.addFlashAttribute("pw_result", "fail");
			}

			return "redirect:/member/account?user_id=" + user_id;
		} else {
			url = "/member/login";
		}
		return url;
	}

	/***********************************
	 * 주소 변경
	 * 
	 * @param user_id 사용자 id
	 * @param address 변경 할 주소
	 * @return HTTP 응답 상태 코드
	 * @throws Exception
	 ***********************************/
	@PostMapping("/addressModi")
	@ResponseBody
	public ResponseEntity<Void> addressModi(@RequestParam(value = "user_id") String user_id, Model model, String address, RedirectAttributes rttr) throws Exception {

		try {
			MemberVO m_vo = new MemberVO();
			
			m_vo.setUser_id(user_id);
			m_vo.setAddress(address);
			
			memberService.addressModi(m_vo);
			
			return ResponseEntity.ok().build();
		}catch(Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	/******************************************
	 * 회원 탈퇴
	 * @param user_id 탈퇴할 회원의 아이디
	 * @return HTTP 응답 상태 코드
	 *****************************************/
	
	@PostMapping("/withDraw")
    @ResponseBody
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
