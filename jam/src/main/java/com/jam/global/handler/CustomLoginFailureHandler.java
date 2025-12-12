package com.jam.global.handler;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CustomLoginFailureHandler implements AuthenticationFailureHandler {

	@Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
		
		// 시스템 오류라면 500
	    if (exception instanceof AuthenticationServiceException) {
	        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	        response.setContentType("application/json;charset=UTF-8");

	        log.error("시스템 오류: " + exception);
	        response.getWriter().write("{ \"error\": \"시스템 오류입니다. 잠시 후 다시 시도해주세요.\" }");
	        
	        return;
	    }

	    // 나머지는 사용자 인증 실패 (401)
	    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
	    response.setContentType("application/json;charset=UTF-8");

	    log.warn("인증 실패: " + exception.getMessage());
	    response.getWriter().write("{ \"error\": \"아이디 또는 비밀번호가 올바르지 않습니다.\" }");
    }
}
