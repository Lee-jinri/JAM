package com.jam.community.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.jam.common.dto.PageDto;
import com.jam.community.dto.CommunityDetailResponseDto;
import com.jam.community.dto.CommunityDto;
import com.jam.community.dto.CommunityEditRequestDto;
import com.jam.community.dto.CommunityListResponseDto;
import com.jam.community.dto.CommunityWriteRequestDto;
import com.jam.community.entity.Community;
import com.jam.community.mapper.CommunityMapper;
import com.jam.community.repository.CommunityRepository;
import com.jam.file.dto.FileAssetDto;
import com.jam.file.dto.FileCategory;
import com.jam.global.exception.ForbiddenException;
import com.jam.global.exception.NotFoundException;
import com.jam.global.service.FileReferenceService;
import com.jam.global.service.PostImageViewService;
import com.jam.member.entity.Member;
import com.jam.member.repository.MemberRepository;

/**
 * CommunityService에 대한 Mockito 단위 테스트.
 * listCnt/getPopularBoard/getFavorites/favoritesListCnt는 mapper 위임 확인용으로만 작성
 * XML에 있는 실제 SQL 로직은 이 유닛테스트로 검증되지 않음.
 */
@ExtendWith(MockitoExtension.class)
class CommunityServiceUnitTest {

	@Mock
	private CommunityMapper comMapper;
	@Mock
	private PostImageViewService imageViewService;
	@Mock
	private FileReferenceService fileRefService;
	@Mock
	private CommunityRepository communityRepository;
	@Mock
	private MemberRepository memberRepository;

	@InjectMocks
	private CommunityService communityService;

	private Member member;

	@BeforeEach
	void setUp() {
		member = new Member();
		member.setUserId("user1");
		member.setUserName("tester");
	}

	@Nested
	@DisplayName("writePost")
	class WritePost {

		@Test
		@DisplayName("존재하는 회원이면 글을 저장하고 postId를 반환한다")
		void writePost_success() {
			CommunityWriteRequestDto dto = new CommunityWriteRequestDto();
			dto.setTitle("제목");
			dto.setContent("내용");

			given(memberRepository.findById("user1")).willReturn(Optional.of(member));

			Community saved = new Community();
			saved.setPostId(10L);
			given(communityRepository.save(any(Community.class))).willReturn(saved);

			Long postId = communityService.writePost("user1", dto);

			assertThat(postId).isEqualTo(10L);

			ArgumentCaptor<Community> captor = ArgumentCaptor.forClass(Community.class);
			verify(communityRepository).save(captor.capture());
			Community persisted = captor.getValue();
			assertThat(persisted.getTitle()).isEqualTo("제목");
			assertThat(persisted.getContent()).isEqualTo("내용");
			assertThat(persisted.getViewCount()).isEqualTo(0);
			assertThat(persisted.getCommentCount()).isEqualTo(0);
			assertThat(persisted.getCreatedAt()).isNotNull();
			assertThat(persisted.getMember()).isEqualTo(member);
		}

		@Test
		@DisplayName("존재하지 않는 회원이면 예외를 던지고 저장하지 않는다")
		void writePost_memberNotFound() {
			CommunityWriteRequestDto dto = new CommunityWriteRequestDto();
			dto.setTitle("제목");
			dto.setContent("내용");

			given(memberRepository.findById("ghost")).willReturn(Optional.empty());

			assertThatThrownBy(() -> communityService.writePost("ghost", dto))
					.isInstanceOf(NotFoundException.class);

			verify(communityRepository, never()).save(any());
		}

		@Test
		@DisplayName("첨부파일이 null이면 insertFiles를 호출하지 않는다")
		void writePost_nullFileAssets_doesNotInsertFiles() {
			CommunityWriteRequestDto dto = new CommunityWriteRequestDto();
			dto.setTitle("제목");
			dto.setContent("내용");

			given(memberRepository.findById("user1")).willReturn(Optional.of(member));
			Community saved = new Community();
			saved.setPostId(1L);
			given(communityRepository.save(any(Community.class))).willReturn(saved);

			communityService.writePost("user1", dto);

			verify(fileRefService, never()).insertFiles(any(), any());
		}

