package com.jam.client.mypage.dao;

import org.apache.ibatis.annotations.Param;

import com.jam.client.member.vo.MemberVO;

public interface MypageDAO {

	int addFavorite(@Param("user_id") String user_id,  @Param("board_type") String boardType, @Param("post_id") Long post_id);
	int deleteFavorite(@Param("user_id") String user_id, @Param("board_type") String boardType, @Param("post_id") Long post_id);
	
	// 사용자 정보 조회
	MemberVO account(String user_id);
}
