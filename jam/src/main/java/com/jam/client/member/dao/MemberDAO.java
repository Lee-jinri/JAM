package com.jam.client.member.dao;

import java.util.HashMap;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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

	// 로그인
	public MemberVO login(MemberVO member);

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
	public MemberVO account(MemberVO member);

	// 마이페이지 - 회원 정보 수정
	public int memberUpdate(MemberVO member);

	// 아이디 찾기
	public MemberVO findId(@Param("email") String email, @Param("phone") String phone);

	// 비밀번호 찾기
	public int findPw(@Param("user_id") String user_id, @Param("email") String email, @Param("phone") String phone);
	
	// 임시 비밀번호로 변경
	public int updatePw(@Param("user_id") String user_id, @Param("user_pw") String user_pw);

	// 전화번호 변경
	public int phoneModi(MemberVO m_vo);
	
	// 비밀번호 확인
	public String pwConfirm(MemberVO m_vo);
	
	// 비밀번호 변경
	public int pwModi(MemberVO m_vo);

	// 주소 변경
	public int addressModi(MemberVO m_vo);

	// 회원 탈퇴
	public int withDraw(String user_id);

	// 카카오 정보 확인
	public MemberVO findKakao(HashMap<String, Object> userInfo);

	// 카카오 정보 저장
	public void kakaoInsert(HashMap<String, Object> userInfo);

	// 네이버 정보 확인
	public MemberVO findNaver(HashMap<String, Object> userInfo);

	// 네이버 정보 저장
	public void naverInsert(HashMap<String, Object> userInfo);


}
