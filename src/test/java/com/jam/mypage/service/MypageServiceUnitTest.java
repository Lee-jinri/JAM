package com.jam.mypage.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import com.jam.global.exception.ConflictException;
import com.jam.member.dto.MemberDto;
import com.jam.mypage.mapper.MypageMapper;

@ExtendWith(MockitoExtension.class)
class MypageServiceUnitTest {

	@Mock
	private MypageMapper mypageMapper;

	@InjectMocks
	private MypageService mypageService;

	@Nested
	@DisplayName("addFavorite")
	class AddFavorite {

		@Test
		@DisplayName("정상 추가되면(1건) true를 반환한다")
		void success_returnsTrue() {
			given(mypageMapper.addFavorite("user1", "JOB", 1L)).willReturn(1);

			assertThat(mypageService.addFavorite("user1", "JOB", 1L)).isTrue();
		}

		@Test
		@DisplayName("이미 즐겨찾기한 글이면(DuplicateKeyException) ConflictException을 던진다")
		void duplicate_throwsConflict() {
			given(mypageMapper.addFavorite("user1", "JOB", 1L))
					.willThrow(new DuplicateKeyException("unique constraint violated"));

			assertThatThrownBy(() -> mypageService.addFavorite("user1", "JOB", 1L))
					.isInstanceOf(ConflictException.class);
		}
	}

	@Nested
	@DisplayName("deleteFavorite")
	class DeleteFavorite {

		@Test
		@DisplayName("정상 삭제되면(1건) true를 반환한다")
		void success_returnsTrue() {
			given(mypageMapper.deleteFavorite("user1", "JOB", 1L)).willReturn(1);

			assertThat(mypageService.deleteFavorite("user1", "JOB", 1L)).isTrue();
		}

		@Test
		@DisplayName("삭제할 게 없으면(0건) false를 반환한다")
		void notFound_returnsFalse() {
			given(mypageMapper.deleteFavorite("user1", "JOB", 1L)).willReturn(0);

			assertThat(mypageService.deleteFavorite("user1", "JOB", 1L)).isFalse();
		}
	}

	@Nested
	@DisplayName("account")
	class Account {

		@Test
		@DisplayName("mapper 결과 그대로 반환한다")
		void delegatesToMapper() {
			MemberDto account = new MemberDto();
			given(mypageMapper.account("user1")).willReturn(account);

			assertThat(mypageService.account("user1")).isEqualTo(account);
		}
	}
}
