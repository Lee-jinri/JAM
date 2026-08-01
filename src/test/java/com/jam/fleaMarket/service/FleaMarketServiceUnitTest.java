package com.jam.fleaMarket.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.jam.file.dto.ImageFileDto;
import com.jam.file.mapper.ImageFileMapper;
import com.jam.fleaMarket.dto.FleaMarketDto;
import com.jam.fleaMarket.mapper.FleaMarketMapper;
import com.jam.global.exception.BadRequestException;
import com.jam.global.exception.ForbiddenException;
import com.jam.global.exception.NotFoundException;
import com.jam.global.util.FileUtils;

/**
 * FleaMarketService에 대한 Mockito 단위 테스트.
 * 단순 mapper 위임 메서드는 위임 확인용으로만 작성했고, XML에 있는 실제 SQL(검색/즐겨찾기 join 등)은
 * 이 유닛테스트로 검증되지 않는다.
 */
@ExtendWith(MockitoExtension.class)
class FleaMarketServiceUnitTest {

	@Mock
	private FleaMarketMapper fleaMapper;
	@Mock
	private FileUtils fileUtils;
	@Mock
	private ImageFileMapper imageFileMapper;

	@InjectMocks
	private FleaMarketService fleaMarketService;

	@Nested
	@DisplayName("getBoard")
	class GetBoard {

		@Test
		@DisplayName("user_id가 없으면 getBoard로 조회한다")
		void getBoard_noUserId_callsGetBoard() {
			FleaMarketDto flea = new FleaMarketDto();

			List<FleaMarketDto> mockResult = List.of(new FleaMarketDto());
			given(fleaMapper.getBoard(flea)).willReturn(mockResult);

			List<FleaMarketDto> result = fleaMarketService.getBoard(flea);

			assertThat(result).isEqualTo(mockResult);
			verify(fleaMapper, never()).getBoardWithFavorite(any());
		}

		@Test
		@DisplayName("user_id가 있으면 getBoardWithFavorite로 조회한다")
		void getBoard_withUserId_callsGetBoardWithFavorite() {
			FleaMarketDto flea = new FleaMarketDto();
			flea.setUser_id("user1");

			List<FleaMarketDto> mockResult = List.of(new FleaMarketDto());
			given(fleaMapper.getBoardWithFavorite(flea)).willReturn(mockResult);

			List<FleaMarketDto> result = fleaMarketService.getBoard(flea);

			assertThat(result).isEqualTo(mockResult);
			verify(fleaMapper, never()).getBoard(any());
		}
	}

	@Nested
	@DisplayName("writePost")
	class WritePost {

		@Test
		@DisplayName("이미지가 여러 장이면 첫 번째만 썸네일로 저장하고 나머지는 is_thumbnail=N으로 저장한다")
		void writePost_multipleImages_onlyFirstIsThumbnail() {
			FleaMarketDto flea = new FleaMarketDto();
			flea.setTitle("제목");

			MultipartFile img1 = new MockMultipartFile("images", "a.jpg", "image/jpeg", "a".getBytes());
			MultipartFile img2 = new MockMultipartFile("images", "b.jpg", "image/jpeg", "b".getBytes());

			given(fleaMapper.getNextPostId()).willReturn(10L);
			given(fileUtils.saveToLocal(any(MultipartFile.class), eq("flea")))
					.willReturn("uuid-a.jpg", "uuid-b.jpg");

			long postId = fleaMarketService.writePost(flea, List.of(img1, img2));

			assertThat(postId).isEqualTo(10L);
			assertThat(flea.getThumbnail()).isEqualTo("uuid-a.jpg");
			verify(fileUtils).saveThumbnail("flea", "uuid-a.jpg");

			ArgumentCaptor<ImageFileDto> captor = ArgumentCaptor.forClass(ImageFileDto.class);
			verify(imageFileMapper, org.mockito.Mockito.times(2)).insertImage(captor.capture());
			List<ImageFileDto> inserted = captor.getAllValues();
			assertThat(inserted.get(0).getIs_thumbnail()).isEqualTo("Y");
			assertThat(inserted.get(1).getIs_thumbnail()).isEqualTo("N");
			assertThat(inserted).allMatch(i -> i.getPost_type().equals("FLEA"));

			verify(fleaMapper).writePost(flea);
		}

