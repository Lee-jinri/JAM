package com.jam.community.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jam.community.dto.CommunityDto;
import com.jam.community.mapper.CommunityMapper;
import com.jam.file.dto.FileAssetDto;
import com.jam.file.dto.FileCategory;
import com.jam.global.service.FileReferenceService;
import com.jam.global.service.PostImageViewService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommunityService {

	private final CommunityMapper comMapper;
	private final PostImageViewService imageViewService;
	private final FileReferenceService fileRefService;	
	
	public List<CommunityDto>getBoard(CommunityDto com){
		
		List<CommunityDto> list = new ArrayList<>();
		
		if(com.getUser_id() == null) list = comMapper.getBoard(com);
		else list = comMapper.getBoardWithFavorite(com);
		
		return list;
	}
	
	public int listCnt(CommunityDto com) {
		return comMapper.listCnt(com);
	}
	
	public void incrementReadCnt(Long post_id) {
		comMapper.incrementReadCnt(post_id);
	}
	
	public CommunityDto getPost(Long post_id) {
		
		CommunityDto post = comMapper.getPost(post_id);
		  
		String processedContent = imageViewService.injectViewUrls(post.getContent());
		post.setContent(processedContent);
		return post;
	}

	@Transactional
	public Long writePost(CommunityDto com) {
		
		comMapper.writePost(com);
		Long postId = com.getPost_id();
		
		if(com.getFile_assets() != null && !com.getFile_assets().isEmpty()) {
			fileRefService.insertFiles(com.getFile_assets(), postId);
		}
		
		return postId;
	}
	
	@Transactional
	public int editPost(CommunityDto post) {

		if (post.getDeleted_keys() != null && !post.getDeleted_keys().isEmpty()) {
			fileRefService.deleteFilesByKeys(post.getDeleted_keys());  
		}

		if(post.getFile_assets() != null && !post.getFile_assets().isEmpty()) {
			fileRefService.insertFiles(post.getFile_assets(), post.getPost_id());
		}
		
		return comMapper.editPost(post);
	}
	
	public void deletePost(Long post_id, String user_id) {
		int result = comMapper.deletePost(post_id, user_id);
		
		if(result > 0) {
			FileAssetDto param = new FileAssetDto();
			
			param.setPost_id(post_id);
			param.setPost_type(FileCategory.POST_IMAGE.name());
			
			fileRefService.deleteFiles(param);
		}
	}

	public List<CommunityDto> getMyPosts(CommunityDto com){
		return comMapper.getMyPosts(com);
	}

	public int getMyPostsCnt(CommunityDto com) {
		return comMapper.getMyPostsCnt(com);
	}
	
	public List<CommunityDto> getPopularBoard() {
		return comMapper.getPopularBoard();
	}

	public Map<String, Object> getPostForEdit(Long post_id) {
		CommunityDto post = comMapper.getPostById(post_id);
		
		String processedContent = imageViewService.injectViewUrls(post.getContent());
		post.setContent(processedContent);
		
		FileAssetDto f = new FileAssetDto();
		f.setPost_id(post_id);
		f.setPost_type(FileCategory.POST_IMAGE.name());
		
		List<FileAssetDto> files = fileRefService.getFilesByPost(f);
		
		Map<String, Object> data = new HashMap<>();
		
		data.put("post", post);
		data.put("files", files);
		
		return data;
	}

	public void deleteMyPosts(String userId, List<Long> postIds) {
		if (postIds == null || postIds.isEmpty()) return;
		comMapper.deleteMyPosts(userId, postIds);
	}

	public List<CommunityDto> getFavorites(CommunityDto community) {
		return comMapper.getFavorites(community);
	}

	public int favoritesListCnt(CommunityDto community) {
		return comMapper.favoritesListCnt(community);
	}
}
