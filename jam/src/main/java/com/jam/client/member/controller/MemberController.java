package com.jam.client.member.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import lombok.extern.log4j.Log4j;

@Controller
@RequestMapping("/member")
@Log4j
public class MemberController {
	
	/******************************
	 * 회원 가입 페이지로 이동
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
	 * 로그인 페이지로 이동
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


	/***********************************
	 * @return 아이디/비밀번호 찾기 페이지
	 ***********************************/
	@RequestMapping(value = "/joinFind", method = RequestMethod.GET)
	public String findUserPage() {
		
		return "member/joinFind";
	}
	

	

}
