package com.jam.client.member.service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.jam.client.community.vo.CommunityVO;
import com.jam.client.fleaMarket.vo.FleaMarketVO;
import com.jam.client.job.vo.JobVO;
import com.jam.client.member.dao.MemberDAO;
import com.jam.client.member.vo.MemberVO;
import com.jam.client.roomRental.vo.RoomRentalVO;
import com.jam.global.exception.ConflictException;
import com.jam.global.exception.MailSendFailureException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

	private final MemberDAO memberDao;
	private final PasswordEncoder encoder;    
    private final RedisTemplate<String, String> stringRedisTemplate;
	private final JavaMailSender mailSender;
	
	// 회원가입
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void join(MemberVO member) {

        // 회원 정보 저장
        memberDao.memberJoin(member);
        
        // 기본 권한 부여(ROLE_USER)
        int roleResult = memberDao.assignDefaultRoleToMember(member.getUser_id());

        if (roleResult != 1) {
        	log.error("회원가입 중 기본 권한 부여 실패 (userId={})", member.getUser_id());;
        	throw new ConflictException("회원가입 처리 중 오류가 발생했습니다. 다시 시도해주세요.");
    	}
        
        // Redis에 닉네임 저장
        String key = "users:name:" + member.getUser_id();
        stringRedisTemplate.opsForValue().set(key, member.getUser_name());
	}
	
	// 아이디 중복확인
	@Override
	public int idCheck(String userId){
		
		return memberDao.idCheck(userId);
	}
	
	// 닉네임 중복확인
	@Override
	public int nameCheck(String user_name){
		
		return memberDao.nameCheck(user_name);
	}

	// 전화번호 중복확인
	@Override
	public int phoneCheck(String phone){
		return memberDao.phoneCheck(phone);
	}
	
	// 이메일 중복확인
	@Override
	public int emailCheck(String email){
		return memberDao.emailCheck(email);
	}
	
	

	public String getAccessToken(HttpServletRequest request) {

		String accessToken ="";
		
        Cookie[] cookies = request.getCookies();
        
		if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("Authorization")) {  // 원하는 쿠키 이름 확인
                	accessToken = cookie.getValue();  // 쿠키의 값 가져오기

                }
            }
        }
		
		return accessToken;
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

	// 아이디 찾기
	@Override
	public String FindId(String email, String phone) {
		return memberDao.findId(email, phone);
	}

	// 비밀번호 찾기 (사용자 정보 확인 후 임시 비밀번호 변경, 사용자 이메일로 전송)
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void updatePwAndSendEmail(String user_id, String email, String phone) {

		int count = memberDao.countByUserIdEmailPhone(user_id, email, phone);
		if (count == 0) {
			return;
		} 
	    String tempPw = generateTempPassword();
	    String user_pw = encoder.encode(tempPw);

        // 임시 비밀번호로 비밀번호 변경
        memberDao.updatePw(user_id, user_pw);
        
        int updated = memberDao.updatePw(user_id, user_pw);
        if (updated != 1) {
        	throw new IllegalStateException("비밀번호 변경에 실패했습니다.");
        }
        
	    try {
	        // 이메일 전송
	        sendEmail(email, tempPw);

	    } catch (MessagingException e) {
	        log.error("메일 전송 실패.", e);
	    	throw new MailSendFailureException("메일 전송에 실패했습니다. 잠시 후 다시 시도해주세요.");
	    } 
	}
	
    @Value("${spring.mail.username}")
    private String mailUsername;
    
	// 메일 전송
	private void sendEmail(String email, String tempPw) throws MessagingException {
	    String setFrom = mailUsername;
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
    public String generateTempPassword(){
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
	public MemberVO socialLoginOrRegister(Map<String, Object> userInfo, String provider) {
	
		try {
	        MemberVO user = new MemberVO();
	        
	        // 가입 여부 확인
	        String userId = (String)userInfo.get("user_id");
	        int result = memberDao.findSocialUser(userId);

	        // 회원 정보 없으면 회원가입
	        if (result == 0) {
	            String base = provider + "_" + userInfo.get("user_name");
	            String user_name = base;
	            int count = 1;
	            
	            while (memberDao.nameCheck(user_name) == 1) { // 중복이면 숫자 붙임
	                user_name = base + "_" + count;
	                count++;
	            }

	            userInfo.put("user_name", user_name);
	            memberDao.SocialRegister(userInfo);
	            user.setUser_name(user_name);
	        }else user = memberDao.findByUserInfo(userId);
	        
	        log.info("소셜 로그인 처리 완료");
	        
	        return user;
	    } catch (Exception e) {
	        log.error("소셜 로그인 처리 중 오류 발생: " + e.getMessage());
	    }
		return null;
	}
	
	// 닉네임 변경
	@Override
	public boolean updateUserName(MemberVO member) {
		
		try {
	        // Redis에 닉네임 저장
	        String key = "users:name:" + member.getUser_id();
	        stringRedisTemplate.opsForValue().set(key, member.getUser_name());

	         return memberDao.updateUserName(member) == 1;
	    } catch (Exception e) {
	        log.error("닉네임 변경 중 오류 발생: ", e);
	        throw e;
	    }
	}

	
	// 전화번호 변경
	@Override
	public boolean updatePhone(MemberVO m_vo) {
		return memberDao.updatePhone(m_vo) == 1;
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

	

	// 회원 닉네임 가져오기
	@Override
	public String getUserName(String user_id) {
		return memberDao.getUserName(user_id);
	}
	
	
	// 아이디 가져오기
	@Override
	public String getUserId(String user_name) {
		return memberDao.getUserId(user_name);
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

	
	/**
	 * TODO: 현재는 닉네임만 반환하지만, 추후 프로필 이미지 등 확장 예정
	 * 
	 * @param String user_id
	 * @return MemberVO 닉네임 
	 **/
	@Override
	public MemberVO getUserProfile(String user_id) {
		return memberDao.getUserProfile(user_id);
	}

	
	public Authentication authenticateSocialUser(MemberVO user) {
		try {
			Authentication authentication = new UsernamePasswordAuthenticationToken(
					user,
				    null,
				    user.getAuthorities()
				);
			
			return authentication;
			
		} catch (AuthenticationException e) {
	        log.error("인증 실패", e);
	        throw e; 
	    } catch (Exception e) {
	        log.error("로그인 처리 중 오류 발생", e);
	        throw new RuntimeException("로그인 처리 중 오류 발생", e); // 커스터마이징 가능
	    }
	}
	
	public Authentication authenticateUser(MemberVO user) {
		try {
			Authentication authentication = new UsernamePasswordAuthenticationToken(
					user,
					null,
					user.getAuthorities()
				);
			
			SecurityContextHolder.getContext().setAuthentication(authentication);
			
			return authentication;
		}catch(AuthenticationException e) {
			log.error("인증 실패", e);
			throw e;
		}catch(Exception e) {
			log.error("Authentication 발급 중 오류 발생", e);
			throw new RuntimeException("Authentication 발급 중 오류 발생", e);
		}
	}

	// 회원 탈퇴
	@Override
	public void deleteAccount(String user_id) {
		memberDao.deleteAccount(user_id);
	}
	
	// 카카오 탈퇴
	@Override
	public void kakaoDeleteAccount(String kakaoAccessToken) {
		try {
	        String unlinkUrl = "https://kapi.kakao.com/v1/user/unlink";

	        HttpHeaders headers = new HttpHeaders();
	        headers.set("Authorization", "Bearer " + kakaoAccessToken);

	        HttpEntity<Void> httpEntity = new HttpEntity<>(headers);

	        RestTemplate restTemplate = new RestTemplate();
	        ResponseEntity<String> res = restTemplate.postForEntity(unlinkUrl, httpEntity, String.class);

	        if (!res.getStatusCode().is2xxSuccessful()) {
	            log.warn("카카오 탈퇴 실패 - 응답 코드: " + res.getStatusCodeValue());
	        }
	    } catch (Exception e) {
	        log.error("카카오 탈퇴 실패: " + e.getMessage());
	    }
	}
	
	@Value("${oauth.naver.clientId}")
	private String naver_clientId;

	@Value("${oauth.naver.clientSecret}")
	private String naver_client_secret;
	
	// 네이버 탈퇴
	@Override
	public void naverDeleteAccount(String naverAccessToken) {
		try {
	    	String unlinkUrl = "https://nid.naver.com/oauth2.0/token";

	    	MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
	    	params.add("grant_type", "delete");
	    	params.add("client_id", naver_clientId); 
	    	params.add("client_secret", naver_client_secret);
	    	params.add("access_token", naverAccessToken); 

	    	HttpHeaders headers = new HttpHeaders();
	    	headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

	    	HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(params, headers);

	    	// 요청 전송
	    	RestTemplate restTemplate = new RestTemplate();
	    	ResponseEntity<String> res = restTemplate.postForEntity(unlinkUrl, httpEntity, String.class);

	        if (!res.getStatusCode().is2xxSuccessful()) {
	            log.warn("네이버 연결 끊기 실패 - 응답 코드: " + res.getStatusCodeValue());
	        }
	    } catch (Exception e) {
	        log.error("네이버 연결 끊기 실패: " + e.getMessage());
	    }
	}

	@Override
	@Transactional
	public Authentication updateUserNameAndTokens(MemberVO user, boolean autoLogin, String loginType,
			HttpServletResponse response) {
		
		boolean isUpdated = updateUserName(user);
		
		if (!isUpdated) {
		    throw new IllegalStateException("닉네임 변경 실패");
		}
		
		Authentication authentication = authenticateUser(user);
		
		return authentication;
	}

	@Override
	@Transactional
	public Authentication convertBusiness(String userId, String company_name, MemberVO user) {
		Map<String, String> param = Map.of(
				"user_id", userId,
				"company_name", company_name
			);
		
		memberDao.updateCompanyName(param);
		memberDao.insertCompanyRole(param);
		
		if (user.getRoles() == null) 
			user.setRoles(new ArrayList<>());
		
		if (!user.getRoles().contains("ROLE_COMPANY")) 
			user.getRoles().add("ROLE_COMPANY");
	
		Authentication authentication = authenticateUser(user);
		
		return authentication;
	}

	@Override
	public String findUserIdByRefreshToken(String refreshToken) {
		return memberDao.findUserIdByRefreshToken(refreshToken);
	}

	@Override
	public MemberVO findByUserInfo(String userId) {
		return memberDao.findByUserInfo(userId);
	}
}
