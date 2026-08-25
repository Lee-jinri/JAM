package com.jam.community.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import com.jam.community.dto.CommunityDto;
import com.jam.community.entity.Community;
import com.jam.community.repository.CommunityRepository;
import com.jam.member.entity.Member;
import com.jam.member.repository.MemberRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * CommunityService에 대한 @SpringBootTest 통합 테스트. CommunityMapper.xml에 있는 실제 SQL을 검증한다.
 * MyBatis를 사용하는 getBoard/getBoardWithFavorite는 CommunityMapper.xml에 있는 실제 SQL을 검증.
 * JPA/QueryDSL을 사용하는 나머지 부분은 CommunityRepositoryTest에서 별도로 검증.
 */
@SpringBootTest
@Transactional // 이 클래스에서 새로 쓰는 데이터(D)는 테스트 후 자동 롤백되어 실DB를 오염시키지 않음
public class CommunityServiceIntegrationTest {

	@Autowired
    private CommunityService communityService;

	@Autowired
	private CommunityRepository communityRepository;

	@Autowired
	private MemberRepository memberRepository;

	@PersistenceContext
	private EntityManager entityManager;
	
	@Test
	@DisplayName("A: 검색어 없이 조회하면 전체 리스트를 반환한다")
	void getBoard_noKeyword() {
	    // Given
	    CommunityDto com = new CommunityDto();
	    com.setPageNum(1);
	    com.setKeyword(""); // 검색어 없음
	    
	    // When
	    List<CommunityDto> result = communityService.getBoard(com);
	    
	    // Then
	    assertThat(result).isNotNull();
	    assertThat(result).isNotEmpty(); // 데이터가 최소 1개는 있다고 가정
	}
	
	@Test
	@DisplayName("B: 키워드로 검색하면 제목이나 내용에 해당 키워드가 포함된 게시글만 반환한다"
			+ " (제목 검색 통합 인덱스는 커밋 시점 동기화 되지 않으므로 직접 동기화 후 시드한 글로 검증하고 테스트 후 되돌린다)")
	void getBoard_withKeyword() {
	    // Given
	    Member member = new Member();
	    member.setUserId("comKeywordTestUser");
	    member.setUserPw("pw1234");
	    member.setUserName("comKeywordTestUser_name");
	    member = memberRepository.save(member);

	    String uniqueKeyword = "키워드검색테스트유니크단어";
	    Community post = new Community();
	    post.setMember(member);
	    post.setTitle(uniqueKeyword + " 제목");
	    post.setContent("내용");
	    post.setViewCount(0);
	    post.setCommentCount(0);
	    post.setCreatedAt(LocalDateTime.now());
	    post = communityRepository.save(post);

	    TestTransaction.flagForCommit();
	    TestTransaction.end(); // COMMIT

	    // Oracle Text 인덱스 동기화
	    TestTransaction.start();
	    entityManager.createNativeQuery(
    	    "BEGIN CTXSYS.CTX_DDL.SYNC_INDEX('idx_comm_combined_text'); END;"
    	).executeUpdate();
	    TestTransaction.flagForCommit();
	    TestTransaction.end();

	    try {
	        // When
	        CommunityDto com = new CommunityDto();
	        com.setPageNum(1);
	        com.setKeyword(uniqueKeyword);

	        List<CommunityDto> result = communityService.getBoard(com);

	        // Then
	        assertThat(result).isNotEmpty();
	        assertThat(result).allMatch(p ->
	            p.getTitle().contains(uniqueKeyword) ||
	            p.getContent().contains(uniqueKeyword)
	        );
	    } finally {
	        // @Transactional 롤백은 이미 커밋된 row는 못 지우므로 직접 정리
	        TestTransaction.start();
	        communityRepository.deleteById(post.getPostId());
	        memberRepository.deleteById(member.getUserId());
	        TestTransaction.flagForCommit();
	        TestTransaction.end();
	        TestTransaction.start();
	    }
	}
	
