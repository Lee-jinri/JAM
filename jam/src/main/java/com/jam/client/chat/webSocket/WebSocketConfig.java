package com.jam.client.chat.webSocket;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jam.client.chat.service.ChatService;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSocket // 웹소켓 서버 사용
//@EnableWebSocketMessageBroker // STOMP 사용
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer  {
	
    private final ChatService chatService;
	private final CustomWebSocketInterceptor customWebSocketInterceptor;
    private final ObjectMapper objectMapper;

	@Bean
	public WebSocketHandler webSocketHandler() {
		return new WebSocketHandler(chatService, objectMapper);
	}

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketHandler(), "/ws")
        		.addInterceptors(customWebSocketInterceptor)
                .setAllowedOrigins("*");
    }
}
