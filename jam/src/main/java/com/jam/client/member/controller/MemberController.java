package com.jam.client.member.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.jam.client.community.vo.CommunityVO;
import com.jam.client.fleaMarket.vo.FleaMarketVO;
import com.jam.client.job.vo.JobVO;
import com.jam.client.member.service.MemberService;
import com.jam.client.member.vo.MemberVO;
import com.jam.client.roomRental.vo.RoomRentalVO;
import com.jam.common.vo.PageDTO;
import com.jam.security.JwtTokenProvider;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j;

@Controller
@RequestMapping("/member")
@AllArgsConstructor
@Log4j
public class MemberController {

	@Autowired
	private MemberService memberService;


	/**********************************
	 * 네이버 로그인 페이지로 이동하는 메서드
	 ********************************/
	@RequestMapping(value = "/naver_login", method = RequestMethod.GET)
	public String naverLogin() {
		return "member/naverLogin";
	}
	
	/**********************************
	 * 카카오 로그인 페이지로 이동하는 메서드
	 ********************************/
	@RequestMapping(value = "/kakao_login", method = RequestMethod.GET)
	public String kakakoLogin() {
		return "member/kakaoLogin";
	}
	
	/******************************
	 * 회원 가입 페이지로 이동하는 메서드
	 *  회원 가입 성공 시 회원 가입 이전 페이지로 이동하기 위해 이전 페이지 uri를 세션에 저장
	 * @return 회원가입 페이지
	 ******************************/
	@RequestMapping(value = "/join", method = RequestMethod.GET)
	public String joinPage(HttpServletRequest request) {
		
		String uri = request.getHeader("Referer");

		if (uri != null && !uri.contains("/login")) {
			request.getSession().setAttribute("prevPage", uri);
		}

		return "member/join";
	}
	


	/****************************
	 * 로그인 페이지로 이동하는 메서드
	 * 로그인 성공 시 로그인 이전 페이지로 이동하기 위해 이전 페이지 uri를 세션에 저장
	 * @param request
	 ****************************/
	@GetMapping("/login")
	public void loginPage(HttpServletRequest request) {
		String uri = request.getHeader("Referer");

		if (uri != null && !uri.contains("/login")) {
			request.getSession().setAttribute("prevPage", uri);
		}
	}



	/****************************************************
	 * 마이페이지 - 회원 정보 페이지로 이동하는 메서드
	 * @return 마이페이지 - 회원 정보 페이지 / 로그인 되어 있지 않으면 로그인 페이지로 이동
	 ****************************************************/
	@RequestMapping(value = "/account", method = RequestMethod.GET)
	public String mypageAccount(@RequestParam("user_id") String user_id, Model model) throws Exception {

		if (user_id != null && user_id != "") {
			
			model.addAttribute("user_id", user_id);
			
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
	 ***********************************/
	@RequestMapping(value = "/joinFind", method = RequestMethod.GET)
	public String findUserPage() {
		
		return "member/joinFind";
	}

}
