package com.jam.file.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mybatis.spring.SqlSessionTemplate;

import com.jam.file.dto.FileAssetDto;
import com.jam.file.dto.FileCategory;
import com.jam.file.mapper.FileAssetMapper;
import com.jam.global.util.FileUtils;
import com.jam.s3.service.S3Service;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

	@Mock
	private FileUtils fileUtils;
	@Mock
	private SqlSessionTemplate batchSqlSessionTemplate;
	@Mock
	private FileAssetMapper fileMapper;
	@Mock
	private S3Service s3Service;

	@InjectMocks
	private FileService fileService;

	@Nested
	@DisplayName("insertFiles")
	class InsertFiles {

		@Test
		@DisplayName("각 파일을 검증/정규화하고 postId와 post_type을 세팅해 배치 insert한다")
		void validatesSanitizesAndInsertsEach() {
			FileAssetDto file = new FileAssetDto();
			file.setFile_name("photo.png");
			file.setFile_type("image/png");
			file.setFile_size(1000L);
			file.setFile_category(FileCategory.POST_IMAGE);
			given(fileUtils.sanitizeFilename("photo.png")).willReturn("photo.png");

			fileService.insertFiles(new ArrayList<>(List.of(file)), 10L);

			verify(fileUtils).validateFileType("photo.png", "image/png", FileCategory.POST_IMAGE);
			verify(fileUtils).validateFileSize(1000L, FileCategory.POST_IMAGE);
			assertThat(file.getPost_id()).isEqualTo(10L);
			assertThat(file.getPost_type()).isEqualTo("POST_IMAGE");
			verify(batchSqlSessionTemplate).insert(eq("com.jam.file.mapper.FileAssetMapper.insertFileAsset"), eq(file));
		}
	}

	@Nested
	@DisplayName("deleteFiles")
	class DeleteFiles {

		@Test
		@DisplayName("APPLICATION 타입이면 파일이 없어도 삭제를 계속 진행한다")
		void applicationType_noFiles_stillProceeds() {
			FileAssetDto param = new FileAssetDto();
			param.setPost_type("APPLICATION");
			given(fileMapper.getFilesByPost(param)).willReturn(List.of());

			fileService.deleteFiles(param);

			verify(fileMapper).deleteFiles(List.of());
		}

		@Test
		@DisplayName("POST_IMAGE 타입이고 파일이 없으면 아무 것도 하지 않고 종료한다")
		void postImageType_noFiles_returnsEarly() {
			FileAssetDto param = new FileAssetDto();
			param.setPost_type("POST_IMAGE");
			given(fileMapper.getFilesByPost(param)).willReturn(List.of());

			fileService.deleteFiles(param);

			verify(fileMapper, never()).deleteFiles(any());
			verify(s3Service, never()).deleteObjects(any());
		}

		@Test
		@DisplayName("파일이 있으면 DB에서 삭제하고 S3에서도 삭제를 시도한다")
		void filesExist_deletesFromDbAndS3() {
			FileAssetDto param = new FileAssetDto();
			param.setPost_type("POST_IMAGE");
			FileAssetDto existing = new FileAssetDto();
			existing.setFile_key("some/key.png");
			given(fileMapper.getFilesByPost(param)).willReturn(List.of(existing));

			fileService.deleteFiles(param);

			verify(fileMapper).deleteFiles(List.of(existing));
			verify(s3Service).deleteObjects(List.of("some/key.png"));
		}

		@Test
		@DisplayName("S3 삭제 중 예외가 발생해도 전파되지 않는다")
		void s3DeleteFails_doesNotPropagate() {
			FileAssetDto param = new FileAssetDto();
			param.setPost_type("POST_IMAGE");
			FileAssetDto existing = new FileAssetDto();
			existing.setFile_key("some/key.png");
			given(fileMapper.getFilesByPost(param)).willReturn(List.of(existing));
			org.mockito.Mockito.doThrow(new RuntimeException("S3 down")).when(s3Service).deleteObjects(any());

			fileService.deleteFiles(param);

			verify(fileMapper).deleteFiles(List.of(existing));
		}
	}

	@Nested
	@DisplayName("deleteFilesByKeys")
	class DeleteFilesByKeys {

		@Test
		@DisplayName("빈 목록이면 아무 것도 하지 않는다")
		void emptyKeys_doesNothing() {
			fileService.deleteFilesByKeys(List.of());

			verify(batchSqlSessionTemplate, never()).delete(anyString(), any());
			verify(s3Service, never()).deleteObjects(any());
		}

		@Test
		@DisplayName("각 key에 대해 DB 삭제 후 S3에서도 삭제한다")
		void deletesEachKeyFromDbAndS3() {
			fileService.deleteFilesByKeys(List.of("key1", "key2"));

			verify(batchSqlSessionTemplate, org.mockito.Mockito.times(2))
					.delete(eq("com.jam.file.mapper.FileAssetMapper.deleteFilesByKeys"), any());
			verify(s3Service).deleteObjects(List.of("key1", "key2"));
		}
	}
}
