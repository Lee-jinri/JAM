package com.jam.client.member.service;

import java.util.List;

import com.jam.client.community.vo.CommunityVO;
import com.jam.client.fleaMarket.vo.FleaMarketVO;
import com.jam.client.job.vo.JobVO;
import com.jam.client.member.vo.MemberVO;
import com.jam.client.roomRental.vo.RoomRentalVO;

public interface MemberService {

	// 회원가입
	public int join(MemberVO member) throws Exception;

	// 아이디 중복확인
	public int idCheck(String userId) throws Exception;

	// 닉네임 중복확인
	public int nameCheck(String user_name) throws Exception;

	// 전화번호 중복확인
	public int phoneCheck(String phone) throws Exception;

	// 로그인
	public MemberVO login(MemberVO member);

	// 마이페이지
	public List<CommunityVO> comMyWrite(CommunityVO com_vo);
	public List<FleaMarketVO> fleaMyWrite(FleaMarketVO flea_vo);
	public List<JobVO> jobMyWrite(JobVO jov_vo);
	public List<RoomRentalVO> roomMyWrite(RoomRentalVO room_vo);
	
	// 마이페이지 - 작성 글 페이징
	public int myComListCnt(CommunityVO com_vo);
	public int myFleaListCnt(FleaMarketVO flea_vo);
	public int myJobListCnt(JobVO jov_vo);
	public int myRoomListCnt(RoomRentalVO room_vo);
		
	// 마이페이지 - 회원 정보 페이지
	public MemberVO account(MemberVO member);

	// 마이페이지 - 회원 정보 수정
	public int memberUpdate(MemberVO member);

	// 아이디 찾기
	public MemberVO FindId(String user_name, String phone);

	// 비밀번호 찾기
	public MemberVO FindPw(String user_id, String user_name, String phone);

	// 카카오 로그인
	public String getAccessToken(String code);
	
	// 전화번호 변경
	public int phoneModi(MemberVO m_vo);
	
	// 비밀번호 확인
	public int pwConfirm(MemberVO m_vo);
	
	// 비밀번호 변경
	public int pwModi(MemberVO m_vo);

	// 주소 변경
	public int addressModi(MemberVO m_vo);

	// 회원 탈퇴
	public int withDraw(String user_id);

	
	

	

	

	


	

	
	
	
	
}
