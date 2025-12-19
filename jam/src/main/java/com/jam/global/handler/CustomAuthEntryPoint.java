package com.jam.global.handler;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.jam.global.util.AuthClearUtil;

@Component
public class CustomAuthEntryPoint implements AuthenticationEntryPoint  {
	
	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException ex) throws IOException {
		AuthClearUtil.clearAuth(request, response);
	    
		response.sendError(HttpServletResponse.SC_UNAUTHORIZED);

	    //response.sendRedirect("/common/unauthorized");
	}
}
