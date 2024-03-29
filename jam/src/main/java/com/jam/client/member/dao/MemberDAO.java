package com.jam.client.member.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.jam.client.community.vo.CommunityVO;
import com.jam.client.fleaMarket.vo.FleaMarketVO;
import com.jam.client.job.vo.JobVO;
import com.jam.client.member.vo.MemberVO;
import com.jam.client.roomRental.vo.RoomRentalVO;

public interface MemberDAO {
	
	// 회원가입
	public int memberJoin(MemberVO member) throws Exception;

	// 아이디 중복확인
	public int idCheck(String userId) throws Exception;

	// 닉네임 중복확인
	public int nameCheck(String user_name) throws Exception;

	// 전화번호 중복확인
	public int phoneCheck(String phone) throws Exception;
	
	// 이메일 중복확인
	public int emailCheck(String email) throws Exception;


	// 마이페이지 내가 쓴 글 
	public List<CommunityVO> comMyWrite(CommunityVO com_vo);
	public List<FleaMarketVO> fleaMyWrite(FleaMarketVO flea_vo);
	public List<JobVO> jobMyWrite(JobVO job_vo);
	public List<RoomRentalVO> roomMyWrite(RoomRentalVO room_vo);
	
	// 마이페이지 작성글 페이징
	public int myComListCnt(CommunityVO com_vo);
	public int myFleaListCnt(FleaMarketVO flea_vo);
	public int myJobListCnt(JobVO job_vo);
	public int myRoomListCnt(RoomRentalVO room_vo);
	
	
	// 마이페이지 - 회원 정보 페이지
	public MemberVO account(String user_id);

	// 아이디 찾기
	public MemberVO findId(@Param("email") String email, @Param("phone") String phone);

	// 비밀번호 찾기
	public int findPw(@Param("user_id") String user_id, @Param("email") String email, @Param("phone") String phone);
	
	// 비밀번호 변경
	public int updatePw(@Param("user_id") String user_id, @Param("user_pw") String user_pw);

	// 전화번호 변경
	public int updatePhone(MemberVO m_vo);
	
	// 비밀번호 확인
	public String pwConfirm(MemberVO m_vo);

	// 주소 변경
	public int updateAddress(MemberVO m_vo);

	// 회원 탈퇴
	public int withDraw(String user_id);

	// 소셜 사용자 회원가입 여부 
	public int findSocialUser(MemberVO member);

	// 소셜 사용자 회원가입
	public void SocialRegister(MemberVO member);

	// 사용자 정보 확인
	public MemberVO findByUsername(String username);

	// 회원 닉네임 가져오기
	public String getUserName(String user_id);

	// refresh 토큰 저장
	public int addRefreshToken(@Param("user_id") String user_id, @Param("refreshToken") String refreshToken);

	// refresh 토큰 삭제
	public int deleteRefreshToken(String user_id);

	// refresh 토큰 가져오기
	public String getRefreshToken(String user_id);
	
	



}