		@Test
		@DisplayName("첨부파일 리스트가 비어있으면 insertFiles를 호출하지 않는다")
		void writePost_emptyFileAssets_doesNotInsertFiles() {
			CommunityWriteRequestDto dto = new CommunityWriteRequestDto();
			dto.setTitle("제목");
			dto.setContent("내용");
			dto.setFileAssets(Collections.emptyList());

			given(memberRepository.findById("user1")).willReturn(Optional.of(member));
			Community saved = new Community();
			saved.setPostId(1L);
			given(communityRepository.save(any(Community.class))).willReturn(saved);

			communityService.writePost("user1", dto);

			verify(fileRefService, never()).insertFiles(any(), any());
		}

		@Test
		@DisplayName("첨부파일이 있으면 postId와 함께 insertFiles를 호출한다")
		void writePost_withFileAssets_callsInsertFiles() {
			CommunityWriteRequestDto dto = new CommunityWriteRequestDto();
			dto.setTitle("제목");
			dto.setContent("내용");
			List<FileAssetDto> files = List.of(new FileAssetDto());
			dto.setFileAssets(files);

			given(memberRepository.findById("user1")).willReturn(Optional.of(member));
			Community saved = new Community();
			saved.setPostId(7L);
			given(communityRepository.save(any(Community.class))).willReturn(saved);

			Long postId = communityService.writePost("user1", dto);

			verify(fileRefService).insertFiles(files, postId);
		}
	}

	@Nested
	@DisplayName("editPost")
	class EditPost {

		private Community existing;

		@BeforeEach
		void setUpExisting() {
			existing = new Community();
			existing.setPostId(1L);
			existing.setTitle("기존 제목");
			existing.setContent("기존 내용");
			existing.setCreatedAt(LocalDateTime.of(2000, 1, 1, 0, 0));
			existing.setMember(member); // member.userId = "user1"
		}

		@Test
		@DisplayName("작성자 본인이면 title/content를 수정한다")
		void editPost_success_updatesFields() {
			CommunityEditRequestDto dto = CommunityEditRequestDto.builder()
					.title("새 제목")
					.content("새 내용")
					.build();

			given(communityRepository.findById(1L)).willReturn(Optional.of(existing));

			communityService.editPost(dto, 1L, "user1");

			assertThat(existing.getTitle()).isEqualTo("새 제목");
			assertThat(existing.getContent()).isEqualTo("새 내용");
			assertThat(existing.getCreatedAt()).isAfter(LocalDateTime.of(2000, 1, 1, 0, 0));
		}

		@Test
		@DisplayName("존재하지 않는 글이면 예외를 던진다")
		void editPost_postNotFound() {
			CommunityEditRequestDto dto = CommunityEditRequestDto.builder()
					.title("새 제목").content("새 내용").build();

			given(communityRepository.findById(999L)).willReturn(Optional.empty());

			assertThatThrownBy(() -> communityService.editPost(dto, 999L, "user1"))
					.isInstanceOf(NotFoundException.class);
		}

		@Test
		@DisplayName("작성자 본인이 아니면 ForbiddenException을 던지고 내용은 변경되지 않는다")
		void editPost_notOwner_throwsForbidden() {
			CommunityEditRequestDto dto = CommunityEditRequestDto.builder()
					.title("해킹 제목").content("해킹 내용").build();

			given(communityRepository.findById(1L)).willReturn(Optional.of(existing));

			assertThatThrownBy(() -> communityService.editPost(dto, 1L, "intruder"))
					.isInstanceOf(ForbiddenException.class);

			assertThat(existing.getTitle()).isEqualTo("기존 제목");
			assertThat(existing.getContent()).isEqualTo("기존 내용");
		}

		@Test
		@DisplayName("deleted_keys만 있으면 deleteFilesByKeys만 호출한다")
		void editPost_deletedKeysOnly() {
			List<String> deletedKeys = List.of("key1");
			CommunityEditRequestDto dto = CommunityEditRequestDto.builder()
					.title("새 제목").content("새 내용")
					.deleted_keys(deletedKeys)
					.build();

			given(communityRepository.findById(1L)).willReturn(Optional.of(existing));

			communityService.editPost(dto, 1L, "user1");

			verify(fileRefService).deleteFilesByKeys(deletedKeys);
			verify(fileRefService, never()).insertFiles(any(), any());
		}

