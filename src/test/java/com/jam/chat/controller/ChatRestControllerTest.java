package com.jam.chat.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import com.jam.chat.dto.ChatDto;
import com.jam.chat.dto.ChatRoomListDto;
import com.jam.chat.service.ChatRoomFacade;
import com.jam.chat.service.ChatService;
import com.jam.config.MyBatisConfig;
import com.jam.fleaMarket.service.FleaMarketService;
import com.jam.global.exception.BadRequestException;
import com.jam.global.security.JwtAuthenticationFilter;
import com.jam.member.dto.MemberDto;

/**
 * ChatRestController @WebMvcTest 슬라이스 테스트. 다른 도메인과 같은 패턴.
 */
@WebMvcTest(
		controllers = ChatRestController.class,
		excludeFilters = {
				@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class),
				@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = MyBatisConfig.class)
		})
@Import(ChatRestControllerTest.MethodSecurityTestConfig.class)
class ChatRestControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private ChatService chatService;
	@MockBean
	private ChatRoomFacade chatRoomFacade;
	@MockBean
	private FleaMarketService fleaService;

	private MemberDto loginUser;

	@BeforeEach
	void setUp() {
		reset(chatService, chatRoomFacade, fleaService);

		loginUser = new MemberDto();
		loginUser.setUser_id("user1");
		loginUser.setUser_name("tester");
	}

	@TestConfiguration
	@EnableMethodSecurity
	static class MethodSecurityTestConfig {
		@Bean
		SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
			http.csrf(csrf -> csrf.disable())
					.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
			return http.build();
		}
	}

	@Nested
	@DisplayName("GET /api/chat/chatRooms")
	class GetChatRooms {

		@Test
		@DisplayName("비로그인이면 401을 응답한다")
		void anonymous_unauthorized() throws Exception {
			mockMvc.perform(get("/api/chat/chatRooms"))
					.andExpect(status().isUnauthorized());
		}

		@Test
		@DisplayName("로그인 사용자의 채팅방 목록을 반환한다")
		void loggedIn_returnsChatRooms() throws Exception {
			given(chatService.getChatRooms("user1")).willReturn(List.of(new ChatRoomListDto()));

			mockMvc.perform(get("/api/chat/chatRooms").with(user(loginUser)))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$").isArray());
		}
	}

	@Nested
	@DisplayName("GET /api/chat/chatRoomId")
	class GetChatRoomId {

		@Test
		@DisplayName("비로그인이면 401을 응답한다")
		void anonymous_unauthorized() throws Exception {
			mockMvc.perform(get("/api/chat/chatRoomId").param("postId", "1"))
					.andExpect(status().isUnauthorized());
		}

		@Test
		@DisplayName("존재하지 않는 게시글이면 404를 응답한다")
		void postNotFound_notFound() throws Exception {
			given(fleaService.getWriterIdByPostId(1L)).willReturn(null);

			mockMvc.perform(get("/api/chat/chatRoomId").param("postId", "1").with(user(loginUser)))
					.andExpect(status().isNotFound());
		}

		@Test
		@DisplayName("정상 요청이면 채팅방 id를 반환한다")
		void success_returnsRoomId() throws Exception {
			given(fleaService.getWriterIdByPostId(1L)).willReturn("writer1");
			given(chatRoomFacade.getOrCreateChatRoomId("user1", "writer1")).willReturn(99L);

			mockMvc.perform(get("/api/chat/chatRoomId").param("postId", "1").with(user(loginUser)))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$").value(99));
		}

		@Test
		@DisplayName("상대방이 탈퇴한 사용자면(BadRequestException) 400이 전파된다")
		void targetInactive_badRequest() throws Exception {
			given(fleaService.getWriterIdByPostId(1L)).willReturn("writer1");
			given(chatRoomFacade.getOrCreateChatRoomId("user1", "writer1"))
					.willThrow(new BadRequestException("상대방이 탈퇴하여 채팅을 시작할 수 없습니다."));

			mockMvc.perform(get("/api/chat/chatRoomId").param("postId", "1").with(user(loginUser)))
					.andExpect(status().isBadRequest());
		}
	}

	@Nested
	@DisplayName("GET /api/chat/messages")
	class GetMessages {

		@Test
		@DisplayName("비로그인이면 401을 응답한다")
		void anonymous_unauthorized() throws Exception {
			mockMvc.perform(get("/api/chat/messages").param("roomId", "1"))
					.andExpect(status().isUnauthorized());
		}

		@Test
		@DisplayName("로그인 사용자의 메시지 목록을 반환한다")
		void loggedIn_returnsMessages() throws Exception {
			given(chatService.getMessages(1L, "user1")).willReturn(List.of(new ChatDto()));

			mockMvc.perform(get("/api/chat/messages").param("roomId", "1").with(user(loginUser)))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$").isArray());

			verify(chatService).getMessages(1L, "user1");
		}
	}
}
