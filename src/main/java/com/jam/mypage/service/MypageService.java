package com.jam.mypage.service;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import com.jam.global.exception.ConflictException;
import com.jam.member.dto.MemberDto;
import com.jam.mypage.mapper.MypageMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MypageService {

	private final MypageMapper mypageMapper;

	/***
	 * 북마크 글 추가
	 *
	 * @param user_id 	사용자 아이디
	 * @param boardType 게시판 타입 (community, job, fleaMarket, roomRental)
	 * @param boardNo 	추가할 게시글 번호
	 */
	public boolean addFavorite(String user_id, String boardType, Long post_id) {
		try {
			int result = mypageMapper.addFavorite(user_id, boardType, post_id);
			return result == 1;
		} catch (DuplicateKeyException e) {
			throw new ConflictException("이미 즐겨찾기에 추가한 게시글입니다.");
		}
	}

	/***
	 * 북마크 글 삭제
	 * 
	 * @param user_id 	사용자 아이디
	 * @param boardType 게시판 타입 (community, job, fleaMarket, roomRental)
	 * @param boardNo  	삭제할 게시글 번호
	 */
	public boolean deleteFavorite(String user_id, String boardType, Long post_id) {
		int result = mypageMapper.deleteFavorite(user_id, boardType, post_id);
		return result == 1;
	}
	
	// 마이페이지 - 회원 정보 페이지
	public MemberDto account(String user_id) {
		return mypageMapper.account(user_id);
	}
}
