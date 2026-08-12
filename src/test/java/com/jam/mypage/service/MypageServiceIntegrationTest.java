package com.jam.mypage.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.jam.global.exception.ConflictException;
import com.jam.member.dto.MemberDto;
import com.jam.member.entity.Member;
import com.jam.member.repository.MemberRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * MypageService에 대한 @SpringBootTest 통합테스트. MypageMapper.xml에 있는 실제 SQL과
 * favorite 테이블의 UNIQUE_FAVORITE(user_id, board_type, post_id) 제약 위반 시 동작을 검증한다.
 */
@SpringBootTest
@Transactional
class MypageServiceIntegrationTest {

	@Autowired
	private MypageService mypageService;

	@Autowired
	private MemberRepository memberRepository;

	@PersistenceContext
	private EntityManager entityManager;

	private int seq = 0;

	private Member seedMember(String prefix) {
		String userId = prefix + (++seq) + "_" + System.nanoTime() % 100000;
		Member member = new Member();
		member.setUserId(userId);
		member.setUserPw("pw1234");
		member.setUserName("n" + Integer.toHexString(userId.hashCode()));
		Member saved = memberRepository.save(member);
		entityManager.flush();
		return saved;
	}

	@Nested
	@DisplayName("addFavorite / deleteFavorite")
	class Favorite {

		@Test
		@DisplayName("정상적으로 추가되고 삭제된다")
		void addAndDelete_success() {
			Member member = seedMember("favUser");

			boolean added = mypageService.addFavorite(member.getUserId(), "JOB", 100L);
			assertThat(added).isTrue();

			boolean deleted = mypageService.deleteFavorite(member.getUserId(), "JOB", 100L);
			assertThat(deleted).isTrue();
		}

		@Test
		@DisplayName("같은 글을 두 번 즐겨찾기하면 ConflictException을 던진다")
		void duplicateFavorite_throwsConflict() {
			Member member = seedMember("favDupUser");
			mypageService.addFavorite(member.getUserId(), "JOB", 200L);

			assertThatThrownBy(() -> mypageService.addFavorite(member.getUserId(), "JOB", 200L))
					.isInstanceOf(ConflictException.class);
		}

		@Test
		@DisplayName("존재하지 않는 즐겨찾기를 삭제하면 false를 반환한다")
		void deleteNonExistentFavorite_returnsFalse() {
			Member member = seedMember("favDelUser");

			boolean deleted = mypageService.deleteFavorite(member.getUserId(), "JOB", 999L);

			assertThat(deleted).isFalse();
		}

		@Test
		@DisplayName("board_type이 다르면 별개의 즐겨찾기로 취급되어 둘 다 추가된다")
		void differentBoardType_bothAdded() {
			Member member = seedMember("favTypeUser");

			assertThat(mypageService.addFavorite(member.getUserId(), "JOB", 300L)).isTrue();
			assertThat(mypageService.addFavorite(member.getUserId(), "COM", 300L)).isTrue();
		}
	}

	@Nested
	@DisplayName("account")
	class Account {

		@Test
		@DisplayName("가입된 회원 정보를 조회한다")
		void returnsMemberInfo() {
			Member member = seedMember("accUser");

			MemberDto account = mypageService.account(member.getUserId());

			assertThat(account.getUser_id()).isEqualTo(member.getUserId());
			assertThat(account.getUser_name()).isEqualTo(member.getUserName());
		}

		@Test
		@DisplayName("존재하지 않는 사용자면 null을 반환한다")
		void nonExistentUser_returnsNull() {
			assertThat(mypageService.account("no-such-user-id")).isNull();
		}
	}
}
