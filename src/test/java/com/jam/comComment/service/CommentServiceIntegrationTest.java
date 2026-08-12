package com.jam.comComment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.jam.comComment.dto.CommentDto;
import com.jam.community.entity.Community;
import com.jam.community.repository.CommunityRepository;
import com.jam.global.exception.ForbiddenException;
import com.jam.member.entity.Member;
import com.jam.member.repository.MemberRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * CommentService에 대한 @SpringBootTest 통합 테스트. ComCommentMapper.xml에 있는 실제 SQL과,
 * 댓글 작성/삭제가 Community.commentCount(JPA)에 미치는 실제 부수효과를 검증한다.
 */
@SpringBootTest
@Transactional
class CommentServiceIntegrationTest {

	@Autowired
	private CommentService commentService;

	@Autowired
	private CommunityRepository communityRepository;

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
		return memberRepository.save(member);
	}

	private Community seedPost(Member member) {
		Community post = new Community();
		post.setMember(member);
		post.setTitle("댓글 테스트용 글");
		post.setContent("내용");
		post.setViewCount(0);
		post.setCommentCount(0);
		post.setCreatedAt(LocalDateTime.now());
		Community saved = communityRepository.save(post);
		entityManager.flush();
		return saved;
	}

	private int currentCommentCount(Long postId) {
		entityManager.flush();
		entityManager.clear();
		return communityRepository.findById(postId).orElseThrow().getCommentCount();
	}

	@Nested
	@DisplayName("insertComment")
	class InsertComment {

		@Test
		@DisplayName("댓글을 작성하면 저장되고 게시글의 댓글 수가 1 증가한다")
		void insertComment_savesAndIncrementsCount() {
			Member member = seedMember("commentWriter");
			Community post = seedPost(member);

			CommentDto c = new CommentDto();
			c.setPost_id(post.getPostId());
			c.setUser_id(member.getUserId());
			c.setContent("첫 댓글입니다");

			commentService.insertComment(c);

			assertThat(c.getComment_id()).isNotNull();
			assertThat(currentCommentCount(post.getPostId())).isEqualTo(1);

			List<CommentDto> list = commentService.commentList(post.getPostId(), member.getUserId());
			assertThat(list).hasSize(1);
			assertThat(list.get(0).getContent()).isEqualTo("첫 댓글입니다");
			assertThat(list.get(0)).extracting(CommentDto::isAuthor).isEqualTo(true);
		}

		@Test
		@DisplayName("작성자가 아닌 다른 사용자가 조회하면 isAuthor가 false다")
		void insertComment_otherUserViews_authorFalse() {
			Member writer = seedMember("commentWriter");
			Member viewer = seedMember("commentViewer");
			Community post = seedPost(writer);

			CommentDto c = new CommentDto();
			c.setPost_id(post.getPostId());
			c.setUser_id(writer.getUserId());
			c.setContent("댓글");
			commentService.insertComment(c);

			List<CommentDto> list = commentService.commentList(post.getPostId(), viewer.getUserId());

			assertThat(list.get(0).isAuthor()).isFalse();
		}
	}

	@Nested
	@DisplayName("updateComment")
	class UpdateComment {

		@Test
		@DisplayName("작성자 본인이 수정하면 내용이 반영된다")
		void updateComment_owner_updatesContent() {
			Member member = seedMember("commentEditOwner");
			Community post = seedPost(member);
			CommentDto c = new CommentDto();
			c.setPost_id(post.getPostId());
			c.setUser_id(member.getUserId());
			c.setContent("수정 전");
			commentService.insertComment(c);

			CommentDto edit = new CommentDto();
			edit.setComment_id(c.getComment_id());
			edit.setUser_id(member.getUserId());
			edit.setContent("수정 후");

			commentService.updateComment(edit);

			List<CommentDto> list = commentService.commentList(post.getPostId(), member.getUserId());
			assertThat(list.get(0).getContent()).isEqualTo("수정 후");
		}

		@Test
		@DisplayName("작성자 본인이 아니면 ForbiddenException을 던지고 내용이 바뀌지 않는다")
		void updateComment_notOwner_throwsForbidden_doesNotChangeContent() {
			Member owner = seedMember("commentEditOwner");
			Community post = seedPost(owner);
			CommentDto c = new CommentDto();
			c.setPost_id(post.getPostId());
			c.setUser_id(owner.getUserId());
			c.setContent("원본 내용");
			commentService.insertComment(c);

			CommentDto edit = new CommentDto();
			edit.setComment_id(c.getComment_id());
			edit.setUser_id("intruder");
			edit.setContent("해킹된 내용");

			assertThatThrownBy(() -> commentService.updateComment(edit))
					.isInstanceOf(ForbiddenException.class);

			List<CommentDto> list = commentService.commentList(post.getPostId(), owner.getUserId());
			assertThat(list.get(0).getContent()).isEqualTo("원본 내용");
		}
	}

	@Nested
	@DisplayName("deleteComment")
	class DeleteComment {

		@Test
		@DisplayName("작성자 본인이 삭제하면 댓글이 사라지고 게시글의 댓글 수가 1 감소한다")
		void deleteComment_owner_deletesAndDecrementsCount() {
			Member member = seedMember("commentDeleteOwner");
			Community post = seedPost(member);
			CommentDto c = new CommentDto();
			c.setPost_id(post.getPostId());
			c.setUser_id(member.getUserId());
			c.setContent("삭제될 댓글");
			commentService.insertComment(c);
			assertThat(currentCommentCount(post.getPostId())).isEqualTo(1);

			commentService.deleteComment(c.getComment_id(), member.getUserId());

			assertThat(currentCommentCount(post.getPostId())).isEqualTo(0);
			assertThat(commentService.commentList(post.getPostId(), member.getUserId())).isEmpty();
		}

		@Test
		@DisplayName("작성자 본인이 아니면 ForbiddenException을 던지고 댓글도, 댓글 수도 그대로다")
		void deleteComment_notOwner_throwsForbidden_doesNotDeleteOrDecrementCount() {
			Member owner = seedMember("commentDeleteOwner");
			Community post = seedPost(owner);
			CommentDto c = new CommentDto();
			c.setPost_id(post.getPostId());
			c.setUser_id(owner.getUserId());
			c.setContent("지켜지는 댓글");
			commentService.insertComment(c);
			assertThat(currentCommentCount(post.getPostId())).isEqualTo(1);

			assertThatThrownBy(() -> commentService.deleteComment(c.getComment_id(), "intruder"))
					.isInstanceOf(ForbiddenException.class);

			assertThat(currentCommentCount(post.getPostId())).isEqualTo(1);
			assertThat(commentService.commentList(post.getPostId(), owner.getUserId())).hasSize(1);
		}
	}
}
