package com.jam.member.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/member")
@Slf4j
public class MemberController {
	/******************************
	 * 회원 가입 페이지로 이동
	 *  회원 가입 성공 시 회원 가입 이전 페이지로 이동하기 위해 이전 페이지 uri를 세션에 저장
	 * @return 회원가입 페이지
	 ******************************/
	@GetMapping("/join")
	public String joinPage() {
		return "member/join";
	}
	
	/****************************
	 * 로그인 페이지로 이동
	 * 로그인 성공 시 로그인 이전 페이지로 이동하기 위해 이전 페이지 uri를 세션에 저장
	 * @param request
	 ****************************/
	@GetMapping("/login")
	public String loginPage() {
		return "member/login";
	}

	/***********************************
	 * @return 아이디/비밀번호 찾기 페이지
	 ***********************************/
	@GetMapping("/findAccount")
	public String findUserPage() {
		return "member/findAccount";
	}
}
