package com.jam.community.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.amazonaws.services.kms.model.NotFoundException;
import com.jam.common.dto.CommonDto;
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
import com.jam.global.service.FileReferenceService;
import com.jam.global.service.PostImageViewService;
import com.jam.member.entity.Member;
import com.jam.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommunityService {

	private final CommunityMapper comMapper;
	private final PostImageViewService imageViewService;
	private final FileReferenceService fileRefService;	

	private final CommunityRepository communityRepository;
    private final MemberRepository memberRepository;
    
	public List<CommunityDto>getBoard(CommunityDto com){
		List<CommunityDto> list = new ArrayList<>();
		
		if(com.getUser_id() == null) list = comMapper.getBoard(com);
		else list = comMapper.getBoardWithFavorite(com);
		
		return list;
	}
	
	public int listCnt(CommunityDto com) {
		return comMapper.listCnt(com);
	}
	
	@Transactional
	public void incrementReadCnt(Long postId) {
		communityRepository.incrementViewCount(postId);
	}

	public CommunityDetailResponseDto getPost(Long postId) {
		// TODO: JPQL로 community + member 한 번에 조회
		Community community = communityRepository.findById(postId)
	            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));
		
		String processedContent = imageViewService.injectViewUrls(community.getContent());

	    return CommunityDetailResponseDto.builder()
	    			.postId(community.getPostId())
	    			.title(community.getTitle())
	    			.content(processedContent)
	    			.viewCount(community.getViewCount())
	    			.createdAt(community.getCreatedAt())
	    			.userId(community.getMember().getUserId())
	    			.userName(community.getMember().getUserName())
	    			.build();
	}

    @Transactional
    public Long writePost(String userId, CommunityWriteRequestDto requestDto) {
    	Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 회원입니다."));
    	
        Community community = new Community();
        community.setMember(member);
        community.setTitle(requestDto.getTitle());
        community.setContent(requestDto.getContent());
        community.setViewCount(0);
        community.setCommentCount(0);
        community.setCreatedAt(LocalDateTime.now());

        Community saved = communityRepository.save(community);   
        Long postId = saved.getPostId();

        if (requestDto.getFileAssets() != null && !requestDto.getFileAssets().isEmpty()) {
            fileRefService.insertFiles(requestDto.getFileAssets(), postId);
        }
        return postId;
    }
	
	@Transactional(readOnly = true)
	public Map<String, Object> getPostForEdit(Long postId) {
		Community community = communityRepository.findById(postId)
				.orElseThrow(() -> new NotFoundException("존재하지 않는 게시글입니다."));
		
	    String processedContent = imageViewService.injectViewUrls(community.getContent());
	    
		Map<String, Object> post = new HashMap<>();
	    post.put("title", community.getTitle());
	    post.put("content", processedContent);
	    
		FileAssetDto f = new FileAssetDto();
		f.setPost_id(postId);
		f.setPost_type(FileCategory.POST_IMAGE.name());
		
		List<FileAssetDto> files = fileRefService.getFilesByPost(f);
		
		Map<String, Object> data = new HashMap<>();
		
		data.put("post", post);
		data.put("files", files);
		
		return data;
	}
	
	@Transactional
	public void editPost(CommunityEditRequestDto dto, Long postId, String userId) {
		Community community = communityRepository.findById(postId)
				.orElseThrow(() -> new NotFoundException("존재하지 않는 게시글입니다."));
		
		if(!community.getMember().getUserId().equals(userId)) {
			throw new ForbiddenException("본인이 작성한 글만 수정할 수 있습니다.");
		}
		
		community.setTitle(dto.getTitle());
		community.setContent(dto.getContent());
		community.setCreatedAt(LocalDateTime.now());
		
		
		if (dto.getDeleted_keys() != null && !dto.getDeleted_keys().isEmpty()) {
			fileRefService.deleteFilesByKeys(dto.getDeleted_keys());  
		}

		if(dto.getFile_assets() != null && !dto.getFile_assets().isEmpty()) {
			fileRefService.insertFiles(dto.getFile_assets(), postId);
		}
	}
	
	@Transactional
	public void deletePost(Long postId, String userId) {
		Community community = communityRepository.findById(postId)
				.orElseThrow(() -> new NotFoundException("존재하지 않는 게시글입니다."));
		
		if(!community.getMember().getUserId().equals(userId)) {
			throw new ForbiddenException("본인이 작성한 글만 삭제할 수 있습니다.");
		}
		
		communityRepository.delete(community);
		
		FileAssetDto param = new FileAssetDto();
		
		param.setPost_id(postId);
		param.setPost_type(FileCategory.POST_IMAGE.name());
		
		fileRefService.deleteFiles(param);
	}

	@Transactional(readOnly = true)
	public Map<String, Object> getMyPosts(String userId, String keyword, int pageNum, int amount){
		Pageable pageable = PageRequest.of(pageNum - 1, amount);
	    Page<CommunityListResponseDto> page = communityRepository.getMyPosts(userId, keyword, pageable);

	    int total = (int) page.getTotalElements();
	    
	    CommonDto community = new CommonDto();
	    community.setPageNum(pageNum);
	    community.setAmount(amount);
	    community.setKeyword(keyword);
	    
	    PageDto pageMaker = new PageDto(community, total);
	    
	    Map<String, Object> result = new HashMap<>();
	    result.put("posts", page.getContent());
	    result.put("pageMaker", pageMaker);
	    
		return result;
	}
	
	public List<CommunityDto> getPopularBoard() {
		return comMapper.getPopularBoard();
	}

	@Transactional
	public void deleteMyPosts(String userId, List<Long> postIds) {
		if (postIds == null || postIds.isEmpty()) return;

	    long ownedCount = communityRepository.countByPostIdInAndMember_UserId(postIds, userId);
	    if (ownedCount != postIds.size()) {
	        throw new ForbiddenException("본인이 작성한 글만 삭제할 수 있습니다.");
	    }

	    communityRepository.deleteMyPosts(userId, postIds);
	    
	    for(Long postId: postIds) {
	    	FileAssetDto param = new FileAssetDto();
			
			param.setPost_id(postId);
			param.setPost_type(FileCategory.POST_IMAGE.name());
			
			fileRefService.deleteFiles(param);
	    }
	}

	public List<CommunityDto> getFavorites(CommunityDto community) {
		return comMapper.getFavorites(community);
	}

	public int favoritesListCnt(CommunityDto community) {
		return comMapper.favoritesListCnt(community);
	}
}
