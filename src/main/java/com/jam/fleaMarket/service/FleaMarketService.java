package com.jam.fleaMarket.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.jam.file.dto.FileCategory;
import com.jam.file.dto.ImageFileDto;
import com.jam.file.mapper.ImageFileMapper;
import com.jam.fleaMarket.dto.FleaMarketDto;
import com.jam.fleaMarket.mapper.FleaMarketMapper;
import com.jam.global.exception.BadRequestException;
import com.jam.global.exception.ForbiddenException;
import com.jam.global.exception.NotFoundException;
import com.jam.global.util.FileUtils;
import com.jam.member.service.MemberService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FleaMarketService {

	private final FleaMarketMapper fleaMapper;
	private final MemberService memberService;
	private final FileUtils fileUtils;
	private final ImageFileMapper imageFileMapper;
	
	public List<FleaMarketDto> getBoard(FleaMarketDto flea_vo) {
		
		List<FleaMarketDto> list = new ArrayList<>(); 
		
		if(flea_vo.getUser_id() == null) list = fleaMapper.getBoard(flea_vo);
		else list = fleaMapper.getBoardWithFavorite(flea_vo);
		
		return list;
	}
	
	public int listCnt(FleaMarketDto flea_vo) {
		return fleaMapper.listCnt(flea_vo);
	}

	public void incrementReadCnt(Long post_id) {
		fleaMapper.incrementReadCnt(post_id);
	}

	public FleaMarketDto getPostDetail(FleaMarketDto flea) {
		FleaMarketDto detail = fleaMapper.getPostDetail(flea);
		return detail;
	}

	@Transactional
	public long writePost(FleaMarketDto flea, List<MultipartFile> images) {
		
		long post_id = fleaMapper.getNextPostId();
		
		flea.setPost_id(post_id);
		String thumbnail = null;
		
		for (int i = 0; i < images.size(); i++) {
			MultipartFile image = images.get(i);
			
	    	fileUtils.validateFileType(image.getOriginalFilename(), image.getContentType(), FileCategory.POST_IMAGE);
	        fileUtils.validateFileSize(image.getSize(), FileCategory.POST_IMAGE);
	        
            String savedFileName = fileUtils.saveToLocal(image, "flea"); // 파일 저장
            
            if (savedFileName == null) {
                throw new RuntimeException("이미지 저장 실패");
            }
            
            // 썸네일 저장
            if (i == 0) {
            	thumbnail = savedFileName;
            	fileUtils.saveThumbnail("flea", savedFileName);
            }
            
            ImageFileDto imageDto = new ImageFileDto();
            
            imageDto.setPost_id((long) post_id);
            imageDto.setImage_name(savedFileName);
            imageDto.setPost_type("FLEA");
            imageDto.setIs_thumbnail(i == 0 ? "Y" : "N");
            
            imageFileMapper.insertImage(imageDto); // 이미지 메타정보 DB에 저장
        }
		
		flea.setThumbnail(thumbnail);
		fleaMapper.writePost(flea);
		
		return post_id;
	}

	public FleaMarketDto getPostForEdit(Long post_id) {
		FleaMarketDto updateData =  fleaMapper.getPostForEdit(post_id);
		return updateData;
	}

	@Transactional
	public void editPost(FleaMarketDto flea, List<MultipartFile> images, List<Long> deletedImages, Long thumbnailId, String thumbnailName) {
		
		FleaMarketDto existing = fleaMapper.findOwnerAndThumbnailByPostId(flea.getPost_id());
	    if (existing == null) {
	        throw new NotFoundException("게시글을 찾을 수 없습니다.");
	    }
	    if (!existing.getUser_id().equals(flea.getUser_id())) {
	        throw new ForbiddenException("수정 권한이 없습니다.");
	    }
	    boolean hasNewThumbCandidate = false;

	    if (thumbnailId == null) { // 기존 이미지가 썸네일이 아닌 경우에만 체크
	    	if (images != null && !images.isEmpty()) {
	    		for (MultipartFile image : images) {
	    			if (image.getOriginalFilename().equals(thumbnailName)) {
	    				hasNewThumbCandidate = true;
	    				break;
	    			}
	    		}
	    	}
	    	
	    	// 새로운 썸네일 후보가 전혀 없으면 에러
	    	if (!hasNewThumbCandidate) {
	    		throw new RuntimeException("썸네일이 설정되지 않았습니다.");
	    	}
	    }

	    Long newThumbnailImageId = null;
	    
		if (images != null && !images.isEmpty()) {
			for (int i = 0; i < images.size(); i++) {
				MultipartFile image = images.get(i);

		    	fileUtils.validateFileType(image.getOriginalFilename(), image.getContentType(), FileCategory.POST_IMAGE);
		        fileUtils.validateFileSize(image.getSize(), FileCategory.POST_IMAGE);
		        
	            String savedFileName = fileUtils.saveToLocal(image, "flea"); // 파일 저장
	            
	            if (savedFileName == null) {
	                throw new RuntimeException("이미지 저장 실패");
	            }
	            
	            ImageFileDto imageVO = new ImageFileDto();
	            
	            imageVO.setPost_id(flea.getPost_id());
	            imageVO.setImage_name(savedFileName);
	            imageVO.setPost_type("FLEA");
	            imageVO.setIs_thumbnail("N");
	            
	            imageFileMapper.insertImage(imageVO); // 이미지 메타정보 DB에 저장
	            
	            if(image.getOriginalFilename().equals(thumbnailName)) {
	            	thumbnailName = savedFileName;
	            	newThumbnailImageId = imageVO.getImage_id();
	            }
	        }
		}
		
		if(deletedImages != null && !deletedImages.isEmpty()) {
			for (Long id : deletedImages) {
	            // image_name, is_thumbnail 조회
				ImageFileDto img = imageFileMapper.findById(id);
				
				fileUtils.deleteToLocal(img.getImage_name(), "flea");
				
				if ("Y".equals(img.getIs_thumbnail())) {
			        fileUtils.deleteThumbnail(img.getImage_name(), "flea");
			    }
				
				imageFileMapper.deleteImage(id);
	        }
		}
		
		String resolvedThumbName;
	    Long resolvedThumbId;
	    
		if (thumbnailId != null) {
			ImageFileDto thumbImg = imageFileMapper.findById(thumbnailId);
			resolvedThumbName = thumbImg.getImage_name();
			resolvedThumbId = thumbnailId;
		} else if (thumbnailName != null && !thumbnailName.isBlank()) {
			resolvedThumbName = thumbnailName;
			resolvedThumbId = newThumbnailImageId;
		}else {
		    throw new BadRequestException("썸네일이 설정되지 않았습니다.");
		}
		
		boolean thumbnailChanged = !resolvedThumbName.equals(existing.getThumbnail());
		
		if(thumbnailChanged) {
			imageFileMapper.clearThumbnailFlag(flea.getPost_id());
		    imageFileMapper.setThumbnailFlag(resolvedThumbId);
		    
		    fileUtils.saveThumbnail("flea", resolvedThumbName);
		}
		
	    flea.setThumbnail(resolvedThumbName);
		fleaMapper.editPost(flea);
	}
	
	@Transactional
	public void deletePost(Long post_id, String userId) {
		List<ImageFileDto> deleteFileNames = imageFileMapper.getImages(post_id, "flea");
		
		int count = fleaMapper.deletePost(post_id, userId);
		if(count < 1) throw new ForbiddenException("게시물을 삭제할 권한이 없습니다.");
		
		for(ImageFileDto file: deleteFileNames) {
			imageFileMapper.deleteImage(file.getImage_id());
			fileUtils.deleteToLocal(file.getImage_name(), "flea");
			
			if ("Y".equals(file.getIs_thumbnail())) {
	            fileUtils.deleteThumbnail(file.getImage_name(), "flea");
	        }
		}
	}

	public List<FleaMarketDto> getMyStore(FleaMarketDto flea) {
		return fleaMapper.getMyStore(flea);
	}

	public int getMyStoreCnt(FleaMarketDto flea) {
		return fleaMapper.getMyStoreCnt(flea);
	}

	public String getUserId(String user_name) {
		return memberService.getUserId(user_name);
	}
	
	public boolean isValidUserName(String user_name) throws Exception {
		int count = memberService.nameCheck(user_name);
		
		return count != 0 ? true : false;
	}

	public List<FleaMarketDto> getFavorites(FleaMarketDto flea) {
		return fleaMapper.getFavorites(flea);
	}

	public List<ImageFileDto> getImages(Long post_id) {
		return imageFileMapper.getImages(post_id, "FLEA");
	}

	public String getWriterIdByPostId(Long postId) {
		return fleaMapper.getWriterIdByPostId(postId);
	}

	public List<ImageFileDto> findFleaImagesByPostId(Long post_id) {
		return imageFileMapper.findFleaImagesByPostId(post_id, "FLEA");
	}
}
