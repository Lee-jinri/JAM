package com.jam.client.member.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
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

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j;

@Controller
@RequestMapping("/member")
@AllArgsConstructor
@Log4j
public class MemberController {

	@Autowired
	private MemberService memberService;

	// 비밀번호 암호화
//	@Autowired(required = true)
//	private BCryptPasswordEncoder encoder;
//	

	/*******************
	 * 회원가입 페이지
	 ********************/
	@RequestMapping(value = "/join", method = RequestMethod.GET)
	public ModelAndView joinPage(Model model) {

		ModelAndView mav = new ModelAndView();
		mav.setViewName("member/join");
		return mav;
	}

	/*******************
	 * 로그인 페이지
	 * 
	 * @param action
	 ********************/
	@RequestMapping(value = "/login", method = RequestMethod.GET)
	public ModelAndView login() {

		ModelAndView mav = new ModelAndView();
		mav.setViewName("member/login");

		return mav;
	}

	/**********************
	 * 회원가입
	 *********************/

	@RequestMapping(value = "join", method = RequestMethod.POST)
	public String join(MemberVO member, Model model) throws Exception {

		String url = "";
		int result = 0;

		result = memberService.join(member);

		if (result == 0) {
			url = "member/joinPage";
		} else {
			url = "member/joinFinish";
		}

		return url;
	}

	/*****************************
	 * 아이디 중복 확인
	 *****************************/
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

	/*******************************
	 * 닉네임 중복 확인
	 *******************************/
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
	 ************************************/
	@RequestMapping(value = "/memberPhoneChk", method = RequestMethod.POST)
	@ResponseBody
	public String memberPhoneChkPOST(String phone) throws Exception {

		int result = memberService.phoneCheck(phone);
		log.info("핸드폰 번호 " + phone);
		log.info("핸드폰 중복 확인 : " + result );
		if (result != 0) {
			return "fail"; // 중복 핸드폰 번호 존재
		} else {
			return "success"; // 중복 핸드폰 번호 X
		}
	}

	/**********************************
	 * 로그인
	 *********************************/
	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public ModelAndView login(HttpServletRequest request, MemberVO member, RedirectAttributes rttr) throws Exception {

		/* 비밀번호 암호화 해야됨~ */
		HttpSession session = request.getSession();

		MemberVO vo = memberService.login(member);

		ModelAndView mav = new ModelAndView();
		String user_pw = "";
		String encodePw = "";

		if (vo != null) { // 일치하는 아이디 존재시

			user_pw = member.getUser_pw(); // 사용자가 제출한 비밀번호
			encodePw = vo.getUser_pw(); // 데이터베이스에 저장된 비밀번호

			log.info(member.getUser_pw());
			log.info(vo.getUser_pw());

			log.info(member);

			if (user_pw.equals(encodePw)) { // 비밀번호 일치여부 판단

				session.setAttribute("member", vo);
				session.setMaxInactiveInterval(-1); // 세션 시간을 무한대로 설정

				mav.setViewName("main");
				return mav;

			} else {

				rttr.addFlashAttribute("result", 1);
				mav.setViewName("redirect:/member/login");
				return mav;
			}

		} else { // 일치하는 아이디가 존재하지 않을 시 (로그인 실패)

			rttr.addFlashAttribute("result", 2);
			mav.setViewName("redirect:/member/login");
			return mav;
		}

	}

	/********************
	 * 로그아웃
	 ********************/
	@RequestMapping(value = "/logout", method = RequestMethod.GET)
	public void logout(HttpServletRequest request, HttpServletResponse response) throws Exception {

		HttpSession session = request.getSession();
		session.removeAttribute("member");
		session.removeAttribute("isLogon");
		session.invalidate();

	}

	/********************
	 * 마이페이지 - 커뮤니티 작성 글
	 ********************/
	@RequestMapping(value = "/comMyWrite", method = RequestMethod.GET)
	public String comMyWrite(HttpServletRequest request, Model model, @ModelAttribute CommunityVO com_vo) throws Exception {

		String url = "";

		HttpSession session = request.getSession(false);
		if (session != null) {
			MemberVO member = (MemberVO) session.getAttribute("member");
			if (member != null) {
				url = "mypage/comMyWrite";
				
				com_vo.setUser_id(member.getUser_id());
				
				List<CommunityVO> comMyWrite = memberService.comMyWrite(com_vo);
				model.addAttribute("comMyWrite", comMyWrite);
				
				int total = memberService.myComListCnt(com_vo);
				model.addAttribute("pageMaker",new PageDTO(com_vo,total));
			} else {
				url = "member/login";
			}
		} else {
			url = "member/login";
		}

		return url;

	}
	
