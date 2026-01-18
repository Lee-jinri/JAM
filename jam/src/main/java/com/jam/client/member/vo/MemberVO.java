package com.jam.client.member.vo;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.jam.common.vo.CommonVO;

import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper=false)
public class MemberVO extends CommonVO implements UserDetails {
	private String user_id = "";		
	private String user_pw = "";		
	private String user_name = "";		
	private String phone = "";			
	private String address = "";		
	private String email = ""; 			
	private String role = ""; 		
	private List<String> roles;
	private int social_login; 		// 소셜 로그인 여부 0 : 소셜로그인 아님 / 1:소셜 로그인 

	private String company_name; 	// nullable 기업 회원만 
	private LocalDateTime deleted_at;
	
	// implements UserDetails
	private boolean enabled;
    
	// Spring Security 인가용
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
	    if (roles == null || roles.isEmpty()) {
	    	return List.of(new SimpleGrantedAuthority("ROLE_USER"));
	    }

	    return roles.stream()
	                .filter(role -> role != null && !role.isBlank()) // 빈 문자열 방지
	                .map(SimpleGrantedAuthority::new)
	                .toList();
	}
    
    @Override
    public String getPassword() {
        return user_pw;
    }

    @Override
    public String getUsername() {
        return user_id;
    }
    
    public String getNickname() {
        return user_name;
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
    
    public boolean isEmpty() {
        return (user_id == null || user_id.isBlank())
            && (user_name == null || user_name.isBlank())
            && (roles == null || roles.isEmpty());
    }

	
}