		@Test
		@DisplayName("file_assets만 있으면 insertFiles만 호출한다")
		void editPost_fileAssetsOnly() {
			List<FileAssetDto> files = List.of(new FileAssetDto());
			CommunityEditRequestDto dto = CommunityEditRequestDto.builder()
					.title("새 제목").content("새 내용")
					.file_assets(files)
					.build();

			given(communityRepository.findById(1L)).willReturn(Optional.of(existing));

			communityService.editPost(dto, 1L, "user1");

			verify(fileRefService).insertFiles(files, 1L);
			verify(fileRefService, never()).deleteFilesByKeys(any());
		}

		@Test
		@DisplayName("deleted_keys와 file_assets가 모두 있으면 둘 다 호출한다")
		void editPost_bothDeletedAndNewFiles() {
			List<String> deletedKeys = List.of("key1");
			List<FileAssetDto> files = List.of(new FileAssetDto());
			CommunityEditRequestDto dto = CommunityEditRequestDto.builder()
					.title("새 제목").content("새 내용")
					.deleted_keys(deletedKeys)
					.file_assets(files)
					.build();

			given(communityRepository.findById(1L)).willReturn(Optional.of(existing));

			communityService.editPost(dto, 1L, "user1");

			verify(fileRefService).deleteFilesByKeys(deletedKeys);
			verify(fileRefService).insertFiles(files, 1L);
		}

		@Test
		@DisplayName("deleted_keys와 file_assets가 모두 없으면 둘 다 호출하지 않는다")
		void editPost_noFileChanges() {
			CommunityEditRequestDto dto = CommunityEditRequestDto.builder()
					.title("새 제목").content("새 내용")
					.build();

			given(communityRepository.findById(1L)).willReturn(Optional.of(existing));

			communityService.editPost(dto, 1L, "user1");

			verify(fileRefService, never()).deleteFilesByKeys(any());
			verify(fileRefService, never()).insertFiles(any(), any());
		}
	}

	@Nested
	@DisplayName("deletePost")
	class DeletePost {

		private Community existing;

		@BeforeEach
		void setUpExisting() {
			existing = new Community();
			existing.setPostId(1L);
			existing.setMember(member); // member.userId = "user1"
		}

		@Test
		@DisplayName("작성자 본인이면 글과 첨부파일을 삭제한다")
		void deletePost_success() {
			given(communityRepository.findById(1L)).willReturn(Optional.of(existing));

			communityService.deletePost(1L, "user1");

			verify(communityRepository).delete(existing);

			ArgumentCaptor<FileAssetDto> captor = ArgumentCaptor.forClass(FileAssetDto.class);
			verify(fileRefService).deleteFiles(captor.capture());
			assertThat(captor.getValue().getPost_id()).isEqualTo(1L);
			assertThat(captor.getValue().getPost_type()).isEqualTo(FileCategory.POST_IMAGE.name());
		}

		@Test
		@DisplayName("존재하지 않는 글이면 예외를 던지고 아무 것도 삭제하지 않는다")
		void deletePost_postNotFound() {
			given(communityRepository.findById(999L)).willReturn(Optional.empty());

			assertThatThrownBy(() -> communityService.deletePost(999L, "user1"))
					.isInstanceOf(NotFoundException.class);

			verify(communityRepository, never()).delete(any());
			verify(fileRefService, never()).deleteFiles(any());
		}

		@Test
		@DisplayName("작성자 본인이 아니면 ForbiddenException을 던지고 아무 것도 삭제하지 않는다")
		void deletePost_notOwner_throwsForbidden() {
			given(communityRepository.findById(1L)).willReturn(Optional.of(existing));

			assertThatThrownBy(() -> communityService.deletePost(1L, "intruder"))
					.isInstanceOf(ForbiddenException.class);

			verify(communityRepository, never()).delete(any());
			verify(fileRefService, never()).deleteFiles(any());
		}
	}

	@Nested
	@DisplayName("incrementReadCnt")
	class IncrementReadCnt {

		@Test
		@DisplayName("postId로 조회수 증가 쿼리를 호출한다 (존재 여부를 사전 확인하지 않는 구조라 없는 postId여도 예외 없이 호출됨)")
		void incrementReadCnt_callsRepository() {
			communityService.incrementReadCnt(1L);

			verify(communityRepository).incrementViewCount(1L);
		}
	}

	@Nested
	@DisplayName("getPost")
	class GetPost {

