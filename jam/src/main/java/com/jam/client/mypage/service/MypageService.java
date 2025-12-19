package com.jam.client.mypage.service;

import java.util.List;

import com.jam.client.member.vo.MemberVO;
import com.jam.client.mypage.vo.MemberBoardVO;


public interface MypageService {

	boolean addFavorite(String user_id, String boardType, Long post_id);
	boolean deleteFavorite(String user_id, String boardType, Long post_id);

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
	
	// 회원 정보
	public MemberVO account(String user_id);

}
