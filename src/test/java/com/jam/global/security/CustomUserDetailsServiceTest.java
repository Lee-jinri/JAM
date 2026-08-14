package com.jam.global.security;

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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.jam.member.dto.MemberDto;
import com.jam.member.mapper.MemberMapper;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

	@Mock
	private MemberMapper memberMapper;

	@InjectMocks
	private CustomUserDetailsService userDetailsService;

	@Nested
	@DisplayName("loadUserByUsername")
	class LoadUserByUsername {

		@Test
		@DisplayName("사용자가 존재하면 MemberDto를 UserDetails로 반환한다")
		void userExists_returnsUserDetails() {
			MemberDto member = new MemberDto();
			member.setUser_id("user1");
			given(memberMapper.findByUserInfo("user1")).willReturn(member);

			UserDetails result = userDetailsService.loadUserByUsername("user1");

			assertThat(result).isSameAs(member);
			assertThat(result.getUsername()).isEqualTo("user1");
		}

		@Test
		@DisplayName("사용자가 없으면 UsernameNotFoundException을 던진다")
		void userNotFound_throwsException() {
			given(memberMapper.findByUserInfo("ghost")).willReturn(null);

			assertThatThrownBy(() -> userDetailsService.loadUserByUsername("ghost"))
					.isInstanceOf(UsernameNotFoundException.class)
					.hasMessageContaining("ghost");
		}
	}
}