		@Test
		@DisplayName("존재하는 글이면 Community/Member 값이 응답 DTO에 매핑된다")
		void getPost_success_mapsFields() {
			Community community = new Community();
			community.setPostId(1L);
			community.setTitle("제목");
			community.setContent("원본 내용");
			community.setViewCount(5);
			community.setCreatedAt(LocalDateTime.of(2024, 1, 1, 0, 0));
			community.setMember(member); // userId=user1, userName=tester

			given(communityRepository.findById(1L)).willReturn(Optional.of(community));
			given(imageViewService.injectViewUrls("원본 내용")).willReturn("치환된 내용");

			CommunityDetailResponseDto result = communityService.getPost(1L);

			assertThat(result.getPostId()).isEqualTo(1L);
			assertThat(result.getTitle()).isEqualTo("제목");
			assertThat(result.getContent()).isEqualTo("치환된 내용");
			assertThat(result.getViewCount()).isEqualTo(5);
			assertThat(result.getCreatedAt()).isEqualTo(LocalDateTime.of(2024, 1, 1, 0, 0));
			assertThat(result.getUserId()).isEqualTo("user1");
			assertThat(result.getUserName()).isEqualTo("tester");
		}

		@Test
		@DisplayName("존재하지 않는 글이면 예외를 던진다")
		void getPost_notFound() {
			given(communityRepository.findById(999L)).willReturn(Optional.empty());

			assertThatThrownBy(() -> communityService.getPost(999L))
					.isInstanceOf(NotFoundException.class);
		}
	}

	@Nested
	@DisplayName("getPostForEdit")
	class GetPostForEdit {

		private Community existing;

		@BeforeEach
		void setUpExisting() {
			existing = new Community();
			existing.setPostId(1L);
			existing.setTitle("제목");
			existing.setContent("원본 내용");
			existing.setMember(member);
		}

		@Test
		@DisplayName("존재하는 글이면 post(title/content)와 files를 함께 반환한다")
		void getPostForEdit_success() {
			given(communityRepository.findById(1L)).willReturn(Optional.of(existing));
			given(imageViewService.injectViewUrls("원본 내용")).willReturn("치환된 내용");

			List<FileAssetDto> files = List.of(new FileAssetDto());
			given(fileRefService.getFilesByPost(any(FileAssetDto.class))).willReturn(files);

			Map<String, Object> result = communityService.getPostForEdit(1L);

			@SuppressWarnings("unchecked")
			Map<String, Object> post = (Map<String, Object>) result.get("post");
			assertThat(post.get("title")).isEqualTo("제목");
			assertThat(post.get("content")).isEqualTo("치환된 내용");
			assertThat(result.get("files")).isEqualTo(files);
		}

		@Test
		@DisplayName("fileRefService.getFilesByPost가 post_id/post_type 조건으로 정확히 호출된다")
		void getPostForEdit_callsGetFilesByPostWithCorrectParam() {
			given(communityRepository.findById(1L)).willReturn(Optional.of(existing));
			given(imageViewService.injectViewUrls(any())).willReturn("치환된 내용");
			given(fileRefService.getFilesByPost(any())).willReturn(List.of());

			communityService.getPostForEdit(1L);

			ArgumentCaptor<FileAssetDto> captor = ArgumentCaptor.forClass(FileAssetDto.class);
			verify(fileRefService).getFilesByPost(captor.capture());
			assertThat(captor.getValue().getPost_id()).isEqualTo(1L);
			assertThat(captor.getValue().getPost_type()).isEqualTo(FileCategory.POST_IMAGE.name());
		}

		@Test
		@DisplayName("존재하지 않는 글이면 예외를 던지고 getFilesByPost는 호출되지 않는다")
		void getPostForEdit_notFound() {
			given(communityRepository.findById(999L)).willReturn(Optional.empty());

			assertThatThrownBy(() -> communityService.getPostForEdit(999L))
					.isInstanceOf(NotFoundException.class);

			verify(fileRefService, never()).getFilesByPost(any());
		}
	}

	@Nested
	@DisplayName("getMyPosts")
	class GetMyPosts {

		@Test
		@DisplayName("pageNum은 1-based로 받아 0-based Pageable로 변환해서 repository에 넘긴다")
		void getMyPosts_convertsPageNumToZeroBased() {
			Page<CommunityListResponseDto> page = new PageImpl<>(List.of());
			given(communityRepository.getMyPosts(eq("user1"), eq("검색어"), any(Pageable.class)))
					.willReturn(page);

			communityService.getMyPosts("user1", "검색어", 1, 10);

			ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
			verify(communityRepository).getMyPosts(eq("user1"), eq("검색어"), captor.capture());
			assertThat(captor.getValue().getPageNumber()).isEqualTo(0);
			assertThat(captor.getValue().getPageSize()).isEqualTo(10);
		}

