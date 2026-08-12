package com.jam.comComment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.jam.comComment.dto.CommentDto;
import com.jam.comComment.mapper.CommentMapper;
import com.jam.community.repository.CommunityRepository;
import com.jam.global.exception.ForbiddenException;

/**
 * CommentService에 대한 Mockito 단위 테스트.
 * 단순 mapper 위임 메서드는 위임 확인용으로만 작성했고, XML에 있는 실제 SQL은 이 유닛테스트로 검증되지 않는다.
 */
@ExtendWith(MockitoExtension.class)
class CommentServiceUnitTest {

	@Mock
	private CommentMapper commentMapper;
	@Mock
	private CommunityRepository communityRepository;

	@InjectMocks
	private CommentService commentService;

	@Nested
	@DisplayName("commentList")
	class CommentList {

		@Test
		@DisplayName("mapper 결과 그대로 반환")
		void commentList_delegatesToMapper() {
			List<CommentDto> mockResult = List.of(new CommentDto());
			given(commentMapper.commentList(1L, "user1")).willReturn(mockResult);

			List<CommentDto> result = commentService.commentList(1L, "user1");

			assertThat(result).isEqualTo(mockResult);
		}
	}

	@Nested
	@DisplayName("insertComment")
	class InsertComment {

		@Test
		@DisplayName("댓글을 저장하기 전 게시글의 댓글 수를 증가시킨다")
		void insertComment_incrementsCommentCount_thenInserts() {
			CommentDto c = new CommentDto();
			c.setPost_id(1L);
			given(commentMapper.insertComment(c)).willReturn(1);

			int result = commentService.insertComment(c);

			assertThat(result).isEqualTo(1);
			verify(communityRepository).updateCommentCount(1L, 1);
			verify(commentMapper).insertComment(c);
		}
	}

	@Nested
	@DisplayName("updateComment")
	class UpdateComment {

		@Test
		@DisplayName("본인 댓글이면(1건) 정상적으로 수정된다")
		void updateComment_owner_updates() {
			CommentDto c = new CommentDto();
			given(commentMapper.updateComment(c)).willReturn(1);

			int result = commentService.updateComment(c);

			assertThat(result).isEqualTo(1);
		}

		@Test
		@DisplayName("본인 댓글이 아니면(0건) ForbiddenException을 던진다")
		void updateComment_notOwner_throwsForbidden() {
			CommentDto c = new CommentDto();
			given(commentMapper.updateComment(c)).willReturn(0);

			assertThatThrownBy(() -> commentService.updateComment(c))
					.isInstanceOf(ForbiddenException.class);
		}
	}

	@Nested
	@DisplayName("deleteComment")
	class DeleteComment {

		@Test
		@DisplayName("본인 댓글이면(1건) 삭제하고 게시글의 댓글 수를 감소시킨다")
		void deleteComment_owner_deletesAndDecrementsCount() {
			given(commentMapper.getPostIdByCommentId(1L)).willReturn(10L);
			given(commentMapper.deleteComment(1L, "user1")).willReturn(1);

			int result = commentService.deleteComment(1L, "user1");

			assertThat(result).isEqualTo(1);
			verify(communityRepository).updateCommentCount(10L, -1);
		}

		@Test
		@DisplayName("본인 댓글이 아니면(0건) ForbiddenException을 던지고 댓글 수를 감소시키지 않는다")
		void deleteComment_notOwner_throwsForbidden_doesNotDecrementCount() {
			given(commentMapper.getPostIdByCommentId(1L)).willReturn(10L);
			given(commentMapper.deleteComment(1L, "intruder")).willReturn(0);

			assertThatThrownBy(() -> commentService.deleteComment(1L, "intruder"))
					.isInstanceOf(ForbiddenException.class);

			verify(communityRepository, never()).updateCommentCount(any(), anyInt());
		}
	}
}