		@Test
		@DisplayName("파일 저장에 실패하면(null 반환) 예외를 던지고 글을 저장하지 않는다")
		void writePost_saveToLocalFails_throwsAndDoesNotWrite() {
			FleaMarketDto flea = new FleaMarketDto();
			MultipartFile img = new MockMultipartFile("images", "a.jpg", "image/jpeg", "a".getBytes());

			given(fleaMapper.getNextPostId()).willReturn(1L);
			given(fileUtils.saveToLocal(any(MultipartFile.class), eq("flea"))).willReturn(null);

			assertThatThrownBy(() -> fleaMarketService.writePost(flea, List.of(img)))
					.isExactlyInstanceOf(RuntimeException.class)
					.hasMessage("이미지 저장 실패");

			verify(imageFileMapper, never()).insertImage(any());
			verify(fleaMapper, never()).writePost(any());
		}
	}

	@Nested
	@DisplayName("editPost")
	class EditPost {

		private FleaMarketDto existing;

		@BeforeEach
		void setUpExisting() {
			existing = new FleaMarketDto();
			existing.setPost_id(1L);
			existing.setUser_id("user1");
			existing.setThumbnail("old.jpg");
		}

		@Test
		@DisplayName("존재하지 않는 글이면 NotFoundException을 던진다")
		void editPost_postNotFound() {
			given(fleaMapper.findOwnerAndThumbnailByPostId(1L)).willReturn(null);

			FleaMarketDto flea = new FleaMarketDto();
			flea.setPost_id(1L);
			flea.setUser_id("user1");

			assertThatThrownBy(() -> fleaMarketService.editPost(flea, null, null, 100L, null))
					.isInstanceOf(NotFoundException.class);

			verify(fleaMapper, never()).editPost(any());
		}

		@Test
		@DisplayName("작성자 본인이 아니면 ForbiddenException을 던진다")
		void editPost_notOwner() {
			given(fleaMapper.findOwnerAndThumbnailByPostId(1L)).willReturn(existing);

			FleaMarketDto flea = new FleaMarketDto();
			flea.setPost_id(1L);
			flea.setUser_id("intruder");

			assertThatThrownBy(() -> fleaMarketService.editPost(flea, null, null, 100L, null))
					.isInstanceOf(ForbiddenException.class);

			verify(fleaMapper, never()).editPost(any());
		}

		@Test
		@DisplayName("thumbnailId가 없고 새 이미지도 없으면 썸네일 미지정 예외를 던진다")
		void editPost_noThumbnailId_noImages_throws() {
			given(fleaMapper.findOwnerAndThumbnailByPostId(1L)).willReturn(existing);

			FleaMarketDto flea = new FleaMarketDto();
			flea.setPost_id(1L);
			flea.setUser_id("user1");

			assertThatThrownBy(() -> fleaMarketService.editPost(flea, null, null, null, "aaa.jpg"))
					.isExactlyInstanceOf(RuntimeException.class)
					.hasMessage("썸네일이 설정되지 않았습니다.");
		}

		@Test
		@DisplayName("thumbnailId가 없고 새 이미지 중 thumbnailName과 일치하는 파일이 없으면 예외를 던진다")
		void editPost_noThumbnailId_noMatchingImage_throws() {
			given(fleaMapper.findOwnerAndThumbnailByPostId(1L)).willReturn(existing);

			FleaMarketDto flea = new FleaMarketDto();
			flea.setPost_id(1L);
			flea.setUser_id("user1");

			MultipartFile img = new MockMultipartFile("images", "a.jpg", "image/jpeg", "a".getBytes());

			assertThatThrownBy(() -> fleaMarketService.editPost(flea, List.of(img), null, null, "b.jpg"))
					.isExactlyInstanceOf(RuntimeException.class)
					.hasMessage("썸네일이 설정되지 않았습니다.");
		}

		@Test
		@DisplayName("기존 이미지를 썸네일로 재지정해도 기존과 같은 파일이면 썸네일 관련 갱신을 하지 않는다")
		void editPost_existingThumbnailId_unchanged_skipsThumbnailUpdate() {
			given(fleaMapper.findOwnerAndThumbnailByPostId(1L)).willReturn(existing);

			ImageFileDto thumbImg = new ImageFileDto();
			thumbImg.setImage_name("old.jpg");
			given(imageFileMapper.findById(100L)).willReturn(thumbImg);

			FleaMarketDto flea = new FleaMarketDto();
			flea.setPost_id(1L);
			flea.setUser_id("user1");

			fleaMarketService.editPost(flea, null, null, 100L, null);

			verify(imageFileMapper, never()).clearThumbnailFlag(any());
			verify(imageFileMapper, never()).setThumbnailFlag(any());
			verify(fileUtils, never()).saveThumbnail(any(), any());
			assertThat(flea.getThumbnail()).isEqualTo("old.jpg");
			verify(fleaMapper).editPost(flea);
		}

