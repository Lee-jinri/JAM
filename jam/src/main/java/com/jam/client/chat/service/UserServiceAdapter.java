package com.jam.client.chat.service;

import org.springframework.stereotype.Service;

import com.jam.client.member.service.MemberService;

@Service
public class UserServiceAdapter {
	private final MemberService memberService;

    public UserServiceAdapter (MemberService memberService) {
        this.memberService = memberService;
    }

    public String getUserId(String nickname) {
        return memberService.getUserId(nickname);
    }
}
