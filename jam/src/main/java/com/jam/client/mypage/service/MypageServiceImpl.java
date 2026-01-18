package com.jam.client.mypage.service;

import org.springframework.stereotype.Service;

import com.jam.client.member.vo.MemberVO;
import com.jam.client.mypage.dao.MypageDAO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MypageServiceImpl implements MypageService{
	
	private final MypageDAO mypageDao;
	
	/***
	 * 북마크 글 추가
	 * 
	 * @param user_id 	사용자 아이디
	 * @param boardType 게시판 타입 (community, job, fleaMarket, roomRental)
	 * @param boardNo 	추가할 게시글 번호
	 */
	@Override
	public boolean addFavorite(String user_id, String boardType, Long post_id) {
		int result = mypageDao.addFavorite(user_id, boardType, post_id);
		return result == 1;
	}

	/***
	 * 북마크 글 삭제
	 * 
	 * @param user_id 	사용자 아이디
	 * @param boardType 게시판 타입 (community, job, fleaMarket, roomRental)
	 * @param boardNo  	삭제할 게시글 번호
	 */
	@Override
	public boolean deleteFavorite(String user_id, String boardType, Long post_id) {
		int result = mypageDao.deleteFavorite(user_id, boardType, post_id);
		return result == 1;
	}
	
	// 마이페이지 - 회원 정보 페이지
	@Override
	public MemberVO account(String user_id) {
		return mypageDao.account(user_id);
	}
}