		@Test
		@DisplayName("repository가 반환한 Page의 content가 결과의 posts로 그대로 담긴다")
		void getMyPosts_returnsPageContentAsPosts() {
			CommunityListResponseDto post = new CommunityListResponseDto(
					1L, "제목", LocalDateTime.now(), 0, 0, "user1", "tester");
			Page<CommunityListResponseDto> page = new PageImpl<>(List.of(post));
			given(communityRepository.getMyPosts(any(), any(), any())).willReturn(page);

			Map<String, Object> result = communityService.getMyPosts("user1", null, 1, 10);

			assertThat(result.get("posts")).isEqualTo(page.getContent());
		}

		@Test
		@DisplayName("전체 개수(getTotalElements)를 기준으로 pageMaker의 total이 채워진다")
		void getMyPosts_setsTotalOnPageMaker() {
			CommunityListResponseDto post = new CommunityListResponseDto(
					1L, "제목", LocalDateTime.now(), 0, 0, "user1", "tester");
			Page<CommunityListResponseDto> page = new PageImpl<>(List.of(post), PageRequest.of(0, 10), 25);
			given(communityRepository.getMyPosts(any(), any(), any())).willReturn(page);

			Map<String, Object> result = communityService.getMyPosts("user1", null, 1, 10);

			PageDto pageMaker = (PageDto) result.get("pageMaker");
			assertThat(pageMaker.getTotal()).isEqualTo(25);
		}

		@Test
		@DisplayName("결과가 0건이면 posts는 빈 리스트, total은 0이다")
		void getMyPosts_emptyResult() {
			Page<CommunityListResponseDto> page = new PageImpl<>(List.of());
			given(communityRepository.getMyPosts(any(), any(), any())).willReturn(page);

			Map<String, Object> result = communityService.getMyPosts("user1", null, 1, 10);

			assertThat((List<?>) result.get("posts")).isEmpty();
			PageDto pageMaker = (PageDto) result.get("pageMaker");
			assertThat(pageMaker.getTotal()).isEqualTo(0);
		}

		@Test
		@DisplayName("keyword가 null이어도 별도 가공 없이 그대로 repository에 전달된다")
		void getMyPosts_passesNullKeywordThrough() {
			Page<CommunityListResponseDto> page = new PageImpl<>(List.of());
			given(communityRepository.getMyPosts(eq("user1"), isNull(), any(Pageable.class)))
					.willReturn(page);

			communityService.getMyPosts("user1", null, 1, 10);

			verify(communityRepository).getMyPosts(eq("user1"), isNull(), any(Pageable.class));
		}
	}

	@Nested
	@DisplayName("deleteMyPosts")
	class DeleteMyPosts {

		@Test
		@DisplayName("postIds가 null이면 아무 것도 하지 않는다")
		void deleteMyPosts_nullPostIds_doesNothing() {
			communityService.deleteMyPosts("user1", null);

			verify(communityRepository, never()).countByPostIdInAndMember_UserId(any(), any());
			verify(communityRepository, never()).deleteMyPosts(any(), any());
			verify(fileRefService, never()).deleteFiles(any());
		}

		@Test
		@DisplayName("postIds가 빈 리스트면 아무 것도 하지 않는다")
		void deleteMyPosts_emptyPostIds_doesNothing() {
			communityService.deleteMyPosts("user1", List.of());

			verify(communityRepository, never()).countByPostIdInAndMember_UserId(any(), any());
			verify(communityRepository, never()).deleteMyPosts(any(), any());
			verify(fileRefService, never()).deleteFiles(any());
		}

		@Test
		@DisplayName("모두 본인 소유 글이면 삭제하고, postId마다 첨부파일도 삭제한다")
		void deleteMyPosts_allOwned_deletesEachPostFiles() {
			List<Long> postIds = List.of(1L, 2L, 3L);
			given(communityRepository.countByPostIdInAndMember_UserId(postIds, "user1")).willReturn(3L);

			communityService.deleteMyPosts("user1", postIds);

			verify(communityRepository).deleteMyPosts("user1", postIds);

			ArgumentCaptor<FileAssetDto> captor = ArgumentCaptor.forClass(FileAssetDto.class);
			verify(fileRefService, times(3)).deleteFiles(captor.capture());
			assertThat(captor.getAllValues())
					.extracting(FileAssetDto::getPost_id)
					.containsExactlyInAnyOrder(1L, 2L, 3L);
			assertThat(captor.getAllValues())
					.allMatch(f -> f.getPost_type().equals(FileCategory.POST_IMAGE.name()));
		}

