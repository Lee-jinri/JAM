package com.jam.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jam.client.chat.controller.ChatController;
import com.jam.client.chat.service.ChatService;
import com.jam.client.member.controller.MemberController;
import com.jam.client.member.service.MemberService;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSocket // 웹소켓 서버 사용
//@EnableWebSocketMessageBroker // STOMP 사용
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer  {
	
    private final ChatService chatService;
	private final MemberService memberService;
	private final CustomWebSocketInterceptor customWebSocketInterceptor;

	/*
	private final RedisTemplate<String, String> redisTemplate;
    */
    //private final ObjectMapper objectMapper;
    
	/*
	@Autowired
    private RedisTemplate<String, String> redisTemplate;
	
	@Autowired
    private ChatService chatService;
	*/
    //private final ChatService chatService;
	
    /*@Autowired
    public WebSocketConfig(ChatService chatService, ObjectMapper objectMapper) {
        this.chatService = chatService;
        this.objectMapper = objectMapper;
    }*/

	@Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
	

	/*
    @Bean
    public WebSocketHandler webSocketHandler() {
        return new WebSocketHandler(redisTemplate, chatService);
    }*/
	
	@Bean
	public WebSocketHandler webSocketHandler() {
		return new WebSocketHandler(chatService, memberService);
	}

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketHandler(), "/ws")
        		.addInterceptors(customWebSocketInterceptor)
                .setAllowedOrigins("*");
    }


	
}
