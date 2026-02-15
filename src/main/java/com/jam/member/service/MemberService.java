package com.jam.member.service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
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

import com.jam.community.dto.CommunityDto;
import com.jam.fleaMarket.dto.FleaMarketDto;
import com.jam.global.exception.ConflictException;
import com.jam.global.exception.MailSendFailureException;
import com.jam.job.dto.JobDto;
import com.jam.member.dto.MemberDto;
import com.jam.member.mapper.MemberMapper;
import com.jam.studio.dto.StudioDto;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class MemberService {

	private final MemberMapper memberMapper;
	private final PasswordEncoder encoder;    
    private final RedisTemplate<String, String> stringRedisTemplate;
	private final JavaMailSender mailSender;
	
	// 회원가입
	@Transactional(rollbackFor = Exception.class)
	public void join(MemberDto member) {

        // 회원 정보 저장
        memberMapper.memberJoin(member);
        
        // 기본 권한 부여(ROLE_USER)
        int roleResult = memberMapper.assignDefaultRoleToMember(member.getUser_id());

        if (roleResult != 1) {
        	log.error("회원가입 중 기본 권한 부여 실패 (userId={})", member.getUser_id());;
        	throw new ConflictException("회원가입 처리 중 오류가 발생했습니다. 다시 시도해주세요.");
    	}
        
        // Redis에 닉네임 저장
        cacheUserName(member.getUser_id(), member.getUser_name());
	}
	
	// 아이디 중복확인
	public int idCheck(String userId){
		return memberMapper.idCheck(userId);
	}
	
	// 닉네임 중복확인
	public int nameCheck(String user_name){
		return memberMapper.nameCheck(user_name);
	}

	// 전화번호 중복확인
	public int phoneCheck(String phone){
		return memberMapper.phoneCheck(phone);
	}
	
	// 이메일 중복확인
	public int emailCheck(String email){
		return memberMapper.emailCheck(email);
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
	// FIXME: 이름이 너무 구리다
	public List<CommunityDto> comMyWrite(CommunityDto com) {
		return memberMapper.comMyWrite(com);
	}

	public List<FleaMarketDto> fleaMyWrite(FleaMarketDto flea) {
		return memberMapper.fleaMyWrite(flea);
	}
	
	public List<JobDto> jobMyWrite(JobDto job) {
		return memberMapper.jobMyWrite(job);
	}

	public List<StudioDto> roomMyWrite(StudioDto studio) {
		return memberMapper.roomMyWrite(studio);
	}
	
	// 마이페이지 작성글 페이징
	public int myComListCnt(CommunityDto com) {
		return memberMapper.myComListCnt(com);
	}

	public int myFleaListCnt(FleaMarketDto flea) {
		return memberMapper.myFleaListCnt(flea);
	}

	public int myJobListCnt(JobDto job) {
		return memberMapper.myJobListCnt(job);
	}

	public int myRoomListCnt(StudioDto studio) {
		return memberMapper.myRoomListCnt(studio);
	}

	// 아이디 찾기
	public String FindId(String email, String phone) {
		return memberMapper.findId(email, phone);
	}

	// 비밀번호 찾기 (사용자 정보 확인 후 임시 비밀번호 변경, 사용자 이메일로 전송)
	@Transactional(rollbackFor = Exception.class)
	public void updatePwAndSendEmail(String user_id, String email, String phone) {

		int count = memberMapper.countByUserIdEmailPhone(user_id, email, phone);
		if (count == 0) {
			return;
		} 
	    String tempPw = generateTempPassword();
	    String user_pw = encoder.encode(tempPw);

        // 임시 비밀번호로 비밀번호 변경
        memberMapper.updatePw(user_id, user_pw);
        
        int updated = memberMapper.updatePw(user_id, user_pw);
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
	public MemberDto socialLoginOrRegister(Map<String, Object> userInfo, String provider) {
	
        MemberDto user = new MemberDto();
        
        // 가입 여부 확인
        String userId = (String)userInfo.get("user_id");
        int result = memberMapper.findSocialUser(userId);

        // 회원 정보 없으면 회원가입
        if (result == 0) {
            String base = provider + "_" + userInfo.get("user_name");
            String user_name = base;
            int count = 1;
            
            while (memberMapper.nameCheck(user_name) == 1) { // 중복이면 숫자 붙임
                user_name = base + "_" + count;
                count++;
            }

            userInfo.put("user_name", user_name);
            
            String randomPw = UUID.randomUUID().toString();
            userInfo.put("user_pw", encoder.encode(randomPw));
            
            memberMapper.SocialRegister(userInfo);
            
            // 기본 권한 부여(ROLE_USER)
            int roleResult = memberMapper.assignDefaultRoleToMember(userId);

            if (roleResult != 1) {
            	log.error("회원가입 중 기본 권한 부여 실패 (userId={})", userId);;
            	throw new ConflictException("회원가입 처리 중 오류가 발생했습니다. 다시 시도해주세요.");
        	}
            
            user.setUser_name(user_name);

            // Redis에 닉네임 저장
            //cacheUserName(userId, user_name);
        }else {
        	user = memberMapper.findByUserInfo(userId);
        	
            log.info("소셜 로그인 처리 완료");
            
        }
        return user;
	}
	
	// 닉네임 변경
	public boolean updateUserName(MemberDto member) {
		
		try {
	        // Redis에 닉네임 저장
            cacheUserName(member.getUser_id(), member.getUser_name());

	         return memberMapper.updateUserName(member) == 1;
	    } catch (Exception e) {
	        log.error("닉네임 변경 중 오류 발생: ", e);
	        throw e;
	    }
	}

	
	// 전화번호 변경
	public boolean updatePhone(MemberDto member) {
		return memberMapper.updatePhone(member) == 1;
	}
	
	// 비밀번호 확인
	public String getPassword(MemberDto member) {
		
		return memberMapper.getPassword(member);
	}

	// 비밀번호 변경
	public int updatePw(String user_id, String user_pw) {
	
		return memberMapper.updatePw(user_id, user_pw);
	}
	
	// 주소 변경
	public int updateAddress(MemberDto member) {
		
		return memberMapper.updateAddress(member);
	}

	// 회원 닉네임 가져오기
	public String getUserName(String user_id) {
		return memberMapper.getUserName(user_id);
	}
	
	// 아이디 가져오기
	public String getUserId(String user_name) {
		return memberMapper.getUserId(user_name);
	}

	// refresh 토큰 저장
	public int addRefreshToken(String user_id, String refreshToken) {
		return memberMapper.addRefreshToken(user_id, refreshToken);
	}

	// refresh 토큰 삭제
	public int deleteRefreshToken(String user_id) {
		return memberMapper.deleteRefreshToken(user_id);
	}

	public String getRefreshToken(String user_id) {
		return memberMapper.getRefreshToken(user_id);
	}

	
	/**
	 * TODO: 현재는 닉네임만 반환하지만, 추후 프로필 이미지 등 확장 예정
	 * 
	 * @param String user_id
	 * @return MemberDto 닉네임 
	 **/
	public MemberDto getUserProfile(String user_id) {
		return memberMapper.getUserProfile(user_id);
	}

	public Authentication authenticateSocialUser(MemberDto user) {
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
	
	public Authentication authenticateUser(MemberDto user) {
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
	@Transactional
	public void deleteAccount(String user_id) {
		memberMapper.deleteAccount(user_id);
		
		String redisKey = "user:nickname:" + user_id;
		stringRedisTemplate.delete(redisKey);
	}
	
	// 카카오 탈퇴
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
	
	@Transactional
	public Authentication updateUserNameAndTokens(MemberDto user, boolean autoLogin, String loginType,
			HttpServletResponse response) {
		
		boolean isUpdated = updateUserName(user);
		
		if (!isUpdated) {
		    throw new IllegalStateException("닉네임 변경 실패");
		}
		
		Authentication authentication = authenticateUser(user);
		
		return authentication;
	}
	
	@Transactional
	public Authentication convertBusiness(String userId, String company_name, MemberDto user) {
		Map<String, String> param = Map.of(
				"user_id", userId,
				"company_name", company_name
			);
		
		memberMapper.updateCompanyName(param);
		memberMapper.insertCompanyRole(param);
		
		if (user.getRoles() == null) 
			user.setRoles(new ArrayList<>());
		
		if (!user.getRoles().contains("ROLE_COMPANY")) 
			user.getRoles().add("ROLE_COMPANY");
	
		Authentication authentication = authenticateUser(user);
		
		return authentication;
	}

	public String findUserIdByRefreshToken(String refreshToken) {
		return memberMapper.findUserIdByRefreshToken(refreshToken);
	}

	public MemberDto findByUserInfo(String userId) {
		return memberMapper.findByUserInfo(userId);
	}
	
	// NOTE: 채팅 관련 확장 대비 (현재 미사용)
	private void cacheUserName(String userId, String userName) {
		String key = "users:name:" + userId;
		stringRedisTemplate.opsForValue().set(key, userName);
	}
}
