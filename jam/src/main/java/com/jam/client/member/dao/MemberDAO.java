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

	// 로그인
	public MemberVO login(MemberVO member);

	// 마이페이지 내가 쓴 글 
	public List<CommunityVO> comMyWrite(MemberVO member);
	public List<FleaMarketVO> fleaMyWrite(MemberVO member);
	public List<JobVO> jobMyWrite(MemberVO member);
	public List<RoomRentalVO> roomMyWrite(MemberVO member);
	
	// 마이페이지 - 회원 정보 페이지
	public MemberVO account(MemberVO member);

	// 마이페이지 - 회원 정보 수정
	public int memberUpdate(MemberVO member);

	// 아이디 찾기
	public MemberVO findId(@Param("user_name") String user_name, @Param("phone") String phone);

	// 비밀번호 찾기
	public MemberVO findPw(@Param("user_id") String user_id, @Param("user_name") String user_name, @Param("phone") String phone);

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
