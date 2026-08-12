package com.jam.s3.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.jam.file.dto.FileCategory;
import com.jam.global.util.FileUtils;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

	@Mock
	private FileUtils fileUtils;
	@Mock
	private S3Presigner presigner;
	@Mock
	private S3Client s3Client;

	@InjectMocks
	private S3Service s3Service;

	@BeforeEach
	void setBucket() {
		ReflectionTestUtils.setField(s3Service, "bucket", "test-bucket");
	}

	private URL url(String s) {
		try {
			return new URL(s);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	@Nested
	@DisplayName("presignUpload")
	class PresignUpload {

		@Test
		@DisplayName("검증 후 presigned URL과 정규화된 파일명/contentType을 반환한다")
		void validatesThenReturnsPresignedInfo() {
			given(fileUtils.sanitizeFilename("photo.png")).willReturn("photo.png");
			PresignedPutObjectRequest presigned = mock(PresignedPutObjectRequest.class);
			given(presigned.url()).willReturn(url("https://s3.example.com/upload"));
			given(presigner.presignPutObject(any(software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest.class)))
					.willReturn(presigned);

			Map<String, String> result = s3Service.presignUpload("photo.png", "image/png", 1000L, FileCategory.POST_IMAGE);

			verify(fileUtils).validateFileType("photo.png", "image/png", FileCategory.POST_IMAGE);
			verify(fileUtils).validateFileSize(1000L, FileCategory.POST_IMAGE);
			assertThat(result.get("url")).isEqualTo("https://s3.example.com/upload");
			assertThat(result.get("contentType")).isEqualTo("image/png");
			assertThat(result.get("filename")).isEqualTo("photo.png");
			assertThat(result.get("key")).contains("images/posts").contains("photo.png");
		}
	}

	@Nested
	@DisplayName("buildKey")
	class BuildKey {

		@Test
		@DisplayName("카테고리 prefix/날짜/uuid/파일명 형식으로 key를 만든다")
		void buildsExpectedFormat() {
			String key = s3Service.buildKey(FileCategory.APPLICATION, "resume.pdf");

			String[] parts = key.split("/");
			assertThat(parts[0]).isEqualTo("applications");
			assertThat(parts).hasSize(4);
			assertThat(key).endsWith("/resume.pdf");
		}

		@Test
		@DisplayName("카테고리가 다르면 prefix도 다르다")
		void differentCategory_differentPrefix() {
			String key = s3Service.buildKey(FileCategory.POST_IMAGE, "photo.png");

			assertThat(key).startsWith("images/posts/");
		}
	}

	@Nested
	@DisplayName("deleteObjects")
	class DeleteObjects {

		@Test
		@DisplayName("빈 목록이면 s3Client를 호출하지 않는다")
		void emptyList_doesNothing() {
			s3Service.deleteObjects(List.of());

			verify(s3Client, never()).deleteObject(any(DeleteObjectRequest.class));
		}

		@Test
		@DisplayName("null이면 s3Client를 호출하지 않는다")
		void nullList_doesNothing() {
			s3Service.deleteObjects(null);

			verify(s3Client, never()).deleteObject(any(DeleteObjectRequest.class));
		}

		@Test
		@DisplayName("각 key에 대해 삭제를 시도한다")
		void deletesEachKey() {
			s3Service.deleteObjects(List.of("key1", "key2"));

			verify(s3Client, times(2)).deleteObject(any(DeleteObjectRequest.class));
		}

		@Test
		@DisplayName("일부 삭제가 실패해도(S3Exception) 나머지는 계속 진행된다")
		void oneFails_othersContinue() {
			given(s3Client.deleteObject(any(DeleteObjectRequest.class)))
					.willThrow(S3Exception.builder().message("boom").build())
					.willReturn(null);

			s3Service.deleteObjects(List.of("key1", "key2"));

			verify(s3Client, times(2)).deleteObject(any(DeleteObjectRequest.class));
		}
	}

	@Nested
	@DisplayName("generatePresignedDownloadUrl / generatePresignedViewUrl")
	class PresignedUrls {

		@Test
		@DisplayName("다운로드 URL은 5분 유효기간으로 생성된다")
		void downloadUrl_generated() {
			PresignedGetObjectRequest presigned = mock(PresignedGetObjectRequest.class);
			given(presigned.url()).willReturn(url("https://s3.example.com/download"));
			given(presigner.presignGetObject(any(software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest.class)))
					.willReturn(presigned);

			String result = s3Service.generatePresignedDownloadUrl("some/key", "resume.pdf");

			assertThat(result).isEqualTo("https://s3.example.com/download");
		}

		@Test
		@DisplayName("뷰 URL은 24시간 유효기간으로 생성된다")
		void viewUrl_generated() {
			PresignedGetObjectRequest presigned = mock(PresignedGetObjectRequest.class);
			given(presigned.url()).willReturn(url("https://s3.example.com/view"));
			ArgumentCaptor<software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest> captor =
					ArgumentCaptor.forClass(software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest.class);
			given(presigner.presignGetObject(captor.capture())).willReturn(presigned);

			String result = s3Service.generatePresignedViewUrl("some/key");

			assertThat(result).isEqualTo("https://s3.example.com/view");
			assertThat(captor.getValue().signatureDuration()).isEqualTo(java.time.Duration.ofHours(24));
		}
	}
}
