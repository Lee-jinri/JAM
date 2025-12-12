package com.jam.global.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.jam.client.member.dao.MemberDAO;
import com.jam.client.member.vo.MemberVO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

	private final MemberDAO memberDao;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        
		MemberVO user = memberDao.findByUserInfo(username);
			
		if (user == null) {
	        log.warn("[로그인 실패] 사용자 정보 없음 - ID: {}" + username);
	        throw new UsernameNotFoundException("해당 사용자를 찾을 수 없습니다. 입력한 ID: " + username);
	    }
		
		return user;
	}
}
