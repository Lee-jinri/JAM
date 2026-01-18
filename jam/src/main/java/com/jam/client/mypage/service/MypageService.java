package com.jam.client.mypage.service;

import com.jam.client.member.vo.MemberVO;


public interface MypageService {

	boolean addFavorite(String user_id, String boardType, Long post_id);
	boolean deleteFavorite(String user_id, String boardType, Long post_id);
	
	// 회원 정보
	public MemberVO account(String user_id);

}
