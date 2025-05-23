package com.jam.config;

import javax.servlet.ServletContext;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.UrlBasedViewResolver;
import org.springframework.web.servlet.view.tiles3.TilesConfigurer;
import org.springframework.web.servlet.view.tiles3.TilesView;

import com.jam.client.chat.webSocket.WebSocketConfig;

@EnableWebMvc
/*
@ComponentScan(
		basePackages = {
				"com.jam.admin.controler", 
				"com.jam.client.chat.controller" , 
				"com.jam.client.community.controller",
				"com.jam.client.comReply.controller",
				"com.jam.client.fleaMarket.controller",
				"com.jam.client.fleaReply.controller",
				"com.jam.client.job.controller",
				"com.jam.client.jobReply.controller",
				"com.jam.client.member.controller",
				"com.jam.client.message.controller",
				"com.jam.client.roomRental.controller",
				"com.jam.client.common.controller",
				"com.jam.redis"
				})*/
@ComponentScan(
		basePackages = {
				"com.jam.client",
				"com.jam.admin"
		})
@Import(WebSocketConfig.class)
@EnableGlobalMethodSecurity(prePostEnabled=true, securedEnabled=true)
public class ServletConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/resources/**").addResourceLocations("/resources/");
        registry.addResourceHandler("/uploadStorage/**").addResourceLocations("file:C:/uploadStorage/");
    }

    @Bean
    public CommonsMultipartResolver multipartResolver() {
        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver();
        multipartResolver.setMaxUploadSize(100000000);
        return multipartResolver;
    }

    @Bean
    public TilesConfigurer tilesConfigurer() {
        TilesConfigurer tilesConfigurer = new TilesConfigurer();
        tilesConfigurer.setDefinitions("/WEB-INF/tiles/tiles-setting.xml");
        return tilesConfigurer;
    }

    @Bean
    public UrlBasedViewResolver tilesViewResolver() {
        UrlBasedViewResolver viewResolver = new UrlBasedViewResolver();
        viewResolver.setViewClass(TilesView.class);
        viewResolver.setOrder(1); // 우선 순위 설정
        return viewResolver;
    }
    @Bean
    public InternalResourceViewResolver viewResolver() {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/WEB-INF/views/");
        viewResolver.setSuffix(".jsp");
        viewResolver.setOrder(2);
        return viewResolver;
    }
    
}