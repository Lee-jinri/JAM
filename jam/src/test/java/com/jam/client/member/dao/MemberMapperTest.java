package com.jam.client.member.dao;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.jam.client.community.dao.ComMapperTest;
import com.jam.client.community.vo.CommunityVO;
import com.jam.client.fleaMarket.vo.FleaMarketVO;
import com.jam.client.job.vo.JobVO;
import com.jam.client.member.vo.MemberVO;
import com.jam.client.roomRental.vo.RoomRentalVO;
import com.jam.config.RootConfig;

import lombok.Setter;
import lombok.extern.log4j.Log4j;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = RootConfig.class)
@Log4j
public class MemberMapperTest {

	@Setter(onMethod_=@Autowired)
	private MemberDAO memberDao;
	
	/* 회원가입 
	@Test
	public void testMemberInsert() throws Exception {
		MemberVO member = new MemberVO();
		member.setUser_id("abcd12345");
		member.setUser_pw("abcd12345");
		member.setUser_name("김영희");
		member.setPhone("01011154575");
		member.setAddress("서울시 강남구 강남동 123번지");
		member.setEmail("abcde1234@abcde.com");
		
		int count = memberDao.memberJoin(member);
		log.info( "회원가입 : "+ count);
	} */
	
	
	/* 아이디 중복 확인 
	@Test
	public void testMemberIdChk() throws Exception{
		String id = "abcd12345";	// 존재하는 아이디
		String id2 = "test123";	// 존재하지 않는 아이디
		
		log.info("존재하는 아이디 : " + memberDao.idCheck(id));
		log.info("존재하지 않는 아이디 : " + memberDao.idCheck(id2));
	}*/
	
	
	/* 닉네임 중복 확인
	@Test
	public void testMemberNameChk() throws Exception{
		String name = "김철수";	// 존재하는 닉네임
		String name2 = "홍길동";	// 존재하지 않는 닉네임
		
		log.info(memberDao.nameCheck(name));
		log.info(memberDao.nameCheck(name2));
	}
	 */
	
	/* 전화번호 중복 확인
	@Test
	public void testMemberPhoneChk() throws Exception{
		String phone = "01012345678"; // 존재하는 전화번호
		String phone2 = "01000001111"; // 존재하지 않는 전화번호
		
		log.info(memberDao.phoneCheck(phone));
		log.info(memberDao.phoneCheck(phone2));
	}
	*/
	
	/* 이메일 중복 확인 
	@Test
	public void testMemberEmailChk() throws Exception{
		String email1 = "xswxsw970@naver.com";
		String email2 = "aaaaa1111@aaaaa.aaa";
		
		log.info("존재하는 이메일 : " + memberDao.emailCheck(email1));
		log.info("존재하지 않는 이메일 : " + memberDao.emailCheck(email2));
		
	}*/
	
	/* 마이페이지 내가 쓴 글 
	@Test
	public void testMyWrite() {
		CommunityVO com_vo = new CommunityVO();
		FleaMarketVO flea_vo = new FleaMarketVO();
		JobVO job_vo = new JobVO();
		RoomRentalVO room_vo = new RoomRentalVO();
		
		com_vo.setUser_id("abcd1234");
		flea_vo.setUser_id("abce1234");
		job_vo.setUser_id("abcd1234");
		room_vo.setUser_id("abcd1234");
		
		log.info(memberDao.comMyWrite(com_vo));
		log.info(memberDao.fleaMyWrite(flea_vo));
		log.info(memberDao.jobMyWrite(job_vo));
		log.info(memberDao.roomMyWrite(room_vo));
		
	}*/
	
	
	
	/* 아이디 찾기 
	@Test
	public void testFindId() {
		String email = "ar971004@naver.com";
		String phone = "01012345678";
		
		log.info(memberDao.findId(email, phone));
	}*/
	
	/* 비밀번호 찾기 
	@Test
	public void testFindPw() {
		String user_id = "abcd1234";
		String email = "ar971004@naver.com";
		String phone = "01012345678";
		
		log.info(memberDao.findPw(user_id, email, phone));
	}*/
	
	/* 비밀번호 변경 
	@Test
	public void testUpdatePw() {
		String user_id = "abcd1234";
		String user_pw = "$2a$10$i1MgcQGfllmX0xvkLNKL4eboi4kPl1hN6S.obkaSEyF81p5NEzxJe";
		
		log.info(memberDao.updatePw(user_id, user_pw));
	}*/
	
	/* 핸드폰 번호 변경 
	@Test
	public void testUpdatePhone() {
		MemberVO member = new MemberVO();
		member.setUser_id("abcd1234");
		member.setPhone("01098765432");
		
		log.info(memberDao.updatePhone(member));
	}*/
	
	/* 비밀번호 확인 
	@Test
	public void testPwConfirm() {
		MemberVO member = new MemberVO();
		member.setUser_id("abcd1234");
		
		log.info(memberDao.pwConfirm(member));
	}*/
	
	
	/* 주소 변경 
	@Test
	public void testAddressModi() throws Exception{
		MemberVO member = new MemberVO();
		member.setUser_id("abcd123");
		member.setAddress("서울시 강서구");
		
		log.info(memberDao.updateAddress(member));
	}
	*/
	
	/* 회원 탈퇴 
	@Test
	public void testDeleteAccount() throws Exception{
		String user_id = "abcd12345";
		
		log.info(memberDao.deleteAccount(user_id));
	}*/

	/* 회원 닉네임 가져오기 
	@Test
	public void testGetUserName() {
		String user_id = "abcd1234";
		
		log.info(memberDao.getUserName(user_id));
	}*/

	/* 닉네임 변경 
	@Test
	public void testUpdateUserName(){
		MemberVO member = new MemberVO();
		member.setUser_name("멍멍이");
		member.setUser_id("abcd1234");
		
		log.info(memberDao.updateUserName(member));
	}
	
	@Test
	public void testGetUserInfo() {
		String refreshToken = "eyJhbGciOiJIUzI1NiJ9.eyJhdXRvTG9naW4iOmZhbHNlLCJqdGkiOiI2OWYyYTNiMS1mYzUwLTQ0ZTAtOTQ1YS1iNDdiOGY0MzIyNzQiLCJleHAiOjE3Mzc1OTI2Mjl9.FzOpO2xzc9Txt-kve1iFB87Uj6mLLhU2FznNZf-HmYc";
		MemberVO member = memberDao.getUserInfo(refreshToken);
		
		log.info(member.getUser_id());
		log.info(member.getUser_name());
		log.info(member.getRole());
				
				
	}*/
	
	
}
