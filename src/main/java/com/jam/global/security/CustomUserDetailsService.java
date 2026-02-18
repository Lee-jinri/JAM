package com.jam.global.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.jam.member.dto.MemberDto;
import com.jam.member.mapper.MemberMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {
	private final MemberMapper memberMapper;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		MemberDto user = memberMapper.findByUserInfo(username);
		
		if (user == null) {
	        log.warn("[로그인 실패] 사용자 정보 없음 - ID: {}" + username);
	        throw new UsernameNotFoundException("해당 사용자를 찾을 수 없습니다. 입력한 ID: " + username);
	    }
		return user;
	}

}
