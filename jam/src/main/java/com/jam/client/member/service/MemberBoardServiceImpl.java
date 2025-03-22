package com.jam.client.member.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jam.client.member.dao.MemberBoardDAO;
import com.jam.client.member.vo.MemberBoardVO;

@Service
public class MemberBoardServiceImpl implements MemberBoardService{
	
	@Autowired
	private MemberBoardDAO memberBoardDao;
	
	// 게시판별 북마크 목록 조회 (community, job, fleaMarket, roomRental)
	
	@Override
	public List<MemberBoardVO> getFavoriteCommunity(MemberBoardVO favorite) {
		return memberBoardDao.getFavoriteCommunity(favorite);
	}
	
	@Override
	public List<MemberBoardVO> getFavoriteJob(MemberBoardVO favorite){
		return memberBoardDao.getFavoriteJob(favorite);
	}
	
	@Override
	public List<MemberBoardVO> getFavoriteFlea(MemberBoardVO favorite){
		return memberBoardDao.getFavoriteFlea(favorite);
	}
	
	@Override
	public List<MemberBoardVO> getFavoriteRoom(MemberBoardVO favorite){
		return memberBoardDao.getFavoriteRoom(favorite);
	}
	
	
	
	/***
	 * 북마크 글 추가
	 * 
	 * @param user_id 	사용자 아이디
	 * @param boardType 게시판 타입 (community, job, fleaMarket, roomRental)
	 * @param boardNo 	추가할 게시글 번호
	 */
	@Override
	public boolean addFavorite(String user_id, String boardType, Long boardNo) {
		int result = memberBoardDao.addFavorite(user_id, boardType, boardNo);
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
	public boolean deleteFavorite(String user_id, Long boardNo, String boardType) {
		int result = memberBoardDao.deleteFavorite(user_id, boardType, boardNo);
		return result == 1;
	}

	/**
	 * 북마크: 게시판별 게시글 개수를 조회 (페이징 처리를 위함)
	 *
	 * @param userId 사용자 아이디
	 * @param boardType 게시판 타입 (community, job, fleaMarket, roomRental)
	 * @return 게시글 개수
	 */
	@Override
	public int listCnt(String boardType, String userId) {
		return memberBoardDao.listCnt(boardType, userId);
	}

	
	
	// 게시판별 작성한 글 목록 조회 (community, job, fleaMarket, roomRental)
	
	@Override
	public List<MemberBoardVO> getWrittenCommunity(MemberBoardVO written) {
		String search = resolveSearchColumn(written.getBoard_type(), written.getSearch());
		
		written.setSearch(search);
		
		return memberBoardDao.getWrittenCommunity(written);
	}

	@Override
	public List<MemberBoardVO> getWrittenJob(MemberBoardVO written) {
		String search = resolveSearchColumn(written.getBoard_type(), written.getSearch());
		
		written.setSearch(search);
		
		return memberBoardDao.getWrittenJob(written);
	}

	@Override
	public List<MemberBoardVO> getWrittenFlea(MemberBoardVO written) {
		String search = resolveSearchColumn(written.getBoard_type(), written.getSearch());
		
		written.setSearch(search);	
		return memberBoardDao.getWrittenFlea(written);
	}

	@Override
	public List<MemberBoardVO> getWrittenRoom(MemberBoardVO written) {
		String search = resolveSearchColumn(written.getBoard_type(), written.getSearch());
		
		written.setSearch(search);	
		return memberBoardDao.getWrittenRoom(written);
	}
	
	private String resolveSearchColumn(String boardType, String search) {
	    if (search == null || search.isEmpty()) {
	        return null;
	    }

	    switch (boardType) {
	        case "community":
	            return search.equals("title") ? "com_title" : "com_content";
	        case "job":
	            return search.equals("title") ? "job_title" : "job_content";
	        case "fleaMarket":
	            return search.equals("title") ? "flea_title" : "flea_content";
	        case "roomRental":
	            return search.equals("title") ? "roomRental_title" : "roomRental_content";
	        default:
	            return null;
	    }
	}

	/**
	 * 작성한 글 수 조회 (페이징 처리를 위함)
	 *
	 * @param written 게시글 정보 객체
	 *        - user_id     : 사용자 아이디
	 *        - board_type  : 게시판 타입 (community, job, fleaMarket, roomRental)
	 *        - search      : 검색 조건 (title, content)
	 *        - keyword     : 검색 키워드
	 * @return 게시글 개수
	 */
	@Override
	public int writtenListCnt(MemberBoardVO written) {
		String search = resolveSearchColumn(written.getBoard_type(), written.getSearch());
		
		written.setSearch(search);	
		
		return memberBoardDao.writtenListCnt(written);
	}
	
}