	@Test
	@DisplayName("C: 일치하는 검색 결과가 없으면 빈 리스트를 반환한다")
	void getBoard_emptyResult() {
	    // Given
	    CommunityDto com = new CommunityDto();
	    com.setPageNum(1);
	    com.setKeyword("절대없을것같은검색어12345");
	    
	    // When
	    List<CommunityDto> result = communityService.getBoard(com);
	    
	    // Then
	    assertThat(result).isEmpty(); // 리스트가 null은 아니지만 비어있어야 함 (size=0)
	}
	
	@Test
	@DisplayName("D: 즐겨찾기한 글은 isFavorite=true, 안 한 글은 isFavorite=false로 정확히 반영된다")
	void getBoard_withUser() {
	    // Given: 회원 1명 + 글 2개(즐겨찾기 O / X)를 직접 시드하고, 그 중 하나만 즐겨찾기 등록
	    Member member = new Member();
	    member.setUserId("favTestUser");
	    member.setUserPw("pw1234");
	    member.setUserName("favTestUser_name");
	    // userId를 수동 할당했기 때문에 save()가 merge()로 동작해 새 관리 인스턴스를 반환함 - 반드시 반환값을 써야 함
	    member = memberRepository.save(member);

	    Community favoritedPost = new Community();
	    favoritedPost.setMember(member);
	    favoritedPost.setTitle("즐겨찾기한 글");
	    favoritedPost.setContent("내용");
	    favoritedPost.setViewCount(0);
	    favoritedPost.setCommentCount(0);
	    favoritedPost.setCreatedAt(LocalDateTime.now());
	    communityRepository.save(favoritedPost);

	    Community notFavoritedPost = new Community();
	    notFavoritedPost.setMember(member);
	    notFavoritedPost.setTitle("즐겨찾기 안 한 글");
	    notFavoritedPost.setContent("내용");
	    notFavoritedPost.setViewCount(0);
	    notFavoritedPost.setCommentCount(0);
	    notFavoritedPost.setCreatedAt(LocalDateTime.now());
	    communityRepository.save(notFavoritedPost);

	    // favorite 테이블은 JPA 엔티티가 없어 네이티브 쿼리로 직접 시드.
	    // FK(favorite.user_id -> member.user_id) 검증을 위해 먼저 flush로 member INSERT를 실제 반영
	    entityManager.flush();
	    entityManager.createNativeQuery(
	                "INSERT INTO favorite (favorite_id, user_id, post_id, board_type, created_at) "
	                        + "VALUES (seq_favorite.nextval, :userId, :postId, 'COM', SYSTIMESTAMP)")
	            .setParameter("userId", member.getUserId())
	            .setParameter("postId", favoritedPost.getPostId())
	            .executeUpdate();

	    // When
	    CommunityDto com = new CommunityDto();
	    com.setPageNum(1);
	    com.setUser_id(member.getUserId());

	    List<CommunityDto> result = communityService.getBoard(com);

	    // Then: 두 글 각각을 결과에서 찾아 즐겨찾기 여부가 정확히 반영됐는지 개별 확인
	    CommunityDto favoritedResult = result.stream()
	            .filter(post -> post.getPost_id().equals(favoritedPost.getPostId()))
	            .findFirst()
	            .orElseThrow(() -> new AssertionError("즐겨찾기한 글이 조회 결과에 없습니다."));
	    CommunityDto notFavoritedResult = result.stream()
	            .filter(post -> post.getPost_id().equals(notFavoritedPost.getPostId()))
	            .findFirst()
	            .orElseThrow(() -> new AssertionError("즐겨찾기 안 한 글이 조회 결과에 없습니다."));

	    assertThat(favoritedResult.isFavorite()).isTrue();
	    assertThat(notFavoritedResult.isFavorite()).isFalse();
	}
	
	@Test
	@DisplayName("E: 사용자 아이디가 없다면 모든 게시물의 즐겨찾기 여부는 false이다")
	void getBoard_noUser() {
	    CommunityDto com = new CommunityDto();
	    com.setPageNum(1);
	    
	    List<CommunityDto> result = communityService.getBoard(com);
	    
	    assertThat(result).isNotEmpty();
	    assertThat(result).allMatch(post -> post.isFavorite() == false);
	}
}
