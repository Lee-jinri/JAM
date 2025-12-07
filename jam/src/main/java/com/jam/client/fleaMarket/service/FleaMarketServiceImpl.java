package com.jam.client.fleaMarket.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.jam.client.fleaMarket.dao.FleaMarketDAO;
import com.jam.client.fleaMarket.vo.FleaMarketVO;
import com.jam.client.member.service.MemberService;
import com.jam.file.dao.ImageFileDAO;
import com.jam.file.vo.ImageFileVO;
import com.jam.global.util.FileUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FleaMarketServiceImpl implements FleaMarketService {

	private final FleaMarketDAO fleaDao;
	private final MemberService memberService;
	private final FileUtils fileUtils;
	private final ImageFileDAO imageFileDao;
	
	@Override
	public List<FleaMarketVO> getBoard(FleaMarketVO flea_vo) {
		
		List<FleaMarketVO> list = new ArrayList<>(); 
		
		if(flea_vo.getUser_id() == null) list = fleaDao.getBoard(flea_vo);
		else list = fleaDao.getBoardWithFavorite(flea_vo);
		
		return list;
	}
	
	@Override
	public int listCnt(FleaMarketVO flea_vo) {
		return fleaDao.listCnt(flea_vo);
	}

	@Override
	public void incrementReadCnt(Long post_id) {
		
		fleaDao.incrementReadCnt(post_id);
	}

	@Override
	public FleaMarketVO getPostDetail(Long post_id) {
		
		FleaMarketVO detail = fleaDao.getPostDetail(post_id);
		
		return detail;
	}

	@Override
	@Transactional
	public long writePost(FleaMarketVO flea_vo, List<MultipartFile> images) {
		
		long post_id = fleaDao.getNextPostId();
		
		flea_vo.setPost_id(post_id);
		String thumbnail = null;
		
		for (int i = 0; i < images.size(); i++) {
			MultipartFile image = images.get(i);
            String savedFileName = fileUtils.saveToLocal(image, "flea"); // 파일 저장
            
            if (savedFileName == null) {
                throw new RuntimeException("이미지 저장 실패");
            }
            
            if (i == 0) thumbnail = savedFileName;
            
            ImageFileVO imageVO = new ImageFileVO();
            
            imageVO.setPost_id((long) post_id);
            imageVO.setImage_name(savedFileName);
            imageVO.setPost_type("flea");
            
            imageFileDao.insertImage(imageVO); // 이미지 메타정보 DB에 저장
        }
		
		flea_vo.setThumbnail(thumbnail);
		fleaDao.writePost(flea_vo);
		
		return post_id;
	}

	@Override
	public FleaMarketVO getPostForEdit(Long post_id) {
		
		FleaMarketVO updateData =  fleaDao.getPostForEdit(post_id);
		
		return updateData;
	}

	@Override
	public int editPost(FleaMarketVO flea_vo) {
		
		return fleaDao.editPost(flea_vo);
	}

	@Override
	public int deletePost(Long flea_no, String userId) {
	
		return fleaDao.deletePost(flea_no, userId);
	}

	@Override
	public List<FleaMarketVO> getMyStore(FleaMarketVO flea_vo) {
		return fleaDao.getMyStore(flea_vo);
	}

	@Override
	public int getMyStoreCnt(FleaMarketVO flea_vo) {
		return fleaDao.getMyStoreCnt(flea_vo);
	}

	@Override
	public String getUserId(String user_name) {
		return memberService.getUserId(user_name);
	}
	
	@Override
	public boolean isValidUserName(String user_name) throws Exception {
		int count = memberService.nameCheck(user_name);
		
		return count != 0 ? true : false;
	}

	@Override
	public List<FleaMarketVO> getFavorites(FleaMarketVO flea) {
		return fleaDao.getFavorites(flea);
	}

	@Override
	public List<ImageFileVO> getImages(Long post_id) {
		return fleaDao.getImages(post_id);
	}
}
