package com.jam.client.member.service;

import java.util.List;

import com.jam.client.member.vo.MemberBoardVO;

public interface MemberBoardService {

	boolean addFavorite(String user_id, String boardType, Long boardNo);
	boolean deleteFavorite(String user_id, Long boardNo, String boardType);

	List<MemberBoardVO> getFavoriteCommunity(MemberBoardVO favorite);
	List<MemberBoardVO> getFavoriteJob(MemberBoardVO favorite);
	List<MemberBoardVO> getFavoriteFlea(MemberBoardVO favorite);
	List<MemberBoardVO> getFavoriteRoom(MemberBoardVO favorite);

	int listCnt(String boardType, String userId);
	
	List<MemberBoardVO> getWrittenCommunity(MemberBoardVO written);
	List<MemberBoardVO> getWrittenJob(MemberBoardVO written);
	List<MemberBoardVO> getWrittenFlea(MemberBoardVO written);
	List<MemberBoardVO> getWrittenRoom(MemberBoardVO written);

	int writtenListCnt(MemberBoardVO written);
}
