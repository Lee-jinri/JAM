package com.jam.mypage.controller;

import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jam.global.exception.UnauthorizedException;
import com.jam.global.jwt.JwtService;
import com.jam.member.dto.MemberDto;
import com.jam.mypage.service.MypageService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping(value="/api/mypage")
@RequiredArgsConstructor
@Slf4j
public class MypageRestController {

    private final MypageService mypageService;
	private final JwtService jwtService;
	private final StringRedisTemplate redisTemplate;

	@PostMapping(value = "/favorite/{postId}", produces = "application/json")
    public ResponseEntity<String> addFavorite(
    		@PathVariable Long postId, 
    		@RequestParam String boardType, 
    		@AuthenticationPrincipal MemberDto user) {
        
		if(user == null || user.getUser_id() == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body("로그인이 필요한 서비스 입니다. 로그인 하시겠습니까?");
		}
		boolean added = mypageService.addFavorite(user.getUser_id(), boardType, postId);
        
		if (!added) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("즐겨찾기 실패: 해당 게시글이 존재하지 않음");
        }
		
		return ResponseEntity.ok("즐겨찾기 추가 완료");
    }

	@DeleteMapping(value = "/favorite/{postId}", produces = "application/json")
	public ResponseEntity<String> deleteFavorite(@PathVariable Long postId, @RequestParam String boardType, @AuthenticationPrincipal MemberDto user){
		
		if(user == null || user.getUser_id() == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body("로그인이 필요한 서비스 입니다. 로그인 하시겠습니까?");
		}
		boolean deleted = mypageService.deleteFavorite(user.getUser_id(), boardType, postId);
		
		if (!deleted) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("즐겨찾기 취소 실패: 해당 게시글이 존재하지 않음");
        }
		
		return ResponseEntity.ok("즐겨찾기 완료");
	}
	
	/**
	 * 마이페이지 - 사용자 정보 조회
	 *
	 * 세션 또는 소셜 로그인 여부를 통해 사용자의 인증 상태를 확인한 후,
	 * 인증이 완료된 사용자에 한해 사용자 정보를 조회하여 반환합니다.
	 * 인증되지 않은 경우 401(UNAUTHORIZED) 상태 코드를 반환합니다.
	 *
	 * @param request 클라이언트 요청 객체 (쿠키 접근용)
	 * @return 사용자 정보(MemberDto) 또는 HTTP 상태 코드
	 */
	@PreAuthorize("isAuthenticated()")
	@GetMapping(value = "/account", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<MemberDto> getAccount(
			@AuthenticationPrincipal MemberDto user, 
			HttpServletRequest request, 
			HttpServletResponse response){

		if(user == null || user.getUser_id() == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		
		boolean verifyStatus = isVerified(request, response, user.getUser_id());
		
		if(verifyStatus) {
			MemberDto account = mypageService.account(user.getUser_id());
			return ResponseEntity.ok().body(account);
		}
		
		return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
	}
	
	/**
	 * 마이페이지 인증 상태(verifyStatus)를 확인합니다.
	 * 
	 * 레디스에 인증 플래그가 존재하고, 유효 시간(10분)이 만료되지 않은 경우 true를 반환합니다.
	 * 인증되지 않았거나 인증 플래그가 만료되면 false를 반환합니다.
	 *
	 * @param user 현재 로그인한 사용자 정보
     * @return ResponseEntity<Boolean> 인증 성공 여부 (true/false)
     * @throws UnauthorizedException 로그인이 되어 있지 않은 경우
	 */
	@GetMapping(value = "/account/verify-status")
	public ResponseEntity<Boolean> getVerifyStatus(
			HttpServletRequest request, 
			HttpServletResponse response,
			@AuthenticationPrincipal MemberDto user) {
				
		if (user == null) {
			throw new UnauthorizedException("로그인이 필요한 서비스 입니다.");
	    }
		
		boolean verifyStatus = isVerified(request, response, user.getUser_id());
		
		return ResponseEntity.ok(verifyStatus); 
	}
	
	/**
	 * 비밀번호 인증 성공 시 호출되는 메서드
	 * 
	 * 프론트엔드에서 비밀번호 확인 이후 이 엔드포인트를 호출하여
	 * 계정 정보 접근 권한을 부여합니다. (레디스 값을 true로 설정, 만료시간 10분)
	 */
	@GetMapping("/verify-status/set")
	public ResponseEntity<String> setVerifyStatusRedis(@AuthenticationPrincipal MemberDto user) {
		if(user == null || user.getUser_id() == null) throw new UnauthorizedException("로그인이 필요한 서비스 입니다.");
		
		String key = "auth:mypage:" + user.getUser_id();
		
		redisTemplate.opsForValue().set(key, "true", Duration.ofMinutes(10));
		return ResponseEntity.ok(null);
	}

	/**
     * 사용자의 마이페이지 인증 상태을 검증합니다.
     * 1. 소셜 로그인(naver, kakao) 사용자는 추가 검증 없이 즉시 인증 처리 및 상태를 "true"로 기록합니다.
     * 2. 일반 로그인 사용자는 Redis에 저장된 인증 플래그(auth:mypage:{userId})가 "true"인지 확인합니다.
     *
     * @param request  HttpServletRequest (쿠키에서 AccessToken을 추출) 및 jwt 토큰 무효화를 위한 요청 객체
     * @param response 유효하지 않은 인증 정보를 삭제하기 위한 응답 객체
     * @param userId   사용자 ID
     * @return boolean 인증 성공 시 true, 실패 시 false
     */
	private boolean isVerified(HttpServletRequest request, HttpServletResponse response, String userId) {

		String loginType = jwtService.extractLoginType(request, response, request.getCookies());
		String key = "auth:mypage:" + userId;
		
		if ("naver".equals(loginType) || "kakao".equals(loginType)) {
			if (!"true".equals(redisTemplate.opsForValue().get(key))) {
		        redisTemplate.opsForValue().set(key, "true", Duration.ofMinutes(10));
		    }
			return true;
		}
		String status = redisTemplate.opsForValue().get(key);
	    return "true".equals(status);
	}
}
