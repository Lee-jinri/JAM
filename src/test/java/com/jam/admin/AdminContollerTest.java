package com.jam.admin;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

import com.jam.config.MyBatisConfig;
import com.jam.global.security.JwtAuthenticationFilter;
import com.jam.member.dto.MemberDto;

/**
 * AdminContoller @WebMvcTest 슬라이스 테스트. ROLE_ADMIN 권한 게이트만 검증한다.
 * updateAllAreas는 메서드 내부에서 new RestTemplate()으로 통계청 공공 API를 직접 호출해
 * (주입 불가) 목으로 대체할 수 없으므로, 정상 호출(관리자) 경로는 테스트 범위에서 제외하고
 * 비인가 접근이 확실히 막히는지만 검증한다.
 */
@WebMvcTest(
		controllers = AdminContoller.class,
		excludeFilters = {
				@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class),
				@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = MyBatisConfig.class)
		})
@Import(AdminContollerTest.MethodSecurityTestConfig.class)
class AdminContollerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private AdminService adminService;

	private MemberDto regularUser;
	private MemberDto adminUser;

	@BeforeEach
	void setUp() {
		regularUser = new MemberDto();
		regularUser.setUser_id("user1");
		regularUser.setRoles(List.of("ROLE_USER"));

		adminUser = new MemberDto();
		adminUser.setUser_id("admin1");
		adminUser.setRoles(List.of("ROLE_ADMIN"));
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
	@DisplayName("GET /admin/main")
	class DoAdmin {

		@Test
		@DisplayName("비로그인이면 접근 거부된다")
		void anonymous_denied() throws Exception {
			mockMvc.perform(get("/admin/main"))
					.andExpect(status().isForbidden());
		}

		@Test
		@DisplayName("ROLE_ADMIN이 아니면 접근 거부된다")
		void nonAdmin_denied() throws Exception {
			mockMvc.perform(get("/admin/main").with(user(regularUser)))
					.andExpect(status().isForbidden());
		}
	}

	@Nested
	@DisplayName("POST /admin/updateAreas")
	class UpdateAllAreas {

		@Test
		@DisplayName("비로그인이면 접근 거부된다")
		void anonymous_denied() throws Exception {
			mockMvc.perform(post("/admin/updateAreas"))
					.andExpect(status().isForbidden());
		}

		@Test
		@DisplayName("ROLE_ADMIN이 아니면 접근 거부된다")
		void nonAdmin_denied() throws Exception {
			mockMvc.perform(post("/admin/updateAreas").with(user(regularUser)))
					.andExpect(status().isForbidden());
		}
	}
}
