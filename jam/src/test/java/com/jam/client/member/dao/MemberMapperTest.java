package com.jam.client.member.dao;


import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.jam.client.community.vo.CommunityVO;
import com.jam.client.fleaMarket.vo.FleaMarketVO;
import com.jam.client.job.vo.JobVO;
import com.jam.client.member.vo.MemberVO;
import com.jam.client.roomRental.vo.RoomRentalVO;

import lombok.Setter;
import lombok.extern.log4j.Log4j;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/main/webapp/WEB-INF/spring/root-context.xml")
@Log4j
public class MemberMapperTest {

	@Setter(onMethod_=@Autowired)
	private MemberDAO memberDao;
	
	/* 회원가입 
	@Test
	public void testMemberInsert() {
		MemberVO member = new MemberVO();
		member.setUser_id("abcd123");
		member.setUser_pw("abcd123");
		member.setUser_name("김철수");
		member.setPhone("01012345678");
		member.setAddress("서울시 강남구 강남동 123번지");
		
		int count = memberDao.memberJoin(member);
		log.info("입력된 행의 수 " + count);
	} */
	
	
	/* 아이디 중복검사 
	@Test
	public void testMemberIdChk() throws Exception{
		String id = "abcde132";	// 존재하는 아이디
		String id2 = "test123";	// 존재하지 않는 아이디
		
		log.info(memberDao.idCheck(id));
		log.info(memberDao.idCheck(id2));
	}*/
	
	
	/* 닉네임 중복검사
	@Test
	public void testMemberNameChk() throws Exception{
		String name = "김철수";	// 존재하는 닉네임
		String name2 = "김영희";	// 존재하지 않는 닉네임
		
		log.info(memberDao.nameCheck(name));
		log.info(memberDao.nameCheck(name2));
	}
	 */
	
	/* 비밀번호 중복검사 
	@Test
	public void testMemberPhoneChk() throws Exception{
		String phone = "01012345678"; // 존재하는 전화번호
		String phone2 = "01000001111"; // 존재하지 않는 전화번호
		
		log.info(memberDao.phoneCheck(phone));
		log.info(memberDao.phoneCheck(phone2));
	}
	*/
	
	/* 로그인 
	@Test
	public void testlogin() throws Exception{
		MemberVO member = new MemberVO();
		member.setUser_id("moong123");
		
		log.info(memberDao.login(member));
	}
	*/
	
	/* 전화번호 변경 
	@Test
	public void testPhoneModi() throws Exception{
		MemberVO member = new MemberVO();
		member.setUser_id("abcd123");
		member.setPhone("01098764512");
		
		log.info(memberDao.phoneModi(member));
	}*/
	
	/* 비밀번호 확인 
	@Test
	public void testPwConfirm() throws Exception{
		MemberVO member = new MemberVO();
		member.setUser_id("abcd123");
		member.setUser_pw("abcd123");
		
		log.info(memberDao.pwConfirm(member));
	}*/
	
	/* 비밀번호 변경
	@Test
	public void testPwModi() throws Exception{
		MemberVO member = new MemberVO();
		member.setUser_id("abcd123");
		member.setUser_pw("abcd123");
		
		log.info(memberDao.pwModi(member));
	}*/
	
	/* 주소 변경 
	@Test
	public void testAddressModi() throws Exception{
		MemberVO member = new MemberVO();
		member.setUser_id("abcd123");
		member.setAddress("서울시 강서구");
		
		log.info(memberDao.addressModi(member));
	}
	*/
	
	/* 회원 탈퇴 
	@Test
	public void testWithDraw() throws Exception{
		String user_id = "dkdkdk123";
		
		log.info(memberDao.withDraw(member));
	}
	*/
	
	/* 마이페이지 회원이 쓴 글
	@Test
	public void testMyWrite(){
		MemberVO member = new MemberVO();
		member.setUser_id("abcd123");
		
		log.info(memberDao.comMyWrite(member));
		log.info(memberDao.fleaMyWrite(member));
		log.info(memberDao.jobMyWrite(member));
		log.info(memberDao.roomMyWrite(member));
	} 
	*/
	
	/* 마이페이지 - 회원 정보 페이지
	@Test
	public void testAccount() {
		MemberVO member = new MemberVO();
		member.setUser_id("abcd123");
		
		log.info(memberDao.account(member));
	}*/

	/* 마이페이지 - 회원 정보 수정
	@Test
	public void testMemberUpdate() {
		MemberVO member = new MemberVO();
		member.setUser_id("abcd123");
		member.setUser_pw("abce123");
		member.setUser_name("김철수");
		member.setAddress("서울시 강서구");
		member.setPhone("01098641154");
		
		
		log.info(memberDao.memberUpdate(member));
	}*/

	/* 아이디 찾기
	@Test
	public void findId() {
		String user_name ="이뭉이";
		String phone = "01000000000";
		
		log.info(memberDao.findId(user_name, phone));
	}*/

	/* 비밀번호 찾기
	@Test
	public void findPw() {
		String user_id = "moong123";
		String user_name = "이뭉이";
		String phone = "01000000000";
		
		log.info(memberDao.findPw(user_id, user_name, phone));
	}*/
	

	
}
