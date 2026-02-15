package com.jam.mypage.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping(value="/mypage")
@AllArgsConstructor
@Slf4j
public class MypageController {

	// TODO: 추후 다른 사용자의 작성글 조회 기능 추가 (닉네임 클릭 시 이동)
	/*************************************
	 * @return 다른 사용자가 작성한 글 페이지
	 ************************************/
	@GetMapping("/user/{targetUserId}/posts")
	public String userPostsPage(@PathVariable String targetUserId, Model model) {
	    model.addAttribute("targetUserId", targetUserId);
	    return "mypage/posts";
	}
	
	/****************************************************
	 * 마이페이지 - 회원 정보 페이지로 이동하는 메서드
	 * @return 마이페이지 - 회원 정보 페이지
	 ****************************************************/
	@GetMapping("/account")
	public String mypageAccount() throws Exception {
		return "mypage/account";
	}
}

