package com.jam.chat.service;

import org.springframework.stereotype.Service;

import com.jam.member.service.MemberService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceAdapter {
	private final MemberService memberService;
	
    public String getUserName(String userId) {
        return memberService.getUserName(userId);
    }
}
