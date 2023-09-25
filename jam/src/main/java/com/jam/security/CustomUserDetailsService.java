package com.jam.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.jam.client.member.dao.MemberDAO;
import com.jam.client.member.vo.MemberVO;

import lombok.Setter;
import lombok.extern.log4j.Log4j;

@Log4j
public class CustomUserDetailsService implements UserDetailsService {

	@Setter(onMethod_= {@Autowired})
    private MemberDAO memberDao;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		MemberVO user = memberDao.findByUsername(username);
		
		
		if(user == null) {
			log.info("loadUserByUsername");
            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다 " + username);
        }
       
       
        return User.builder()
                .username(user.getUser_id())
                .password(user.getUser_pw()) 
                .roles(user.getRole())
                .build();
		
	}

}
