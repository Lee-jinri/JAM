package com.jam.config;

import javax.servlet.Filter;
import javax.servlet.ServletRegistration;

import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import com.jam.client.chat.webSocket.WebSocketConfig;
import com.jam.global.redis.RedisConfig;

public class WebInitializer extends AbstractAnnotationConfigDispatcherServletInitializer{
    
	@Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class[]{RootConfig.class, SecurityConfig.class, WebSocketConfig.class, RedisConfig.class}; 
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class[]{ServletConfig.class}; 
    }

    @Override
    protected String[] getServletMappings() {
        return new String[]{"/"};
    }
    
    @Override
    protected Filter[] getServletFilters() {
        CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter();
        characterEncodingFilter.setEncoding("UTF-8");
        characterEncodingFilter.setForceEncoding(true);
        

        return new Filter[] {characterEncodingFilter};
    }

	@Override
	protected void customizeRegistration(ServletRegistration.Dynamic registration) {
		// 404 에러를 Spring이 예외로 던지게 설정
		registration.setInitParameter("throwExceptionIfNoHandlerFound", "true");
	}
}
