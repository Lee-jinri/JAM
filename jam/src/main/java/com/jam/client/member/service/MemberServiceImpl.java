package com.jam.client.member.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jam.client.community.vo.CommunityVO;
import com.jam.client.fleaMarket.vo.FleaMarketVO;
import com.jam.client.job.vo.JobVO;
import com.jam.client.member.dao.MemberDAO;
import com.jam.client.member.vo.MemberVO;
import com.jam.client.roomRental.vo.RoomRentalVO;

@Service
public class MemberServiceImpl implements MemberService {

	@Autowired
	private MemberDAO memberDao;
	
	@Autowired(required = true)
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

	// 로그인
	@Override
	public MemberVO login(MemberVO member) {
		
		return memberDao.login(member);
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
	public MemberVO account(MemberVO member) {
		return memberDao.account(member);
	}

	// 마이페이지 - 회원 정보 수정
	@Override
	public int memberUpdate(MemberVO member) {
		return memberDao.memberUpdate(member);
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
	public int UpdatePw(String user_id, String email) {
		
		String tempPw = getTempPassword();
		String user_pw = encoder.encode(tempPw);
		
		
		/* 이메일 보내기 */
		String setFrom = "ar971004@naver.com";
		String title = "JAM 임시 비밀번호 입니다.";
		
		String content = "JAM에서 발송된 메일입니다.\n 임시 비밀번호를 이용하여 사이트에 접속하셔서 비밀번호를 변경하세요.\n";
		try {

			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true,"UTF-8");

			//메일 보관함에 저장
			helper.setFrom(setFrom);
			helper.setTo(email);
			helper.setSubject(title);
			
			content = content.replace("\n", "<br/>");
			content += "<font color=red>"+ tempPw + "</font><br>";
			helper.setText(content, true);
			
			// 메일 전송
			mailSender.send(message); 
		} catch (MessagingException e) {
			e.printStackTrace();
		}
		
		return memberDao.updatePw(user_id, user_pw);
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

    // 카카오 토큰 받기
    @Override
    public String getAccessToken(String code) {
		String access_Token = "";
		String refresh_Token = "";
		String reqURL = "https://kauth.kakao.com/oauth/token";
		
		try {
			// url 객체 생성
			URL url = new URL(reqURL);
            
            // url에서 url connection 객체 얻기
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			
			// setRequestMethod : HTTP 메소드 설정, 기본값은 GET 
			conn.setRequestMethod("POST");
			// setDoOutput : urlconnection이 서버에 데이터를 보낼 수 있는지 여부 설정, 기본값 false 
			conn.setDoOutput(true);
			
			// POST 요청에 필요로 요구하는 파라미터 스트림을 통해 전송
            // POST로 보낼 Body 작성
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
			
			StringBuilder sb = new StringBuilder();
			sb.append("grant_type=authorization_code");
			sb.append("&client_id="); //본인이 발급받은 key
			sb.append("&redirect_uri=http://localhost:8080/member/kakao_login"); // 본인이 설정한 주소
			sb.append("&code=" + code);
			
			// 버퍼에 있는 값 전부 출력
			bw.write(sb.toString());
			
			// 남아있는 데이터를 모두 출력
			bw.flush();
            
			// 결과 코드가 200이라면 성공
			int responseCode = conn.getResponseCode();
			System.out.println("responseCode : " + responseCode);
            
			// 요청을 통해 얻은 JSON 타입의 Response 메세지 읽어오기
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line = "";
			String result = "";
            
			while ((line = br.readLine()) != null) {
				result += line;
			}
			System.out.println("response body : " + result);
            
			// Gson 라이브러리에 포함된 클래스로 JSON파싱 객체 생성
			JsonParser parser = new JsonParser();
			JsonElement element = parser.parse(result);
            
			access_Token = element.getAsJsonObject().get("access_token").getAsString();
			refresh_Token = element.getAsJsonObject().get("refresh_token").getAsString();
            
			System.out.println("access_token : " + access_Token);
			System.out.println("refresh_token : " + refresh_Token);
            
			br.close();
			bw.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return access_Token;
	}

    // 카카오 토큰으로 회원 정보 받기
	@Override
	public MemberVO getUserInfo(String access_Token) {
		
		// 요청하는 클라이언트마다 가진 정보가 다를 수 있기에 HashMap타입으로 선언
		HashMap<String, Object> userInfo = new HashMap<String, Object>();
		String reqURL = "https://kapi.kakao.com/v2/user/me";
		
		try {
			URL url = new URL(reqURL);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Authorization", "Bearer " + access_Token);
				
			int responseCode = conn.getResponseCode();
			System.out.println("responseCode : " + responseCode);
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line = "";
			String result = "";
			
			while ((line = br.readLine()) != null) {
				result += line;
			}
			System.out.println("response body : " + result);
				
			JsonParser parser = new JsonParser();
			JsonElement element = parser.parse(result);
			JsonObject properties = element.getAsJsonObject().get("properties").getAsJsonObject();
			JsonObject kakao_account = element.getAsJsonObject().get("kakao_account").getAsJsonObject();
			String nickname = properties.getAsJsonObject().get("nickname").getAsString();
			String email = kakao_account.getAsJsonObject().get("email").getAsString();
			
			String[] id = email.split("@");
			
			userInfo.put("user_id", id[0]);
			userInfo.put("user_name", nickname);
			userInfo.put("email", email);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// 회원 정보가 있는지 확인
		MemberVO result = memberDao.findKakao(userInfo);
			
		// 회원 정보 없을 때
		if(result == null) {
			memberDao.kakaoInsert(userInfo);
			return memberDao.findKakao(userInfo);
		} else {
			return result; // 회원 정보 있으면 회원 정보 리턴
		}
	}
	
	// 네이버 토큰 받기
	@Override
	public String getNaverToken(String code) {
		String access_Token = "";
		String refresh_Token = "";
		String reqURL = "https://nid.naver.com/oauth2.0/token"; // 접근토큰 발급 요청 url
		
		try {
			// url 객체 생성
			URL url = new URL(reqURL);
			
            // url에서 url connection 객체 얻기
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			
			// setRequestMethod : HTTP 메소드 설정, 기본값은 GET 
			conn.setRequestMethod("POST");
			// setDoOutput : url connection이 서버에 데이터를 보낼 수 있는지 여부 설정, 기본값 false 
			conn.setDoOutput(true);
			
			// POST 요청에 필요로 요구하는 파라미터 스트림을 통해 전송
            // POST로 보낼 Body 작성
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
			
			StringBuilder sb = new StringBuilder();
			sb.append("grant_type=authorization_code");
			sb.append("&client_id="); // Client ID
			sb.append("&client_secret="); // Client Secret
			sb.append("&code=" + code);
			
			// 버퍼에 있는 값 전부 출력
			bw.write(sb.toString());
			
			// 남아있는 데이터를 모두 출력
			bw.flush();
            
			// 결과 코드가 200이라면 성공
			int responseCode = conn.getResponseCode();
			System.out.println("responseCode : " + responseCode);
            
			// 요청을 통해 얻은 JSON 타입의 Response 메세지 읽어오기
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line = "";
			String result = "";
            
			while ((line = br.readLine()) != null) {
				result += line;
			}
			System.out.println("response body : " + result);
            
			// Gson 라이브러리에 포함된 클래스로 JSON파싱 객체 생성
			JsonParser parser = new JsonParser();
			JsonElement element = parser.parse(result);
            
			access_Token = element.getAsJsonObject().get("access_token").getAsString();
			refresh_Token = element.getAsJsonObject().get("refresh_token").getAsString();
            
			System.out.println("access_token : " + access_Token);
			System.out.println("refresh_token : " + refresh_Token);
			
			br.close();
			bw.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return access_Token;
	}
	
	// 네이버 회원정보 받기 
	@Override
	public MemberVO getNaverInfo(String access_Token) {
		
		HashMap<String, Object> userInfo = new HashMap<String, Object>();
		String reqURL = "https://openapi.naver.com/v1/nid/me";
		
		try {
			URL url = new URL(reqURL);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Authorization", "Bearer " + access_Token);
				
			int responseCode = conn.getResponseCode();
			System.out.println("responseCode : " + responseCode);
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line = "";
			String result = "";
			
			while ((line = br.readLine()) != null) {
				result += line;
			}
			System.out.println("response body : " + result);
			
			JsonParser parser = new JsonParser();
			JsonElement element = parser.parse(result);
			JsonObject response = element.getAsJsonObject().get("response").getAsJsonObject();
			
			String user_id = response.getAsJsonObject().get("id").getAsString();
			String name = response.getAsJsonObject().get("name").getAsString();
			String email = response.getAsJsonObject().get("email").getAsString();
			
			userInfo.put("user_id", user_id);
			userInfo.put("user_name", name);
			userInfo.put("email", email);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// 회원 정보가 있는지 확인
		MemberVO result = memberDao.findNaver(userInfo);
			
		// 회원 정보 없을 때
		if(result == null) {
			memberDao.naverInsert(userInfo);
			return memberDao.findNaver(userInfo);
		} else {
			return result; // 회원 정보 있으면 회원 정보 리턴
		}
	}
	
	
	// 전화번호 변경
	@Override
	public int phoneModi(MemberVO m_vo) {
		return memberDao.phoneModi(m_vo);
	}
	
	// 비밀번호 확인
	@Override
	public String pwConfirm(MemberVO m_vo) {
		
		return memberDao.pwConfirm(m_vo);
	}

	// 비밀번호 변경
	@Override
	public int pwModi(MemberVO m_vo) {
	
		return memberDao.pwModi(m_vo);
	}
	
	// 주소 변경
	@Override
	public int addressModi(MemberVO m_vo) {
		
		return memberDao.addressModi(m_vo);
	}

	// 회원 탈퇴
	@Override
	public int withDraw(String user_id) {
		return memberDao.withDraw(user_id);
	}

	

	

	

	

	


	
	
	
	
	

	


	

	



}
