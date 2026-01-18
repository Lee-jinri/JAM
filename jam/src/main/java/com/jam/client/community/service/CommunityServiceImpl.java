package com.jam.client.community.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jam.client.community.dao.CommunityDAO;
import com.jam.client.community.vo.CommunityVO;
import com.jam.file.vo.FileAssetVO;
import com.jam.file.vo.FileCategory;
import com.jam.global.service.FileReferenceService;
import com.jam.global.service.PostImageViewService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommunityServiceImpl implements CommunityService {
	
	private final CommunityDAO comDao;
	private final PostImageViewService imageViewService;
	private final FileReferenceService fileRefService;	
	
	@Override
	public List<CommunityVO>getBoard(CommunityVO com_vo){
		
		List<CommunityVO> list = new ArrayList<>();
		
		if(com_vo.getUser_id() == null) list = comDao.getBoard(com_vo);
		else list = comDao.getBoardWithFavorite(com_vo);
		
		return list;
	}
	
	@Override
	public int listCnt(CommunityVO com_vo) {
		return comDao.listCnt(com_vo);
	}
	
	@Override
	public void incrementReadCnt(Long post_id) {
		comDao.incrementReadCnt(post_id);
	}
	
	@Override
	public CommunityVO getPost(Long post_id) {
		
		CommunityVO post = comDao.getPost(post_id);
		  
		String processedContent = imageViewService.injectViewUrls(post.getContent());
		post.setContent(processedContent);
		return post;
	}

	@Override
	@Transactional
	public Long writePost(CommunityVO com) {
		
		comDao.writePost(com);
		Long postId = com.getPost_id();
		
		if(com.getFile_assets() != null && !com.getFile_assets().isEmpty()) {
			fileRefService.insertFiles(com.getFile_assets(), postId);
		}
		
		return postId;
	}
	
	@Transactional
	@Override
	public int editPost(CommunityVO post) {

		if (post.getDeleted_keys() != null && !post.getDeleted_keys().isEmpty()) {
			fileRefService.deleteFilesByKeys(post.getDeleted_keys());  
		}

		if(post.getFile_assets() != null && !post.getFile_assets().isEmpty()) {
			fileRefService.insertFiles(post.getFile_assets(), post.getPost_id());
		}
		
		return comDao.editPost(post);
	}
	
	@Override
	public void deletePost(Long post_id, String user_id) {
		int result = comDao.deletePost(post_id, user_id);
		
		if(result > 0) {
			FileAssetVO param = new FileAssetVO();
			
			param.setPost_id(post_id);
			param.setPost_type(FileCategory.POST_IMAGE.name());
			
			fileRefService.deleteFiles(param);
		}
	}

	@Override
	public List<CommunityVO> getMyPosts(CommunityVO com_vo){
		return comDao.getMyPosts(com_vo);
	}

	@Override
	public int getMyPostsCnt(CommunityVO com_vo) {
		return comDao.getMyPostsCnt(com_vo);
	}
	
	@Override
	public List<CommunityVO> getPopularBoard() {
		return comDao.getPopularBoard();
	}

	@Override
	public Map<String, Object> getPostForEdit(Long post_id) {
		CommunityVO post = comDao.getPostById(post_id);
		
		String processedContent = imageViewService.injectViewUrls(post.getContent());
		post.setContent(processedContent);
		
		FileAssetVO f = new FileAssetVO();
		f.setPost_id(post_id);
		f.setPost_type(FileCategory.POST_IMAGE.name());
		
		List<FileAssetVO> files = fileRefService.getFilesByPost(f);
		
		Map<String, Object> data = new HashMap<>();
		
		data.put("post", post);
		data.put("files", files);
		
		return data;
	}

	@Override
	public void deleteMyPosts(String userId, List<Long> postIds) {
		if (postIds == null || postIds.isEmpty()) return;
		comDao.deleteMyPosts(userId, postIds);
	}

	@Override
	public List<CommunityVO> getFavorites(CommunityVO community) {
		return comDao.getFavorites(community);
	}

	@Override
	public int favoritesListCnt(CommunityVO community) {
		return comDao.favoritesListCnt(community);
	}
}
