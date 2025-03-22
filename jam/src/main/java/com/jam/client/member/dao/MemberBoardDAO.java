package com.jam.client.member.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.jam.client.member.vo.MemberBoardVO;

public interface MemberBoardDAO {

	int addFavorite(@Param("user_id") String user_id,  @Param("board_type") String boardType, @Param("board_no") Long boardNo);
	int deleteFavorite(@Param("user_id") String user_id, @Param("board_type") String boardType, @Param("board_no") Long boardNo);
	
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
	
}
