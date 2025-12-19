package com.jam.client.chat.service;

import org.springframework.stereotype.Service;

import com.jam.client.member.service.MemberService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceAdapter {
	// 
	private final MemberService memberService;
	
    public String getUserName(String userId) {
        return memberService.getUserName(userId);
    }
}
