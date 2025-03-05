package com.jam.security;

import java.util.Collection;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

public class CustomAuthentication extends UsernamePasswordAuthenticationToken {

	private static final long serialVersionUID = 1L;
	
	private final String nickname;

    public CustomAuthentication(Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities, String nickname) {
        super(principal, credentials, authorities);
        this.nickname = nickname;
    }

    public String getNickname() {
        return nickname;
    }
}