	/********************
	 * 마이페이지 - 중고악기 작성 글
	 ********************/
	@RequestMapping(value = "/fleaMyWrite", method = RequestMethod.GET)
	public String fleaMyWrite(HttpServletRequest request, Model model, @ModelAttribute FleaMarketVO flea_vo) throws Exception {

		String url = "";

		HttpSession session = request.getSession(false);
		if (session != null) {
			MemberVO member = (MemberVO) session.getAttribute("member");
			if (member != null) {
				url = "mypage/fleaMyWrite";
				
				flea_vo.setUser_id(member.getUser_id());
				
				List<FleaMarketVO> fleaMyWrite = memberService.fleaMyWrite(flea_vo);
				model.addAttribute("fleaMyWrite", fleaMyWrite);
				
				int total = memberService.myFleaListCnt(flea_vo);
				model.addAttribute("pageMaker",new PageDTO(flea_vo, total));
			} else {
				url = "member/login";
			}
		} else {
			url = "member/login";
		}

		return url;

	}
	
	/********************
	 * 마이페이지 - 구인구직 작성 글
	 ********************/
	@RequestMapping(value = "/jobMyWrite", method = RequestMethod.GET)
	public String jobMyWrite(HttpServletRequest request, Model model, @ModelAttribute JobVO jov_vo) throws Exception {

		String url = "";

		HttpSession session = request.getSession(false);
		if (session != null) {
			MemberVO member = (MemberVO) session.getAttribute("member");
			if (member != null) {
				url = "mypage/jobMyWrite";
				
				jov_vo.setUser_id(member.getUser_id());
				
				List<JobVO> jobMyWrite = memberService.jobMyWrite(jov_vo);
				model.addAttribute("jobMyWrite", jobMyWrite);
				
				int total = memberService.myJobListCnt(jov_vo);
				model.addAttribute("pageMaker",new PageDTO(jov_vo, total));
			} else {
				url = "member/login";
			}
		} else {
			url = "member/login";
		}

		return url;

	}
	
	/********************
	 * 마이페이지 - 중고악기 작성 글
	 ********************/
	@RequestMapping(value = "/roomMyWrite", method = RequestMethod.GET)
	public String roomMyWrite(HttpServletRequest request, Model model, @ModelAttribute RoomRentalVO room_vo) throws Exception {

		String url = "";

		HttpSession session = request.getSession(false);
		if (session != null) {
			MemberVO member = (MemberVO) session.getAttribute("member");
			if (member != null) {
				url = "mypage/roomMyWrite";
				
				room_vo.setUser_id(member.getUser_id());
				
				List<RoomRentalVO> roomMyWrite = memberService.roomMyWrite(room_vo);
				model.addAttribute("roomMyWrite",roomMyWrite);
				
				int total = memberService.myRoomListCnt(room_vo);
				model.addAttribute("pageMaker",new PageDTO(room_vo, total));
			} else {
				url = "member/login";
			}
		} else {
			url = "member/login";
		}

		return url;

	}

	/*************************
	 * 마이페이지 - 회원 정보 페이지
	 *************************/
	@RequestMapping(value = "/account", method = RequestMethod.GET)
	public String mypageUpdateForm(HttpServletRequest request, Model model) throws Exception {

		String url = "";

		HttpSession session = request.getSession(false);
		if (session != null) {
			MemberVO member = (MemberVO) session.getAttribute("member");
			if (member != null) {
				url = "mypage/account";

				MemberVO account = memberService.account(member);

				model.addAttribute("account", account);
			} else {
				url = "member/login";
			}
		} else {
			url = "member/login";
		}

		return url;
	}

	
	/******************************
	 * 아이디/비밀번호 찾기 페이지
	 *******************************/
	@RequestMapping(value = "/joinFind", method = RequestMethod.GET)
	public ModelAndView findIdPage() throws Exception {
		ModelAndView mav = new ModelAndView();
		mav.setViewName("member/joinFind");
		return mav;
	}

	/*************************
	 * 아이디 찾기
	 ***************************/
	@RequestMapping(value = "/memberFindId", method = RequestMethod.POST)
	@ResponseBody
	public String memberFindIdPOST(@RequestParam("user_name") String user_name , @RequestParam("phone") String phone) throws Exception {
		
		 MemberVO result = memberService.FindId(user_name, phone);
		 
		 if(result != null) { return result.getUser_id(); } else { return ""; }
		 
	}

	/*************************
	 * 비밀번호 찾기
	 ***************************/
	@RequestMapping(value = "/memberFindPw", method = RequestMethod.POST)
	@ResponseBody
	public String memberFindPwPOST(String user_id, String user_name, String phone) throws Exception {

		MemberVO result = memberService.FindPw(user_id, user_name, phone);

		if (result != null) {
			return result.getUser_pw();
		} else {
			return "";
		}
	}
	