		@Test
		@DisplayName("기존 이미지를 다른 이미지로 썸네일 재지정하면 썸네일 플래그를 갱신하고 썸네일 파일을 다시 만든다")
		void editPost_existingThumbnailId_changed_updatesThumbnailFlags() {
			given(fleaMapper.findOwnerAndThumbnailByPostId(1L)).willReturn(existing);

			ImageFileDto thumbImg = new ImageFileDto();
			thumbImg.setImage_name("new.jpg");
			given(imageFileMapper.findById(100L)).willReturn(thumbImg);

			FleaMarketDto flea = new FleaMarketDto();
			flea.setPost_id(1L);
			flea.setUser_id("user1");

			fleaMarketService.editPost(flea, null, null, 100L, null);

			verify(imageFileMapper).clearThumbnailFlag(1L);
			verify(imageFileMapper).setThumbnailFlag(100L);
			verify(fileUtils).saveThumbnail("flea", "new.jpg");
			assertThat(flea.getThumbnail()).isEqualTo("new.jpg");
		}

		@Test
		@DisplayName("새로 올린 이미지가 thumbnailName과 일치하면 그 이미지를 새 썸네일로 지정한다")
		void editPost_newImageMatchesThumbnailName_setsAsNewThumbnail() {
			given(fleaMapper.findOwnerAndThumbnailByPostId(1L)).willReturn(existing);
			given(fileUtils.saveToLocal(any(MultipartFile.class), eq("flea"))).willReturn("uuid-match.jpg");
			doAnswer(inv -> {
				ImageFileDto dto = inv.getArgument(0);
				dto.setImage_id(55L);
				return null;
			}).when(imageFileMapper).insertImage(any(ImageFileDto.class));

			FleaMarketDto flea = new FleaMarketDto();
			flea.setPost_id(1L);
			flea.setUser_id("user1");

			MultipartFile img = new MockMultipartFile("images", "match.jpg", "image/jpeg", "x".getBytes());

			fleaMarketService.editPost(flea, List.of(img), null, null, "match.jpg");

			ArgumentCaptor<ImageFileDto> captor = ArgumentCaptor.forClass(ImageFileDto.class);
			verify(imageFileMapper).insertImage(captor.capture());
			assertThat(captor.getValue().getIs_thumbnail()).isEqualTo("N");

			verify(imageFileMapper).clearThumbnailFlag(1L);
			verify(imageFileMapper).setThumbnailFlag(55L);
			verify(fileUtils).saveThumbnail("flea", "uuid-match.jpg");
			assertThat(flea.getThumbnail()).isEqualTo("uuid-match.jpg");
		}

		@Test
		@DisplayName("새 이미지 저장에 실패하면 예외를 던지고 글을 수정하지 않는다")
		void editPost_imageSaveFails_throwsAndDoesNotEdit() {
			given(fleaMapper.findOwnerAndThumbnailByPostId(1L)).willReturn(existing);
			given(fileUtils.saveToLocal(any(MultipartFile.class), eq("flea"))).willReturn(null);

			FleaMarketDto flea = new FleaMarketDto();
			flea.setPost_id(1L);
			flea.setUser_id("user1");

			MultipartFile img = new MockMultipartFile("images", "match.jpg", "image/jpeg", "x".getBytes());

			assertThatThrownBy(() -> fleaMarketService.editPost(flea, List.of(img), null, null, "match.jpg"))
					.isExactlyInstanceOf(RuntimeException.class)
					.hasMessage("이미지 저장 실패");

			verify(imageFileMapper, never()).insertImage(any());
			verify(fleaMapper, never()).editPost(any());
		}

