package com.jam.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import javax.servlet.Filter;

import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import com.jam.client.chat.webSocket.WebSocketConfig;
import com.jam.global.jwt.JwtInterceptor;
import com.jam.global.redis.RedisConfig;


@Configuration
@ComponentScan(basePackages = {"com.jam"})
@PropertySource("classpath:application.properties")
public class WebConfig extends AbstractAnnotationConfigDispatcherServletInitializer implements WebMvcConfigurer {
    
	@Autowired
	private JwtInterceptor jwtInterceptor; 	
	
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
    
    /*
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        StringHttpMessageConverter stringConverter = new StringHttpMessageConverter(Charset.forName("UTF-8"));
        stringConverter.setSupportedMediaTypes(Arrays.asList(new MediaType("text", "html", Charset.forName("UTF-8"))));
        converters.add(stringConverter);

        // JSON 직렬화
        converters.add(new MappingJackson2HttpMessageConverter());
        
    }*/
    
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        StringHttpMessageConverter stringConverter = new StringHttpMessageConverter(Charset.forName("UTF-8"));
        stringConverter.setSupportedMediaTypes(Arrays.asList(new MediaType("text", "plain", Charset.forName("UTF-8"))));
        converters.add(stringConverter);

        converters.add(new MappingJackson2HttpMessageConverter());
    }
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOrigins("http://localhost:8080")
            .allowedMethods("GET", "POST", "PUT", "DELETE")
            .allowCredentials(true);
    }
	
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
        		.addPathPatterns(
        				"/api/member/auth/check",
        				"/api/member/me/token", 
        				"/api/member/logout", 
        				"/oauth/kakao/logout",
        				"/oauth/naver/logout",
        				"/api/member/userName",
        				"/api/member/phone",
        				"/api/member/verify-password",
        				"/api/member/password",
        				"/api/member/address",
        				"/api/member/deleteAccount",
        				
        				"/api/job/**",
        				"/api/community/**",
        				"/api/fleaMarket/**",
        				"/api/roomRental/**",
        				
        				"/api/**/posts",
        				"/jobReplies/**",
        				"/comReplies/**",
        				"/api/chat/**",
        				"/api/mypage/favorite/**",
        				"/api/mypage/written/boards",
        				"/api/mypage/account"
        		);
   }
}
