package com.jam.fleaMarket.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import com.jam.fleaMarket.dto.FleaMarketDto;
import com.jam.fleaMarket.mapper.FleaMarketMapper;
import com.jam.global.exception.ForbiddenException;
import com.jam.member.entity.Member;
import com.jam.member.repository.MemberRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * FleaMarketService에 대한 @SpringBootTest 통합 테스트. FleaMarketMapper.xml에 있는 실제 SQL을 검증한다.
 */
@SpringBootTest
@Transactional // 이 클래스에서 새로 쓰는 데이터는 테스트 후 자동 롤백되어 실DB를 오염시키지 않음
class FleaMarketServiceIntegrationTest {

	@Autowired
	private FleaMarketService fleaMarketService;

	@Autowired
	private FleaMarketMapper fleaMapper;

	@Autowired
	private MemberRepository memberRepository;

	@PersistenceContext
	private EntityManager entityManager;

	// 기존 flea_category에 이미 존재하는 카테고리(통기타류)를 재사용 - FK 위반을 피하기 위함
	private static final int EXISTING_CATEGORY_ID = 11;

	private Member seedMember(String userId) {
		Member member = new Member();
		member.setUserId(userId);
		member.setUserPw("pw1234");
		member.setUserName(userId + "_name");
		// FK(fleaMarket.user_id -> member.user_id) 검증을 위해 즉시 flush로 member INSERT를 실제 반영
		Member saved = memberRepository.save(member);
		entityManager.flush();
		return saved;
	}

	private long seedFleaPost(String userId, String title, int price) {
		FleaMarketDto flea = new FleaMarketDto();
		flea.setTitle(title);
		flea.setContent("통합테스트 시드 데이터");
		flea.setCategory_id(EXISTING_CATEGORY_ID);
		flea.setPrice(price);
		flea.setUser_id(userId);
		flea.setThumbnail("test-thumbnail.png");

		long postId = fleaMapper.getNextPostId();
		flea.setPost_id(postId);
		fleaMapper.writePost(flea);
		return postId;
	}

	private void seedFavorite(String userId, long postId) {
		entityManager.flush();
		entityManager.createNativeQuery(
				"INSERT INTO favorite (favorite_id, user_id, post_id, board_type, created_at) "
						+ "VALUES (seq_favorite.nextval, :userId, :postId, 'FLEA', SYSTIMESTAMP)")
				.setParameter("userId", userId)
				.setParameter("postId", postId)
				.executeUpdate();
	}

	@Nested
	@DisplayName("getBoard")
	class GetBoard {

		@Test
		@DisplayName("A: 검색어 없이 조회하면 전체 리스트를 반환한다")
		void getBoard_noKeyword() {
			FleaMarketDto flea = new FleaMarketDto();
			flea.setPageNum(1);
			flea.setKeyword("");

			List<FleaMarketDto> result = fleaMarketService.getBoard(flea);

			assertThat(result).isNotEmpty();
		}

		@Test
		@DisplayName("B: 키워드로 검색하면 제목에 해당 키워드가 포함된 게시글만 반환한다"
				+ " (CONTAINS 인덱스는 COMMIT 시점에 동기화되므로, 트랜잭션을 실제로 커밋해서 직접 시드한 글로 검증하고 테스트 후 되돌린다)")
		void getBoard_withKeyword() {
			Member member = seedMember("fleaKeywordTestUser");
			String uniqueKeyword = "키워드검색테스트유니크단어";
			long postId = seedFleaPost(member.getUserId(), uniqueKeyword + " 판매합니다", 10000);

			TestTransaction.flagForCommit();
			TestTransaction.end(); // 실제 COMMIT (Oracle Text 인덱스 동기화 트리거)

			try {
				FleaMarketDto flea = new FleaMarketDto();
				flea.setPageNum(1);
				flea.setKeyword(uniqueKeyword);

				List<FleaMarketDto> result = fleaMarketService.getBoard(flea);

				assertThat(result).isNotEmpty();
				assertThat(result).allMatch(post -> post.getTitle().contains(uniqueKeyword));
			} finally {
				// @Transactional 롤백은 이미 커밋된 row는 못 지우므로 직접 정리
				TestTransaction.start();
				entityManager.createNativeQuery("DELETE FROM fleaMarket WHERE post_id = :postId")
						.setParameter("postId", postId)
						.executeUpdate();
				entityManager.createNativeQuery("DELETE FROM member WHERE user_id = :userId")
						.setParameter("userId", member.getUserId())
						.executeUpdate();
				TestTransaction.flagForCommit();
				TestTransaction.end();
				TestTransaction.start();
			}
		}

