package com.jam.global.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;

class JsonUtilsTest {

	static class SampleDto {
		private String name;
		private LocalDateTime createdAt;

		public SampleDto() {}

		public SampleDto(String name, LocalDateTime createdAt) {
			this.name = name;
			this.createdAt = createdAt;
		}

		public String getName() { return name; }
		public void setName(String name) { this.name = name; }
		public LocalDateTime getCreatedAt() { return createdAt; }
		public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
	}

	@Nested
	@DisplayName("toJson / fromJson")
	class RoundTrip {

		@Test
		@DisplayName("객체를 JSON 문자열로 직렬화한다")
		void toJson_serializesObject() throws JsonProcessingException {
			SampleDto dto = new SampleDto("이름", LocalDateTime.of(2026, 8, 15, 10, 30));

			String json = JsonUtils.toJson(dto);

			assertThat(json).contains("\"name\":\"이름\"");
		}

		@Test
		@DisplayName("JavaTimeModule이 등록되어 있어 LocalDateTime을 직렬화/역직렬화할 수 있다")
		void handlesLocalDateTime() throws JsonProcessingException {
			LocalDateTime createdAt = LocalDateTime.of(2026, 8, 15, 10, 30);
			SampleDto dto = new SampleDto("이름", createdAt);

			String json = JsonUtils.toJson(dto);
			SampleDto result = JsonUtils.fromJson(json, SampleDto.class);

			assertThat(result.getCreatedAt()).isEqualTo(createdAt);
		}

		@Test
		@DisplayName("JSON 문자열을 지정한 타입으로 역직렬화한다")
		void fromJson_deserializesToType() throws JsonProcessingException {
			SampleDto result = JsonUtils.fromJson("{\"name\":\"이름\"}", SampleDto.class);

			assertThat(result.getName()).isEqualTo("이름");
		}

		@Test
		@DisplayName("형식이 깨진 JSON이면 JsonProcessingException을 던진다")
		void malformedJson_throws() {
			assertThatThrownBy(() -> JsonUtils.fromJson("not-json", SampleDto.class))
					.isInstanceOf(JsonProcessingException.class);
		}

		@Test
		@DisplayName("타입이 맞지 않으면 MismatchedInputException을 던진다")
		void typeMismatch_throws() {
			assertThatThrownBy(() -> JsonUtils.fromJson("[1,2,3]", SampleDto.class))
					.isInstanceOf(MismatchedInputException.class);
		}
	}
}
