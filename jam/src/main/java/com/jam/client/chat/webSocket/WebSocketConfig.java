package com.jam.client.chat.webSocket;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.jam.client.chat.service.ChatService;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSocket // 웹소켓 서버 사용
//@EnableWebSocketMessageBroker // STOMP 사용
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer  {
	
    private final ChatService chatService;
	private final CustomWebSocketInterceptor customWebSocketInterceptor;

	@Bean
	public WebSocketHandler webSocketHandler() {
		return new WebSocketHandler(chatService);
	}

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketHandler(), "/ws")
        		.addInterceptors(customWebSocketInterceptor)
                .setAllowedOrigins("*");
    }
}