		@Test
		@DisplayName("C: 일치하는 검색 결과가 없으면 빈 리스트를 반환한다")
		void getBoard_emptyResult() {
			FleaMarketDto flea = new FleaMarketDto();
			flea.setPageNum(1);
			flea.setKeyword("절대없을것같은검색어12345");

			List<FleaMarketDto> result = fleaMarketService.getBoard(flea);

			assertThat(result).isEmpty();
		}

		@Test
		@DisplayName("D: 즐겨찾기한 글은 isFavorite=true, 안 한 글은 isFavorite=false로 정확히 반영된다")
		void getBoard_withUser_favoriteReflected() {
			Member member = seedMember("fleaFavTestUser");

			long favoritedPostId = seedFleaPost(member.getUserId(), "즐겨찾기한 글", 10000);
			long notFavoritedPostId = seedFleaPost(member.getUserId(), "즐겨찾기 안 한 글", 20000);
			seedFavorite(member.getUserId(), favoritedPostId);

			FleaMarketDto flea = new FleaMarketDto();
			flea.setPageNum(1);
			flea.setUser_id(member.getUserId());

			List<FleaMarketDto> result = fleaMarketService.getBoard(flea);

			FleaMarketDto favoritedResult = result.stream()
					.filter(post -> post.getPost_id().equals(favoritedPostId))
					.findFirst()
					.orElseThrow(() -> new AssertionError("즐겨찾기한 글이 조회 결과에 없습니다."));
			FleaMarketDto notFavoritedResult = result.stream()
					.filter(post -> post.getPost_id().equals(notFavoritedPostId))
					.findFirst()
					.orElseThrow(() -> new AssertionError("즐겨찾기 안 한 글이 조회 결과에 없습니다."));

			assertThat(favoritedResult.isFavorite()).isTrue();
			assertThat(notFavoritedResult.isFavorite()).isFalse();
		}

		@Test
		@DisplayName("E: 사용자 아이디가 없으면 모든 게시물의 즐겨찾기 여부는 false이다")
		void getBoard_noUser_allFalse() {
			FleaMarketDto flea = new FleaMarketDto();
			flea.setPageNum(1);

			List<FleaMarketDto> result = fleaMarketService.getBoard(flea);

			assertThat(result).isNotEmpty();
			assertThat(result).allMatch(post -> !post.isFavorite());
		}
	}

	@Nested
	@DisplayName("listCnt")
	class ListCnt {

		@Test
		@DisplayName("검색조건 없이 새로 등록한 글 수만큼 카운트가 증가한다")
		void listCnt_increasesByNumberOfNewPosts() {
			FleaMarketDto flea = new FleaMarketDto();
			int before = fleaMarketService.listCnt(flea);

			Member member = seedMember("fleaCntTestUser");
			seedFleaPost(member.getUserId(), "카운트 테스트 글 1", 1000);
			seedFleaPost(member.getUserId(), "카운트 테스트 글 2", 2000);
			seedFleaPost(member.getUserId(), "카운트 테스트 글 3", 3000);

			int after = fleaMarketService.listCnt(flea);

			assertThat(after - before).isEqualTo(3);
		}
	}

	@Nested
	@DisplayName("getPostDetail")
	class GetPostDetail {

		@Test
		@DisplayName("상세 조회 시 제목/가격 등 필드와 즐겨찾기 여부가 정확히 반영된다")
		void getPostDetail_returnsFieldsAndFavoriteFlag() {
			Member owner = seedMember("fleaDetailOwner");
			Member favoriter = seedMember("fleaDetailFavoriter");
			long postId = seedFleaPost(owner.getUserId(), "상세조회 테스트 글", 55000);
			seedFavorite(favoriter.getUserId(), postId);

			FleaMarketDto asFavoriter = new FleaMarketDto();
			asFavoriter.setPost_id(postId);
			asFavoriter.setUser_id(favoriter.getUserId());
			FleaMarketDto favoriterView = fleaMarketService.getPostDetail(asFavoriter);

			FleaMarketDto asStranger = new FleaMarketDto();
			asStranger.setPost_id(postId);
			asStranger.setUser_id("someoneElse");
			FleaMarketDto strangerView = fleaMarketService.getPostDetail(asStranger);

			assertThat(favoriterView.getTitle()).isEqualTo("상세조회 테스트 글");
			assertThat(favoriterView.getPrice()).isEqualTo(55000);
			assertThat(favoriterView.getUser_id()).isEqualTo(owner.getUserId());
			assertThat(favoriterView.isFavorite()).isTrue();
			assertThat(strangerView.isFavorite()).isFalse();
		}
	}

	@Nested
	@DisplayName("incrementReadCnt")
	class IncrementReadCnt {

