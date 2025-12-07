package com.jam.config;

import javax.servlet.Filter;

import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import com.jam.client.chat.webSocket.WebSocketConfig;
import com.jam.global.redis.RedisConfig;


//@Configuration
//@ComponentScan(basePackages = {"com.jam"})
//@PropertySource("classpath:application.properties")
public class WebConfig extends AbstractAnnotationConfigDispatcherServletInitializer implements WebMvcConfigurer {
    
	
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
}
