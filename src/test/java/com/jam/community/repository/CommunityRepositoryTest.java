package com.jam.community.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.jam.community.dto.CommunityListResponseDto;
import com.jam.community.entity.Community;
import com.jam.config.QuerydslConfig;
import com.jam.member.entity.Member;
import com.jam.member.repository.MemberRepository;

/**
 * CommunityRepository/Impl @DataJpaTest (Oracle 실DB, replace=NONE).
 * 커스텀 쿼리(@Query/@Modifying/QueryDSL)만 대상, 기본 CRUD는 제외.
 * 데이터는 테스트마다 직접 시드, 트랜잭션 롤백으로 격리.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Import(QuerydslConfig.class)
class CommunityRepositoryTest {

	@Autowired
	private CommunityRepository communityRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private TestEntityManager entityManager;

	private Member createMember(String userId) {
		return createMember(userId, "name_" + userId);
	}

	private Member createMember(String userId, String userName) {
		Member member = new Member();
		member.setUserId(userId);
		member.setUserPw("pw1234");
		member.setUserName(userName);
		return memberRepository.save(member);
	}

	private Community createPost(Member member, String title, int viewCount, int commentCount) {
		Community community = new Community();
		community.setMember(member);
		community.setTitle(title);
		community.setContent("content");
		community.setViewCount(viewCount);
		community.setCommentCount(commentCount);
		community.setCreatedAt(LocalDateTime.now());
		return communityRepository.save(community);
	}

	@Nested
	@DisplayName("getMyPosts (QueryDSL)")
	class GetMyPosts {

		@Test
		@DisplayName("해당 userId의 글만 조회된다")
		void getMyPosts_onlyOwnPosts() {
			Member owner = createMember("owner1");
			Member other = createMember("other1");
			createPost(owner, "내 글", 0, 0);
			createPost(other, "남의 글", 0, 0);

			Page<CommunityListResponseDto> result =
					communityRepository.getMyPosts("owner1", null, PageRequest.of(0, 10));

			assertThat(result.getContent())
					.extracting(CommunityListResponseDto::getTitle)
					.containsExactly("내 글");
		}

		@Test
		@DisplayName("keyword가 있으면 title에 포함된 글만 반환된다 (대소문자 무관)")
		void getMyPosts_filtersByKeyword_caseInsensitive() {
			Member owner = createMember("owner2");
			createPost(owner, "Guitar 튜닝 팁", 0, 0);
			createPost(owner, "베이스 이야기", 0, 0);

			Page<CommunityListResponseDto> result =
					communityRepository.getMyPosts("owner2", "guitar", PageRequest.of(0, 10));

			assertThat(result.getContent())
					.extracting(CommunityListResponseDto::getTitle)
					.containsExactly("Guitar 튜닝 팁");
		}

		@Test
		@DisplayName("keyword가 null이면 전체 글이 반환된다")
		void getMyPosts_nullKeyword_returnsAll() {
			Member owner = createMember("owner3");
			createPost(owner, "글1", 0, 0);
			createPost(owner, "글2", 0, 0);

			Page<CommunityListResponseDto> result =
					communityRepository.getMyPosts("owner3", null, PageRequest.of(0, 10));

			assertThat(result.getContent()).hasSize(2);
		}

		@Test
		@DisplayName("keyword가 빈 문자열이어도 전체 글이 반환된다")
		void getMyPosts_blankKeyword_returnsAll() {
			Member owner = createMember("owner4");
			createPost(owner, "글1", 0, 0);
			createPost(owner, "글2", 0, 0);

			Page<CommunityListResponseDto> result =
					communityRepository.getMyPosts("owner4", "", PageRequest.of(0, 10));

			assertThat(result.getContent()).hasSize(2);
		}

		@Test
		@DisplayName("postId 내림차순(최신순)으로 정렬된다")
		void getMyPosts_orderedByPostIdDesc() {
			Member owner = createMember("owner5");
			Community first = createPost(owner, "첫번째", 0, 0);
			Community second = createPost(owner, "두번째", 0, 0);

			Page<CommunityListResponseDto> result =
					communityRepository.getMyPosts("owner5", null, PageRequest.of(0, 10));

			assertThat(result.getContent())
					.extracting(CommunityListResponseDto::getPostId)
					.containsExactly(second.getPostId(), first.getPostId());
		}

		@Test
		@DisplayName("페이징이 정확히 적용된다")
		void getMyPosts_pagination() {
			Member owner = createMember("owner6");
			for (int i = 1; i <= 15; i++) {
				createPost(owner, "글" + i, 0, 0);
			}

			Page<CommunityListResponseDto> firstPage =
					communityRepository.getMyPosts("owner6", null, PageRequest.of(0, 10));
			Page<CommunityListResponseDto> secondPage =
					communityRepository.getMyPosts("owner6", null, PageRequest.of(1, 10));

			assertThat(firstPage.getContent()).hasSize(10);
			assertThat(secondPage.getContent()).hasSize(5);
		}

		@Test
		@DisplayName("total은 페이지 크기와 무관하게 조건에 맞는 전체 개수와 일치한다")
		void getMyPosts_totalMatchesFullCount() {
			Member owner = createMember("owner7");
			for (int i = 1; i <= 15; i++) {
				createPost(owner, "글" + i, 0, 0);
			}

			Page<CommunityListResponseDto> page =
					communityRepository.getMyPosts("owner7", null, PageRequest.of(0, 10));

			assertThat(page.getTotalElements()).isEqualTo(15);
		}

		@Test
		@DisplayName("join된 member 정보(userId, userName)가 정확히 채워진다")
		void getMyPosts_includesMemberInfo() {
			Member owner = createMember("owner8", "기타리스트");
			createPost(owner, "글", 0, 0);

			Page<CommunityListResponseDto> result =
					communityRepository.getMyPosts("owner8", null, PageRequest.of(0, 10));

			assertThat(result.getContent().get(0).getUserId()).isEqualTo("owner8");
			assertThat(result.getContent().get(0).getUserName()).isEqualTo("기타리스트");
		}

		@Test
		@DisplayName("조건에 맞는 글이 없으면 빈 Page와 total=0을 반환한다")
		void getMyPosts_noResult_emptyPage() {
			createMember("owner9");

			Page<CommunityListResponseDto> result =
					communityRepository.getMyPosts("owner9", null, PageRequest.of(0, 10));

			assertThat(result.getContent()).isEmpty();
			assertThat(result.getTotalElements()).isEqualTo(0);
		}
	}

	@Nested
	@DisplayName("incrementViewCount")
	class IncrementViewCount {

		@Test
		@DisplayName("호출한 글의 viewCount만 1 증가한다")
		void incrementViewCount_increasesOnlyTargetPost() {
			Member owner = createMember("viewOwner1");
			Community target = createPost(owner, "글", 5, 0);
			Community other = createPost(owner, "다른 글", 5, 0);

			communityRepository.incrementViewCount(target.getPostId());
			entityManager.clear();

			Community updated = communityRepository.findById(target.getPostId()).orElseThrow();
			Community untouched = communityRepository.findById(other.getPostId()).orElseThrow();

			assertThat(updated.getViewCount()).isEqualTo(6);
			assertThat(untouched.getViewCount()).isEqualTo(5);
		}
	}

	@Nested
	@DisplayName("updateCommentCount")
	class UpdateCommentCount {

		@Test
		@DisplayName("양수 amount면 commentCount가 증가한다")
		void updateCommentCount_increase() {
			Member owner = createMember("commentOwner1");
			Community post = createPost(owner, "글", 0, 3);

			communityRepository.updateCommentCount(post.getPostId(), 1);
			entityManager.clear();

			Community updated = communityRepository.findById(post.getPostId()).orElseThrow();
			assertThat(updated.getCommentCount()).isEqualTo(4);
		}

		@Test
		@DisplayName("음수 amount면 commentCount가 감소한다")
		void updateCommentCount_decrease() {
			Member owner = createMember("commentOwner2");
			Community post = createPost(owner, "글", 0, 3);

			communityRepository.updateCommentCount(post.getPostId(), -1);
			entityManager.clear();

			Community updated = communityRepository.findById(post.getPostId()).orElseThrow();
			assertThat(updated.getCommentCount()).isEqualTo(2);
		}
	}

	@Nested
	@DisplayName("deleteMyPosts")
	class DeleteMyPosts {

		@Test
		@DisplayName("지정한 postIds 중 해당 userId 소유인 것만 삭제된다")
		void deleteMyPosts_onlyOwnedByUser() {
			Member owner = createMember("delOwner1");
			Member other = createMember("delOther1");
			Community ownPost = createPost(owner, "내 글", 0, 0);
			Community otherPost = createPost(other, "남의 글", 0, 0);

			communityRepository.deleteMyPosts("delOwner1", List.of(ownPost.getPostId(), otherPost.getPostId()));
			entityManager.clear();

			assertThat(communityRepository.findById(ownPost.getPostId())).isEmpty();
			assertThat(communityRepository.findById(otherPost.getPostId())).isPresent();
		}

		@Test
		@DisplayName("여러 postId를 한 번에 삭제한다")
		void deleteMyPosts_multipleAtOnce() {
			Member owner = createMember("delOwner2");
			Community p1 = createPost(owner, "글1", 0, 0);
			Community p2 = createPost(owner, "글2", 0, 0);
			Community p3 = createPost(owner, "글3", 0, 0);

			communityRepository.deleteMyPosts("delOwner2", List.of(p1.getPostId(), p2.getPostId()));
			entityManager.clear();

			assertThat(communityRepository.findById(p1.getPostId())).isEmpty();
			assertThat(communityRepository.findById(p2.getPostId())).isEmpty();
			assertThat(communityRepository.findById(p3.getPostId())).isPresent();
		}
	}

	@Nested
	@DisplayName("countByPostIdInAndMember_UserId")
	class CountByPostIdInAndMemberUserId {

		@Test
		@DisplayName("postIds 전부가 해당 유저 소유면 postIds 개수를 그대로 반환한다")
		void countByPostIdInAndMemberUserId_allOwned() {
			Member owner = createMember("cntOwner1");
			Community p1 = createPost(owner, "글1", 0, 0);
			Community p2 = createPost(owner, "글2", 0, 0);

			long count = communityRepository.countByPostIdInAndMember_UserId(
					List.of(p1.getPostId(), p2.getPostId()), "cntOwner1");

			assertThat(count).isEqualTo(2);
		}

		@Test
		@DisplayName("일부만 소유면 소유한 개수만 반환한다")
		void countByPostIdInAndMemberUserId_partiallyOwned() {
			Member owner = createMember("cntOwner2");
			Member other = createMember("cntOther2");
			Community ownPost = createPost(owner, "내 글", 0, 0);
			Community otherPost = createPost(other, "남의 글", 0, 0);

			long count = communityRepository.countByPostIdInAndMember_UserId(
					List.of(ownPost.getPostId(), otherPost.getPostId()), "cntOwner2");

			assertThat(count).isEqualTo(1);
		}

		@Test
		@DisplayName("전부 소유가 아니면 0을 반환한다")
		void countByPostIdInAndMemberUserId_noneOwned() {
			createMember("cntOwner3");
			Member other = createMember("cntOther3");
			Community otherPost = createPost(other, "남의 글", 0, 0);

			long count = communityRepository.countByPostIdInAndMember_UserId(
					List.of(otherPost.getPostId()), "cntOwner3");

			assertThat(count).isEqualTo(0);
		}
	}
}
