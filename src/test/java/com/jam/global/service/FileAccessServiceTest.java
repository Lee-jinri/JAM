package com.jam.global.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.jam.global.exception.ForbiddenException;
import com.jam.global.mapper.FileAccessMapper;

/**
 * FileAccessService에 대한 단위 테스트.
 * existsFileAccess의 실제 권한 거부 처리가 주석 처리되어 있어 access==0이어도 항상 true를
 * 반환하던 버그(다운로드 권한 우회)를 수정한 뒤 작성한 회귀 테스트.
 */
@ExtendWith(MockitoExtension.class)
class FileAccessServiceTest {

	@Mock
	private FileAccessMapper fileAccessMapper;

	@InjectMocks
	private FileAccessService fileAccessService;

	@Test
	@DisplayName("접근 권한이 있으면(1건) true를 반환한다")
	void hasAccess_returnsTrue() {
		given(fileAccessMapper.existsFileAccess("user1", 1L)).willReturn(1);

		assertThat(fileAccessService.existsFileAccess("user1", 1L)).isTrue();
	}

	@Test
	@DisplayName("접근 권한이 없으면(0건) ForbiddenException을 던진다")
	void noAccess_throwsForbidden() {
		given(fileAccessMapper.existsFileAccess("intruder", 1L)).willReturn(0);

		assertThatThrownBy(() -> fileAccessService.existsFileAccess("intruder", 1L))
				.isInstanceOf(ForbiddenException.class);
	}
}