		@Test
		@DisplayName("삭제 대상 이미지는 DB row와 로컬 파일을 지우고, 썸네일이었던 이미지는 썸네일 파일도 함께 지운다")
		void editPost_deletedImages_removesFilesAndThumbnailIfApplicable() {
			given(fleaMapper.findOwnerAndThumbnailByPostId(1L)).willReturn(existing);

			ImageFileDto notThumb = new ImageFileDto();
			notThumb.setImage_id(10L);
			notThumb.setImage_name("img10.jpg");
			notThumb.setIs_thumbnail("N");
			given(imageFileMapper.findById(10L)).willReturn(notThumb);

			ImageFileDto wasThumb = new ImageFileDto();
			wasThumb.setImage_id(20L);
			wasThumb.setImage_name("img20.jpg");
			wasThumb.setIs_thumbnail("Y");
			given(imageFileMapper.findById(20L)).willReturn(wasThumb);

			// 남아있는 기존 이미지를 썸네일로 그대로 유지 (thumbnailChanged=false 경로)
			ImageFileDto keptThumb = new ImageFileDto();
			keptThumb.setImage_name("old.jpg");
			given(imageFileMapper.findById(100L)).willReturn(keptThumb);

			FleaMarketDto flea = new FleaMarketDto();
			flea.setPost_id(1L);
			flea.setUser_id("user1");

			fleaMarketService.editPost(flea, null, List.of(10L, 20L), 100L, null);

			verify(imageFileMapper).deleteImage(10L);
			verify(imageFileMapper).deleteImage(20L);
			verify(fileUtils).deleteToLocal("img10.jpg", "flea");
			verify(fileUtils).deleteToLocal("img20.jpg", "flea");
			verify(fileUtils, never()).deleteThumbnail("img10.jpg", "flea");
			verify(fileUtils).deleteThumbnail("img20.jpg", "flea");
		}
	}

	@Nested
	@DisplayName("deletePost")
	class DeletePost {

		@Test
		@DisplayName("작성자 본인이면 글과 이미지(DB row + 로컬 파일)를 삭제하고, 썸네일이었던 이미지는 썸네일 파일도 지운다")
		void deletePost_success_deletesPostAndImages() {
			ImageFileDto thumb = new ImageFileDto();
			thumb.setImage_id(1L);
			thumb.setImage_name("thumb.jpg");
			thumb.setIs_thumbnail("Y");

			ImageFileDto normal = new ImageFileDto();
			normal.setImage_id(2L);
			normal.setImage_name("normal.jpg");
			normal.setIs_thumbnail("N");

			given(imageFileMapper.getImages(1L, "FLEA")).willReturn(List.of(thumb, normal));
			given(fleaMapper.deletePost(1L, "user1")).willReturn(1);

			fleaMarketService.deletePost(1L, "user1");

			verify(imageFileMapper).getImages(1L, "FLEA");
			verify(imageFileMapper).deleteImage(1L);
			verify(imageFileMapper).deleteImage(2L);
			verify(fileUtils).deleteToLocal("thumb.jpg", "flea");
			verify(fileUtils).deleteToLocal("normal.jpg", "flea");
			verify(fileUtils).deleteThumbnail("thumb.jpg", "flea");
			verify(fileUtils, never()).deleteThumbnail("normal.jpg", "flea");
		}

		@Test
		@DisplayName("작성자 본인이 아니면(삭제된 row 0건) ForbiddenException을 던지고 이미지는 지우지 않는다")
		void deletePost_notOwner_throwsForbidden_doesNotDeleteImages() {
			ImageFileDto image = new ImageFileDto();
			image.setImage_id(1L);
			image.setImage_name("img.jpg");
			given(imageFileMapper.getImages(1L, "FLEA")).willReturn(List.of(image));
			given(fleaMapper.deletePost(1L, "intruder")).willReturn(0);

			assertThatThrownBy(() -> fleaMarketService.deletePost(1L, "intruder"))
					.isInstanceOf(ForbiddenException.class);

			verify(imageFileMapper, never()).deleteImage(any());
			verify(fileUtils, never()).deleteToLocal(any(), any());
		}
	}

	@Nested
	@DisplayName("단순 mapper 위임 메서드 (mapper 위임 확인용, XML SQL 검증 X)")
	class MapperDelegation {

		@Test
		@DisplayName("listCnt - mapper 결과 그대로 반환")
		void listCnt_delegatesToMapper() {
			FleaMarketDto flea = new FleaMarketDto();
			given(fleaMapper.listCnt(flea)).willReturn(5);

			assertThat(fleaMarketService.listCnt(flea)).isEqualTo(5);
		}