	/********************************
	 * 카카오 로그인
	 *********************************/
	@RequestMapping(value="/kakaoLogin", method=RequestMethod.GET)
	public String kakaoLogin(@RequestParam(value = "code", required = false) String code) throws Exception {
		System.out.println("#########" + code);
        
		// 위에서 만든 코드 아래에 코드 추가
		String access_Token = memberService.getAccessToken(code);
		System.out.println("###access_Token#### : " + access_Token);
        
		return "member/testPage";
    	}

	/***********************************
	 * 전화번호 변경
	 ***********************************/
	@RequestMapping(value = "/phoneModi", method = RequestMethod.POST)
	public String phoneModi(HttpServletRequest request, Model model, String phone, RedirectAttributes rttr) throws Exception {

		String url = "";

		HttpSession session = request.getSession();
		if (session != null) {
			MemberVO member = (MemberVO) session.getAttribute("member");
			if (member != null) {
				String user_id = member.getUser_id();
				
				MemberVO m_vo = new MemberVO();
				m_vo.setUser_id(user_id);
				m_vo.setPhone(phone);
				
				int result = memberService.phoneModi(m_vo);
				
				if(result == 1) {
					rttr.addFlashAttribute("phone_result", "success");
				}else {
					rttr.addFlashAttribute("phone_result", "fail");
				}
				
				return "redirect:/member/account";
				
			} else {
				url = "member/login";
			}
		} else {
			url = "member/login";
		}

		return url;
	}

	/************************************
	 * 비밀번호 확인
	 ***********************************/
	 @RequestMapping(value = "/pwConfirm", method = RequestMethod.POST)
	 @ResponseBody
	 public String pwConfirm(String user_pw, HttpServletRequest request) throws Exception {
		 	
		 HttpSession session = request.getSession();
		 MemberVO member = (MemberVO) session.getAttribute("member");
			 
		 String user_id = member.getUser_id();
					
		 MemberVO m_vo = new MemberVO();
		 m_vo.setUser_id(user_id);
		 m_vo.setUser_pw(user_pw);
					
		 int result = memberService.pwConfirm(m_vo);

		 if (result == 1) {
			 return "success"; 	// 비밀번호 일치
		 } else {
			 return "fail";		// 비밀번호 일치하지 않음
		 }
	 }
	
	
	/***********************************
	 * 비밀번호 변경
	 ***********************************/
	@RequestMapping(value = "/pwModi", method = RequestMethod.POST)
	public String pwModi(HttpServletRequest request, Model model, String user_pw, RedirectAttributes rttr) throws Exception {

		String url = "";

		HttpSession session = request.getSession();
		if (session != null) {
			MemberVO member = (MemberVO) session.getAttribute("member");
			if (member != null) {
				String user_id = member.getUser_id();
				
				MemberVO m_vo = new MemberVO();
				m_vo.setUser_id(user_id);
				m_vo.setUser_pw(user_pw);
				
				int result = memberService.pwModi(m_vo);

				if(result == 1) {
					rttr.addFlashAttribute("pw_result", "success");
				}else {
					rttr.addFlashAttribute("pw_result", "fail");
				}
				
				return "redirect:/member/account";

			} else {
				url = "/member/login";
			}
		} else {
			url = "member/login";
		}

		return url;
	}
	
	/******************************
	 * 주소 변경
	 ******************************/
	@RequestMapping(value = "/addressModi", method = RequestMethod.POST)
	public String addressModi(HttpServletRequest request, Model model, String address, RedirectAttributes rttr) throws Exception {
		
		String url = "";

		HttpSession session = request.getSession();
		if (session != null) {
			MemberVO member = (MemberVO) session.getAttribute("member");
			if (member != null) {
				String user_id = member.getUser_id();
				
				MemberVO m_vo = new MemberVO();
				m_vo.setUser_id(user_id);
				m_vo.setAddress(address);
				
				int result = memberService.addressModi(m_vo);

				if(result == 1) {
					rttr.addFlashAttribute("add_result", "success");
				}else {
					rttr.addFlashAttribute("add_result", "fail");
				}
				
				return "redirect:/member/account";

			} else {
				url = "/member/login";
			}
		} else {
			url = "member/login";
		}

		return url;
	}
	
	/*******************************
	 * 회원 탈퇴
	 *******************************/
    @RequestMapping(value = "/withDraw", method = RequestMethod.POST)
    public String withDraw(HttpServletRequest request) {
    	
    	String url;
    	
    	HttpSession session = request.getSession();
		if (session != null) {
			MemberVO member = (MemberVO) session.getAttribute("member");
			if (member != null) {
				String user_id = member.getUser_id();
				
				int result = memberService.withDraw(user_id);
				
				if (result == 1) {
					return "redirect:/";
				} else {
					return "fail";
				}

			} else {
				url = "/member/login";
			}
		} else {
			url = "member/login";
		}
		return url;
    }
}