		@Test
		@DisplayName("호출할 때마다 조회수가 1씩 증가한다")
		void incrementReadCnt_increasesViewCount() {
			Member member = seedMember("fleaViewCntUser");
			long postId = seedFleaPost(member.getUserId(), "조회수 테스트 글", 1000);

			FleaMarketDto param = new FleaMarketDto();
			param.setPost_id(postId);

			fleaMarketService.incrementReadCnt(postId);
			fleaMarketService.incrementReadCnt(postId);

			FleaMarketDto detail = fleaMarketService.getPostDetail(param);
			assertThat(detail.getView_count()).isEqualTo(2);
		}
	}

	@Nested
	@DisplayName("getMyStore / getMyStoreCnt")
	class MyStore {

		@Test
		@DisplayName("내가 쓴 글만 조회되고, 키워드로 필터링된다")
		void getMyStore_returnsOnlyOwnPostsFilteredByKeyword() {
			Member me = seedMember("fleaMyStoreUser");
			Member other = seedMember("fleaMyStoreOtherUser");

			seedFleaPost(me.getUserId(), "마이스토어 기타 판매합니다", 10000);
			seedFleaPost(me.getUserId(), "마이스토어 피아노 판매합니다", 20000);
			seedFleaPost(other.getUserId(), "마이스토어 기타 판매합니다(타인글)", 30000);

			FleaMarketDto param = new FleaMarketDto();
			param.setPageNum(1);
			param.setUser_id(me.getUserId());
			param.setKeyword("기타");

			List<FleaMarketDto> result = fleaMarketService.getMyStore(param);
			int cnt = fleaMarketService.getMyStoreCnt(param);

			assertThat(result).hasSize(1);
			assertThat(result.get(0).getTitle()).isEqualTo("마이스토어 기타 판매합니다");
			assertThat(cnt).isEqualTo(1);
		}
	}

	@Nested
	@DisplayName("getFavorites")
	class GetFavorites {

		@Test
		@DisplayName("내가 즐겨찾기한 글만 반환한다")
		void getFavorites_returnsOnlyFavoritedPosts() {
			Member member = seedMember("fleaFavoritesUser");

			long favoritedPostId = seedFleaPost(member.getUserId(), "즐겨찾기 목록 테스트 글", 15000);
			seedFleaPost(member.getUserId(), "즐겨찾기 안 한 목록 테스트 글", 25000);
			seedFavorite(member.getUserId(), favoritedPostId);

			FleaMarketDto param = new FleaMarketDto();
			param.setPageNum(1);
			param.setUser_id(member.getUserId());

			List<FleaMarketDto> result = fleaMarketService.getFavorites(param);
			int cnt = fleaMarketService.getFavoritesCnt(param);

			assertThat(result).extracting(FleaMarketDto::getPost_id).containsExactly(favoritedPostId);
			assertThat(cnt).isEqualTo(1);
		}
	}

	@Nested
	@DisplayName("getWriterIdByPostId / findOwnerAndThumbnailByPostId")
	class OwnerLookup {

		@Test
		@DisplayName("작성자 id와 소유자/썸네일 정보를 정확히 조회한다")
		void returnsOwnerInfo() {
			Member member = seedMember("fleaOwnerLookupUser");
			long postId = seedFleaPost(member.getUserId(), "소유자 조회 테스트 글", 5000);

			assertThat(fleaMarketService.getWriterIdByPostId(postId)).isEqualTo(member.getUserId());

			FleaMarketDto owner = fleaMapper.findOwnerAndThumbnailByPostId(postId);
			assertThat(owner.getUser_id()).isEqualTo(member.getUserId());
		}
	}

	@Nested
	@DisplayName("deletePost")
	class DeletePost {

		@Test
		@DisplayName("작성자 본인이면 글이 삭제된다")
		void deletePost_ownerDeletes_postRemoved() {
			Member member = seedMember("fleaDeleteOwnerUser");
			long postId = seedFleaPost(member.getUserId(), "삭제 테스트 글", 5000);

			fleaMarketService.deletePost(postId, member.getUserId());

			assertThat(fleaMarketService.getWriterIdByPostId(postId)).isNull();
		}

		@Test
		@DisplayName("작성자 본인이 아니면 ForbiddenException을 던지고 글은 삭제되지 않는다")
		void deletePost_notOwner_throwsForbidden_postRemains() {
			Member member = seedMember("fleaDeleteVictimUser");
			long postId = seedFleaPost(member.getUserId(), "삭제 방어 테스트 글", 5000);

			assertThatThrownBy(() -> fleaMarketService.deletePost(postId, "intruder"))
					.isInstanceOf(ForbiddenException.class);

			assertThat(fleaMarketService.getWriterIdByPostId(postId)).isEqualTo(member.getUserId());
		}
	}
}