		@Test
		@DisplayName("incrementReadCnt - mapper를 post_id로 호출한다")
		void incrementReadCnt_callsMapper() {
			fleaMarketService.incrementReadCnt(1L);

			verify(fleaMapper).incrementReadCnt(1L);
		}

		@Test
		@DisplayName("getPostDetail - mapper 결과 그대로 반환")
		void getPostDetail_delegatesToMapper() {
			FleaMarketDto flea = new FleaMarketDto();
			FleaMarketDto mockResult = new FleaMarketDto();
			given(fleaMapper.getPostDetail(flea)).willReturn(mockResult);

			assertThat(fleaMarketService.getPostDetail(flea)).isEqualTo(mockResult);
		}

		@Test
		@DisplayName("getPostForEdit - mapper 결과 그대로 반환")
		void getPostForEdit_delegatesToMapper() {
			FleaMarketDto mockResult = new FleaMarketDto();
			given(fleaMapper.getPostForEdit(1L)).willReturn(mockResult);

			assertThat(fleaMarketService.getPostForEdit(1L)).isEqualTo(mockResult);
		}

		@Test
		@DisplayName("getMyStore - mapper 결과 그대로 반환")
		void getMyStore_delegatesToMapper() {
			FleaMarketDto flea = new FleaMarketDto();
			List<FleaMarketDto> mockResult = List.of(new FleaMarketDto());
			given(fleaMapper.getMyStore(flea)).willReturn(mockResult);

			assertThat(fleaMarketService.getMyStore(flea)).isEqualTo(mockResult);
		}

		@Test
		@DisplayName("getMyStoreCnt - mapper 결과 그대로 반환")
		void getMyStoreCnt_delegatesToMapper() {
			FleaMarketDto flea = new FleaMarketDto();
			given(fleaMapper.getMyStoreCnt(flea)).willReturn(3);

			assertThat(fleaMarketService.getMyStoreCnt(flea)).isEqualTo(3);
		}

		@Test
		@DisplayName("getFavorites - mapper 결과 그대로 반환")
		void getFavorites_delegatesToMapper() {
			FleaMarketDto flea = new FleaMarketDto();
			List<FleaMarketDto> mockResult = List.of(new FleaMarketDto());
			given(fleaMapper.getFavorites(flea)).willReturn(mockResult);

			assertThat(fleaMarketService.getFavorites(flea)).isEqualTo(mockResult);
		}

		@Test
		@DisplayName("getFavorites - 결과 없으면 빈 리스트")
		void getFavorites_empty() {
			FleaMarketDto flea = new FleaMarketDto();
			given(fleaMapper.getFavorites(flea)).willReturn(Collections.emptyList());

			assertThat(fleaMarketService.getFavorites(flea)).isEmpty();
		}

		@Test
		@DisplayName("getFavoritesCnt - mapper 결과 그대로 반환")
		void getFavoritesCnt_delegatesToMapper() {
			FleaMarketDto flea = new FleaMarketDto();
			given(fleaMapper.getFavoritesCnt(flea)).willReturn(4);

			assertThat(fleaMarketService.getFavoritesCnt(flea)).isEqualTo(4);
		}

		@Test
		@DisplayName("getWriterIdByPostId - mapper 결과 그대로 반환")
		void getWriterIdByPostId_delegatesToMapper() {
			given(fleaMapper.getWriterIdByPostId(1L)).willReturn("user1");

			assertThat(fleaMarketService.getWriterIdByPostId(1L)).isEqualTo("user1");
		}

		@Test
		@DisplayName("getImages - post_type을 FLEA(대문자)로 고정해서 조회한다")
		void getImages_callsWithUppercaseFlea() {
			List<ImageFileDto> mockResult = List.of(new ImageFileDto());
			given(imageFileMapper.getImages(1L, "FLEA")).willReturn(mockResult);

			assertThat(fleaMarketService.getImages(1L)).isEqualTo(mockResult);
		}

		@Test
		@DisplayName("findFleaImagesByPostId - post_type을 FLEA(대문자)로 고정해서 조회한다")
		void findFleaImagesByPostId_callsWithUppercaseFlea() {
			List<ImageFileDto> mockResult = List.of(new ImageFileDto());
			given(imageFileMapper.findFleaImagesByPostId(1L, "FLEA")).willReturn(mockResult);

			assertThat(fleaMarketService.findFleaImagesByPostId(1L)).isEqualTo(mockResult);
		}
	}
}
