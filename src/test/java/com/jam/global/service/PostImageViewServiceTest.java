package com.jam.global.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.jam.s3.service.S3Service;

@ExtendWith(MockitoExtension.class)
class PostImageViewServiceTest {

	@Mock
	private S3Service s3Service;

	private PostImageViewService service;

	@Nested
	@DisplayName("injectViewUrls")
	class InjectViewUrls {

		@Test
		@DisplayName("null/빈 문자열은 그대로 반환하고 S3를 호출하지 않는다")
		void nullOrBlank_returnsAsIs() {
			service = new PostImageViewService(s3Service);

			assertThat(service.injectViewUrls(null)).isNull();
			assertThat(service.injectViewUrls("")).isEqualTo("");
			verify(s3Service, never()).generatePresignedViewUrl(org.mockito.ArgumentMatchers.any());
		}

		@Test
		@DisplayName("data-key가 있는 img 태그의 src를 presigned URL로 치환한다")
		void replacesImgSrcWithPresignedUrl() {
			service = new PostImageViewService(s3Service);
			given(s3Service.generatePresignedViewUrl("some/key.png")).willReturn("https://s3.example.com/view");

			String result = service.injectViewUrls("<p>내용</p><img data-key=\"some/key.png\" src=\"placeholder\">");

			assertThat(result).contains("src=\"https://s3.example.com/view\"");
		}

		@Test
		@DisplayName("data-key가 없는 img 태그는 건드리지 않는다")
		void imgWithoutDataKey_untouched() {
			service = new PostImageViewService(s3Service);

			String result = service.injectViewUrls("<img src=\"https://external.com/a.png\">");

			assertThat(result).contains("src=\"https://external.com/a.png\"");
			verify(s3Service, never()).generatePresignedViewUrl(org.mockito.ArgumentMatchers.any());
		}
	}
}
