package com.jam.client.member.vo;

import com.jam.common.vo.CommonVO;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.Data;


@Data
public class MemberVO extends CommonVO implements UserDetails {
	private String user_id = "";		// 아이디
	private String user_pw = "";		// 비밀번호
	private String user_name = "";	// 닉네임
	private String phone = "";		// 전화번호
	private String address = "";	// 주소
	private String email = ""; 		// 이메일
	private String role = ""; // 권한(역할) 정보
	
	// implements UserDetails
	private boolean enabled;

	/*
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }*/
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        
        //String roleGrant = "ROLE_" + role;
        
        GrantedAuthority myGrant = new SimpleGrantedAuthority(role);
        
        List<GrantedAuthority> authorities = new ArrayList<>();
        
        authorities.add(myGrant);
        
        return authorities;
    }
    
    @Override
    public String getPassword() {
        return user_pw;
    }

    @Override
    public String getUsername() {
        return user_id;
    }
	
    @Override
    public boolean isAccountNonExpired() {
        // 계정이 만료되지 않았음을 반환
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // 계정이 잠겨 있지 않음을 반환
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        // 자격 증명이 만료되지 않았음을 반환
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }


	
}
