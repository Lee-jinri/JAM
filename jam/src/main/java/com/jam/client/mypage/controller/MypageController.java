package com.jam.client.mypage.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j;

@Controller
@RequestMapping(value="/mypage")
@AllArgsConstructor
@Log4j
public class MypageController {

	/***********************************
	 * @return 북마크 페이지
	 ***********************************/
	@RequestMapping(value="/favorite", method = RequestMethod.GET)
	public String favoritePage() {
		return "mypage/favorite";
	}
	
	/*************************************
	 * @return 작성한 글 페이지
	 ************************************/
	@RequestMapping(value="/written", method = RequestMethod.GET)
	public String writtenPage() {
		return "mypage/written";
	}
	
	/****************************************************
	 * 마이페이지 - 회원 정보 페이지로 이동하는 메서드
	 * @return 마이페이지 - 회원 정보 페이지 / 로그인 되어 있지 않으면 로그인 페이지로 이동
	 ****************************************************/
	@RequestMapping(value = "/account", method = RequestMethod.GET)
	public String mypageAccount() throws Exception {

		return "mypage/account";
		
	}
}
