package com.jam.mypage.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.jam.global.exception.BadRequestException;

class BoardTypeTest {

	@Test
	@DisplayName("실제 favorite.board_type 리터럴과 정확히 일치하는 값을 가진다")
	void values_matchRealDbLiterals() {
		assertThat(BoardType.JOB.getValue()).isEqualTo("JOB");
		assertThat(BoardType.COMMUNITY.getValue()).isEqualTo("COM");
		assertThat(BoardType.FLEA_MARKET.getValue()).isEqualTo("FLEA");
	}

	@Test
	@DisplayName("대소문자 구분 없이 매칭된다")
	void fromString_caseInsensitive() {
		assertThat(BoardType.fromString("job")).isEqualTo(BoardType.JOB);
		assertThat(BoardType.fromString("Job")).isEqualTo(BoardType.JOB);
		assertThat(BoardType.fromString("JOB")).isEqualTo(BoardType.JOB);
	}

	@Test
	@DisplayName("com/flea도 정확히 매칭된다")
	void fromString_otherTypes() {
		assertThat(BoardType.fromString("com")).isEqualTo(BoardType.COMMUNITY);
		assertThat(BoardType.fromString("flea")).isEqualTo(BoardType.FLEA_MARKET);
	}

	@Test
	@DisplayName("알 수 없는 값이면 BadRequestException을 던진다")
	void fromString_unknown_throws() {
		assertThatThrownBy(() -> BoardType.fromString("studio"))
				.isInstanceOf(BadRequestException.class);
		assertThatThrownBy(() -> BoardType.fromString("fleaMarket"))
				.isInstanceOf(BadRequestException.class);
		assertThatThrownBy(() -> BoardType.fromString("community"))
				.isInstanceOf(BadRequestException.class);
	}
}
