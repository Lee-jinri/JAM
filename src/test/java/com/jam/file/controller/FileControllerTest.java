package com.jam.file.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

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
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import com.jam.config.MyBatisConfig;
import com.jam.file.dto.FileAssetDto;
import com.jam.file.service.FileService;
import com.jam.global.exception.ForbiddenException;
import com.jam.global.security.JwtAuthenticationFilter;
import com.jam.global.service.FileAccessService;
import com.jam.member.dto.MemberDto;
import com.jam.s3.service.S3Service;

/**
 * FileController @WebMvcTest 슬라이스 테스트. 다른 도메인과 같은 패턴.
 */
@WebMvcTest(
		controllers = FileController.class,
		excludeFilters = {
				@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class),
				@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = MyBatisConfig.class)
		})
@Import(FileControllerTest.MethodSecurityTestConfig.class)
class FileControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private S3Service s3Service;
	@MockBean
	private FileService fileService;
	@MockBean
	private FileAccessService fileAccessService;

	private MemberDto loginUser;

	@BeforeEach
	void setUp() {
		reset(s3Service, fileService, fileAccessService);

		loginUser = new MemberDto();
		loginUser.setUser_id("user1");
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
	@DisplayName("POST /api/files/upload-url")
	class PresignUpload {

		@Test
		@DisplayName("필수 필드가 없으면 400을 응답한다")
		void missingFields_badRequest() throws Exception {
			mockMvc.perform(post("/api/files/upload-url")
							.contentType(MediaType.APPLICATION_JSON)
							.content("{\"file_name\":\"photo.png\"}"))
					.andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("정상 요청이면 presigned URL 정보를 반환한다")
		void success_returnsPresignedInfo() throws Exception {
			given(s3Service.presignUpload("photo.png", "image/png", 1000L, com.jam.file.dto.FileCategory.POST_IMAGE))
					.willReturn(Map.of("url", "https://s3.example.com/upload", "key", "images/posts/abc/photo.png"));

			mockMvc.perform(post("/api/files/upload-url")
							.contentType(MediaType.APPLICATION_JSON)
							.content("{\"file_name\":\"photo.png\",\"file_type\":\"image/png\",\"file_size\":1000,\"file_category\":\"POST_IMAGE\"}"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.url").value("https://s3.example.com/upload"));
		}
	}

	@Nested
	@DisplayName("GET /api/files/{fileId}/download-url")
	class DownloadFile {

		@Test
		@DisplayName("비로그인이면 401을 응답한다")
		void anonymous_unauthorized() throws Exception {
			mockMvc.perform(get("/api/files/1/download-url"))
					.andExpect(status().isUnauthorized());
		}

		@Test
		@DisplayName("접근 권한이 없으면(ForbiddenException) 403이 전파된다")
		void noAccess_forbidden() throws Exception {
			doThrow(new ForbiddenException("다운로드 할 권한이 없습니다."))
					.when(fileAccessService).existsFileAccess("user1", 1L);

			mockMvc.perform(get("/api/files/1/download-url").with(user(loginUser)))
					.andExpect(status().isForbidden())
					.andExpect(jsonPath("$.detail").value("다운로드 할 권한이 없습니다."));
		}

		@Test
		@DisplayName("접근 권한이 있으면 다운로드 URL을 반환한다")
		void hasAccess_returnsDownloadUrl() throws Exception {
			given(fileAccessService.existsFileAccess("user1", 1L)).willReturn(true);
			FileAssetDto file = new FileAssetDto();
			file.setFile_key("applications/2026/abc/resume.pdf");
			file.setFile_name("resume.pdf");
			given(fileService.getFileMetaByFileId(1L)).willReturn(file);
			given(s3Service.generatePresignedDownloadUrl("applications/2026/abc/resume.pdf", "resume.pdf"))
					.willReturn("https://s3.example.com/download");

			mockMvc.perform(get("/api/files/1/download-url").with(user(loginUser)))
					.andExpect(status().isOk());
		}
	}

	@Nested
	@DisplayName("GET /api/files/view-url")
	class GetImageViewUrl {

		@Test
		@DisplayName("인증 없이도 조회용 presigned URL을 반환한다")
		void returnsViewUrl_noAuthRequired() throws Exception {
			given(s3Service.generatePresignedViewUrl("images/posts/abc/photo.png"))
					.willReturn("https://s3.example.com/view");

			mockMvc.perform(get("/api/files/view-url").param("key", "images/posts/abc/photo.png"))
					.andExpect(status().isOk());
		}
	}
}
