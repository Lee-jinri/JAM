package com.jam.mypage.mapper;

import org.apache.ibatis.annotations.Param;

import com.jam.member.dto.MemberDto;

public interface MypageMapper {
	int addFavorite(@Param("user_id") String user_id,  @Param("board_type") String boardType, @Param("post_id") Long post_id);
	int deleteFavorite(@Param("user_id") String user_id, @Param("board_type") String boardType, @Param("post_id") Long post_id);
	
	// 사용자 정보 조회
	MemberDto account(String user_id);
}
