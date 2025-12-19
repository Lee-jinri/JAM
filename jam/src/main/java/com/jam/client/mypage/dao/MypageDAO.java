package com.jam.client.mypage.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.jam.client.member.vo.MemberVO;
import com.jam.client.mypage.vo.MemberBoardVO;

public interface MypageDAO {

	int addFavorite(@Param("user_id") String user_id,  @Param("board_type") String boardType, @Param("post_id") Long post_id);
	int deleteFavorite(@Param("user_id") String user_id, @Param("board_type") String boardType, @Param("post_id") Long post_id);
	
	List<MemberBoardVO> getFavoriteCommunity(MemberBoardVO favorite);
	List<MemberBoardVO> getFavoriteJob(MemberBoardVO favorite);
	List<MemberBoardVO> getFavoriteFlea(MemberBoardVO favorite);
	List<MemberBoardVO> getFavoriteRoom(MemberBoardVO favorite);

	List<MemberBoardVO> getWrittenCommunity(MemberBoardVO written);
	List<MemberBoardVO> getWrittenJob(MemberBoardVO written);
	List<MemberBoardVO> getWrittenFlea(MemberBoardVO written);
	List<MemberBoardVO> getWrittenRoom(MemberBoardVO written);
	
	int listCnt(@Param("board_type") String boardType, @Param("user_id") String userId);
	int writtenListCnt(MemberBoardVO writtend);
	
	// 사용자 정보 조회
	MemberVO account(String user_id);
	
}
