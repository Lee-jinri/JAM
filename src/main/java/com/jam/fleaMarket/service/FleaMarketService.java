package com.jam.fleaMarket.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.jam.file.dto.ImageFileDto;
import com.jam.file.mapper.ImageFileMapper;
import com.jam.fleaMarket.dto.FleaMarketDto;
import com.jam.fleaMarket.mapper.FleaMarketMapper;
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
            String savedFileName = fileUtils.saveToLocal(image, "flea"); // 파일 저장
            
            if (savedFileName == null) {
                throw new RuntimeException("이미지 저장 실패");
            }
            
            if (i == 0) thumbnail = savedFileName;
            
            ImageFileDto imageDto = new ImageFileDto();
            
            imageDto.setPost_id((long) post_id);
            imageDto.setImage_name(savedFileName);
            imageDto.setPost_type("FLEA");
            
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
	    
		if (images != null && !images.isEmpty()) {
			for (int i = 0; i < images.size(); i++) {
				MultipartFile image = images.get(i);
	            String savedFileName = fileUtils.saveToLocal(image, "flea"); // 파일 저장
	            
	            if (savedFileName == null) {
	                throw new RuntimeException("이미지 저장 실패");
	            }
	            
	            ImageFileDto imageVO = new ImageFileDto();
	            
	            imageVO.setPost_id(flea.getPost_id());
	            imageVO.setImage_name(savedFileName);
	            imageVO.setPost_type("FLEA");
	            
	            imageFileMapper.insertImage(imageVO); // 이미지 메타정보 DB에 저장
	            
	            if(image.getOriginalFilename().equals(thumbnailName)) thumbnailName = savedFileName;
	        }
		}
		
		if(deletedImages != null && !deletedImages.isEmpty()) {
			for (Long id : deletedImages) {

	            // DB에서 파일명 먼저 조회
	            String deleteFileName = imageFileMapper.findNameById(Long.valueOf(id));

	            fileUtils.deleteToLocal(deleteFileName, "FLEA");
				imageFileMapper.deleteImage(id);
	        }
		}
		
		if (thumbnailId != null) {
			flea.setThumbnail(imageFileMapper.findNameById(thumbnailId));
		} else if (thumbnailName != null && !thumbnailName.isBlank()) {
			flea.setThumbnail(thumbnailName);
		}else {
		    throw new RuntimeException("썸네일이 설정되지 않았습니다.");
		}
		
		fleaMapper.editPost(flea);
	}
	
	@Transactional
	public void deletePost(Long post_id, String userId) {
		List<ImageFileDto> deleteFileNames = imageFileMapper.getImages(post_id, "FLEA");
		
		int count = fleaMapper.deletePost(post_id, userId);
		//if(count < 1) throw new ForbiddenException("게시물을 삭제할 권한이 없습니다.");
		
		for(ImageFileDto file: deleteFileNames) {
			fileUtils.deleteToLocal(file.getImage_name(), "FLEA");
			imageFileMapper.deleteImage(file.getImage_id());
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
}
