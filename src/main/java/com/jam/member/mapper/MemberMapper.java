package com.jam.member.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.jam.community.dto.CommunityDto;
import com.jam.fleaMarket.dto.FleaMarketDto;
import com.jam.job.dto.JobDto;
import com.jam.member.dto.MemberDto;
import com.jam.studio.dto.StudioDto;

public interface MemberMapper {
	// 회원가입
	void memberJoin(MemberDto member);
	int assignDefaultRoleToMember(String userId);
	
	// 아이디 중복확인
	int idCheck(String userId);

	// 닉네임 중복확인
	int nameCheck(String user_name);

	// 전화번호 중복확인
	int phoneCheck(String phone);
	
	// 이메일 중복확인
	int emailCheck(String email);

	// 마이페이지 내가 쓴 글 
	List<CommunityDto> comMyWrite(CommunityDto community);
	List<FleaMarketDto> fleaMyWrite(FleaMarketDto flea);
	List<JobDto> jobMyWrite(JobDto job);
	List<StudioDto> roomMyWrite(StudioDto studio);
	
	// 마이페이지 작성글 페이징
	int myComListCnt(CommunityDto community);
	int myFleaListCnt(FleaMarketDto flea);
	int myJobListCnt(JobDto job);
	int myRoomListCnt(StudioDto studio);
	
	
	// 마이페이지 - 회원 정보 페이지
	MemberDto account(String user_id);

	// 아이디 찾기
	String findId(@Param("email") String email, @Param("phone") String phone);

	// 아이디와 이메일로 존재하는 사용자인지 확인 (비밀번호 변경 전 사용)
	int countByUserIdEmailPhone(@Param("user_id") String user_id, @Param("email") String email, @Param("phone") String phone);
	
	// 비밀번호 변경
	int updatePw(@Param("user_id") String user_id, @Param("user_pw") String user_pw);

	// 닉네임 변경
	int updateUserName(MemberDto member);
	
	// 전화번호 변경
	int updatePhone(MemberDto member);
	
	// 비밀번호 확인을 위해 비밀번호 가져옴
	String getPassword(MemberDto member);

	// 주소 변경
	int updateAddress(MemberDto member);

	// 회원 탈퇴
	int deleteAccount(String user_id);

	// 소셜 사용자 회원가입 여부 
	int findSocialUser(String user_id);

	// 소셜 사용자 회원가입
	void SocialRegister(Map<String, Object> userInfo);

	// 사용자 정보 확인
	MemberDto findByUserInfo(String username);

	// 회원 닉네임 가져오기
	String getUserName(String user_id);

	// 아이디 가져오기
	String getUserId(String user_name);
	
	// refresh 토큰 저장
	int addRefreshToken(@Param("user_id") String user_id, @Param("refreshToken") String refreshToken);

	// refresh 토큰 삭제
	int deleteRefreshToken(String user_id);

	// refresh 토큰 가져오기
	String getRefreshToken(String user_id);

	// refresh token으로 회원 아이디 조회
	String findUserIdByRefreshToken(String refreshToken);
	
	// 사용자 프로필 가져오기
	MemberDto getUserProfile(String user_id);

	void updateCompanyName(Map<String, String> param);
	
	void insertCompanyRole(Map<String, String> param);
}