		@Test
		@DisplayName("본인 소유가 아닌 글이 섞여 있으면 ForbiddenException을 던지고 아무 것도 삭제하지 않는다")
		void deleteMyPosts_notAllOwned_throwsForbidden() {
			List<Long> postIds = List.of(1L, 2L, 3L);
			given(communityRepository.countByPostIdInAndMember_UserId(postIds, "user1")).willReturn(2L);

			assertThatThrownBy(() -> communityService.deleteMyPosts("user1", postIds))
					.isInstanceOf(ForbiddenException.class);

			verify(communityRepository, never()).deleteMyPosts(any(), any());
			verify(fileRefService, never()).deleteFiles(any());
		}

		@Test
		@DisplayName("본인 소유 글이 0개면 ForbiddenException을 던진다")
		void deleteMyPosts_noneOwned_throwsForbidden() {
			List<Long> postIds = List.of(1L, 2L);
			given(communityRepository.countByPostIdInAndMember_UserId(postIds, "user1")).willReturn(0L);

			assertThatThrownBy(() -> communityService.deleteMyPosts("user1", postIds))
					.isInstanceOf(ForbiddenException.class);

			verify(communityRepository, never()).deleteMyPosts(any(), any());
			verify(fileRefService, never()).deleteFiles(any());
		}
	}

	// mapper 위임 확인용. Community.xml에 있는 실제 SQL(검색/정렬 등)은 여기서 검증 되지 않음.
	@Nested
	@DisplayName("listCnt / getPopularBoard / getFavorites / favoritesListCnt (mapper 위임 확인, SQL 검증 X)")
	class MapperDelegation {

		@Test
		@DisplayName("listCnt - N건일 때 mapper 결과 그대로 반환")
		void listCnt_returnsMapperResult() {
			CommunityDto dto = new CommunityDto();
			dto.setKeyword("기타");
			given(comMapper.listCnt(dto)).willReturn(7);

			int result = communityService.listCnt(dto);

			assertThat(result).isEqualTo(7);
			verify(comMapper).listCnt(dto); // 파라미터 그대로 전달되는지
		}

		@Test
		@DisplayName("listCnt - 0건이면 0 반환")
		void listCnt_zeroResult() {
			CommunityDto dto = new CommunityDto();
			given(comMapper.listCnt(dto)).willReturn(0);

			int result = communityService.listCnt(dto);

			assertThat(result).isEqualTo(0);
		}

		@Test
		@DisplayName("getPopularBoard - mapper 결과 그대로 반환")
		void getPopularBoard_returnsMapperResult() {
			List<CommunityDto> mockResult = List.of(new CommunityDto());
			given(comMapper.getPopularBoard()).willReturn(mockResult);

			List<CommunityDto> result = communityService.getPopularBoard();

			assertThat(result).isEqualTo(mockResult);
		}

		@Test
		@DisplayName("getPopularBoard - 결과 없으면 빈 리스트")
		void getPopularBoard_empty() {
			given(comMapper.getPopularBoard()).willReturn(Collections.emptyList());

			List<CommunityDto> result = communityService.getPopularBoard();

			assertThat(result).isEmpty();
		}

		@Test
		@DisplayName("getFavorites - mapper 위임 확인")
		void getFavorites_delegatesToMapper() {
			CommunityDto param = new CommunityDto();
			param.setUser_id("user1");
			List<CommunityDto> mockResult = List.of(new CommunityDto());
			given(comMapper.getFavorites(param)).willReturn(mockResult);

			List<CommunityDto> result = communityService.getFavorites(param);

			assertThat(result).isEqualTo(mockResult);
			verify(comMapper).getFavorites(param);
		}

		@Test
		@DisplayName("favoritesListCnt - mapper 위임 확인")
		void favoritesListCnt_delegatesToMapper() {
			CommunityDto param = new CommunityDto();
			param.setUser_id("user1");
			given(comMapper.favoritesListCnt(param)).willReturn(3);

			int result = communityService.favoritesListCnt(param);

			assertThat(result).isEqualTo(3);
			verify(comMapper).favoritesListCnt(param);
		}
	}
}
