package com.jam.client.member.service;

import java.security.SecureRandom;
import java.util.Date;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.jam.client.community.vo.CommunityVO;
import com.jam.client.fleaMarket.vo.FleaMarketVO;
import com.jam.client.job.vo.JobVO;
import com.jam.client.member.dao.MemberDAO;
import com.jam.client.member.vo.MemberVO;
import com.jam.client.roomRental.vo.RoomRentalVO;

import lombok.extern.log4j.Log4j;

@Service
@Log4j
public class MemberServiceImpl implements MemberService {

	@Autowired
	private MemberDAO memberDao;
	
	
	@Autowired
	private BCryptPasswordEncoder encoder;

	@Autowired
	private JavaMailSender mailSender;
	
	// 회원가입
	@Override
	public int join(MemberVO member) throws Exception  {
		
		return memberDao.memberJoin(member);
	}
	
	// 아이디 중복확인
	@Override
	public int idCheck(String userId) throws Exception {
		
		return memberDao.idCheck(userId);
	}
	
	// 닉네임 중복확인
	@Override
	public int nameCheck(String user_name) throws Exception {
		
		return memberDao.nameCheck(user_name);
	}

	// 전화번호 중복확인
	@Override
	public int phoneCheck(String phone) throws Exception {
		return memberDao.phoneCheck(phone);
	}
	
	// 이메일 중복확인
	@Override
	public int emailCheck(String email) throws Exception{
		return memberDao.emailCheck(email);
	}


	// 마이페이지 작성글
	@Override
	public List<CommunityVO> comMyWrite(CommunityVO com_vo) {
		return memberDao.comMyWrite(com_vo);
	}

	@Override
	public List<FleaMarketVO> fleaMyWrite(FleaMarketVO flea_vo) {
		return memberDao.fleaMyWrite(flea_vo);
	}
	
	@Override
	public List<JobVO> jobMyWrite(JobVO jov_vo) {
		return memberDao.jobMyWrite(jov_vo);
	}

	@Override
	public List<RoomRentalVO> roomMyWrite(RoomRentalVO room_vo) {
		return memberDao.roomMyWrite(room_vo);
	}
	
	// 마이페이지 작성글 페이징
	@Override
	public int myComListCnt(CommunityVO com_vo) {
		return memberDao.myComListCnt(com_vo);
	}

	@Override
	public int myFleaListCnt(FleaMarketVO flea_vo) {
		return memberDao.myFleaListCnt(flea_vo);
	}

	@Override
	public int myJobListCnt(JobVO job_vo) {
		return memberDao.myJobListCnt(job_vo);
	}

	@Override
	public int myRoomListCnt(RoomRentalVO room_vo) {
		return memberDao.myRoomListCnt(room_vo);
	}


	
	// 마이페이지 - 회원 정보 페이지
	@Override
	public MemberVO account(String user_id) {
		return memberDao.account(user_id);
	}
	
	// 아이디 찾기
	@Override
	public MemberVO FindId(String email, String phone) {
		return memberDao.findId(email, phone);
	}

	// 비밀번호 찾기
	@Override
	public int FindPw(String user_id, String email, String phone) {
		
		return memberDao.findPw(user_id, email, phone);
	}
	
	@Override
	public ResponseEntity<String> updatePwAndSendEmail(String user_id, String email) {
	    String tempPw = getTempPassword();
	    String user_pw = encoder.encode(tempPw);

	    try {
	        // 이메일 전송
	        sendEmail(email, tempPw);

	        // 임시 비밀번호로 비밀번호 변경
	        memberDao.updatePw(user_id, user_pw);

	        return new ResponseEntity<>("Password updated successfully.", HttpStatus.OK);
	    } catch (MessagingException e) {
	        log.error("Failed to send email.", e);
	        return new ResponseEntity<>("Failed to send email.", HttpStatus.INTERNAL_SERVER_ERROR);
	    } catch (Exception e) {
	        log.error("Failed to update password.", e);
	        return new ResponseEntity<>("Failed to update password.", HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}
	

	// 메일 전송
	private void sendEmail(String email, String tempPw) throws MessagingException {
	    String setFrom = "ar971004@naver.com";
	    String title = "JAM 임시 비밀번호 입니다.";
	
	    String content = "JAM에서 발송된 메일입니다.<br/>임시 비밀번호를 이용하여 사이트에 접속하셔서 비밀번호를 변경하세요.<br/>";
	    content += "<span style='color: red;'>" + tempPw + "</span><br/>";
	
	    MimeMessage message = mailSender.createMimeMessage();
	    MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
	
	    helper.setFrom(setFrom);
	    helper.setTo(email);
	    helper.setSubject(title);
	
	    content = content.replace("\n", "<br/>");
	    helper.setText(content, true);
	
	    mailSender.send(message);
	}

	
	//임시 비밀번호 발급
    public String getTempPassword(){
    	char[] charSet = new char[] {
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
                'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
                '!', '@', '#', '$', '%', '^', '&' };
 
        StringBuffer sb = new StringBuffer();
        SecureRandom sr = new SecureRandom();
        sr.setSeed(new Date().getTime());
 
        int idx = 0;
        int len = charSet.length;
        for (int i=0; i<10; i++) {
            idx = sr.nextInt(len);    // 강력한 난수를 발생시키기 위해 SecureRandom을 사용한다.
            sb.append(charSet[idx]);
        }
 
        return sb.toString();
    }

	
	// 소셜 회원가입 여부 확인
	@Override
	public int socialLoginOrRegister(MemberVO member) {
	
		if(member.getUser_id() == null || member.getEmail() == null || member.getUser_name() == null || member.getPhone() == null) 
			return 0;
		
		//member.setUser_pw(encoder.encode("naverLoginPassword"));
		
		log.info(member);
		try {
			
			// 가입 여부 확인
			int result = memberDao.findSocialUser(member);
			
			// 회원 정보 없을 때
			if(result == 0) {
				// 네이버 사용자 회원가입
				memberDao.SocialRegister(member);
				
				result = memberDao.findSocialUser(member);
				
				return result;
			}
			
			return result;
		}catch(Exception e) {
			log.error(e.getMessage());
		}
		return 0;
	}
	
	
	// 전화번호 변경
	@Override
	public int updatePhone(MemberVO m_vo) {
		return memberDao.updatePhone(m_vo);
	}
	
	// 비밀번호 확인
	@Override
	public String pwConfirm(MemberVO m_vo) {
		
		return memberDao.pwConfirm(m_vo);
	}

	// 비밀번호 변경
	@Override
	public int updatePw(String user_id, String user_pw) {
	
		return memberDao.updatePw(user_id, user_pw);
	}
	
	// 주소 변경
	@Override
	public int updateAddress(MemberVO m_vo) {
		
		return memberDao.updateAddress(m_vo);
	}

	// 회원 탈퇴
	@Override
	public void withDraw(String user_id) {
		memberDao.withDraw(user_id);
		
	}

	// 회원 닉네임 가져오기
	@Override
	public String getUserName(String user_id) {
		return memberDao.getUserName(user_id);
	}

	// refresh 토큰 저장
	@Override
	public int addRefreshToken(String user_id, String refreshToken) {
		return memberDao.addRefreshToken(user_id, refreshToken);
	}

	// refresh 토큰 삭제
	@Override
	public int deleteRefreshToken(String user_id) {
		return memberDao.deleteRefreshToken(user_id);
	}

	@Override
	public String getRefreshToken(String user_id) {
		return memberDao.getRefreshToken(user_id);
	}

	
}
