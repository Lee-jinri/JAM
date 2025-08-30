package com.jam.client.fleaMarket.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.jam.client.fleaMarket.vo.FleaMarketVO;
import com.jam.file.vo.ImageFileVO;

public interface FleaMarketService {

	// 중고거래 전체 글 조회
	List<FleaMarketVO> getBoard(FleaMarketVO flea_vo);

	// 중고거래 페이징
	int listCnt(FleaMarketVO flea_vo);
	
	// 중고거래 조회수 증가
	void incrementReadCnt(Long flea_no);

	// 중고거래 상세페이지 조회
	FleaMarketVO getPostDetail(Long flea_no);

	// 중고거래 글 작성
	long writePost(FleaMarketVO flea_vo, List<MultipartFile> images);

	// 중고거래 수정할 글 정보 불러오기
	FleaMarketVO getPostForEdit(Long flea_no);

	// 중고거래  글 수정
	int editPost(FleaMarketVO flea_vo);

	// 중고거래 글 삭제
	int deletePost(Long flea_no, String user_id);

	List<FleaMarketVO> getMyStore(FleaMarketVO flea_vo);

	int getMyStoreCnt(FleaMarketVO flea_vo);

	String getUserId(String user_name);

	boolean isValidUserName(String user_name) throws Exception;

	List<FleaMarketVO> getFavorites(FleaMarketVO flea);

	List<ImageFileVO> getImages(Long post_id);
	
}
