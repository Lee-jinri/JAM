package com.jam.client.member.service;


import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

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

	// 이메일 중복확인
	public int emailCheck(String email) throws Exception;


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
	
	// 아이디 찾기
	public String FindId(String email, String phone);

	// 비밀번호 찾기
	public int FindPw(String user_id, String email, String phone);

	// 임시 비밀번호로 변경
	public ResponseEntity<String> updatePwAndSendEmail(String user_id, String email);

	// 소셜 로그인 정보 저장
	public MemberVO socialLoginOrRegister(Map<String, Object> userInfo, String provider);
	
	// 닉네임 변경
	public boolean updateUserName(MemberVO member);
	
	// 전화번호 변경
	public boolean updatePhone(MemberVO m_vo);
	
	// 비밀번호 확인
	public String pwConfirm(MemberVO m_vo);
	
	// 비밀번호 변경
	public int updatePw(String user_id, String user_pw);

	// 주소 변경
	public int updateAddress(MemberVO m_vo);

	// 회원 탈퇴
	public void deleteAccount(String user_id);
	
	// 아이디로 닉네임 가져오기
	public String getUserName(String user_id);
	
	// 닉네임으로 아이디 가져오기
	public String getUserId(String user_name);

	// refresh 토큰 저장
	public int addRefreshToken(String user_id, String refreshToken);

	// refresh 토큰 삭제
	public int deleteRefreshToken(String user_id);

	// refresh 토큰 가져오기
	public String getRefreshToken(String user_id);

	public MemberVO getUserInfo(String refreshToken);
	
	MemberVO getUserProfile(String user_id);

	Authentication authenticateSocialUser(MemberVO user);

	public void kakaoDeleteAccount(String kakaoAccessToken);
	
	public void naverDeleteAccount(String naverAccessToken);
	
	
}
