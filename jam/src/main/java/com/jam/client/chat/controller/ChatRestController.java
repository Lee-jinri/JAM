package com.jam.client.chat.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jam.client.chat.service.ChatService;
import com.jam.client.chat.vo.ChatVO;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;

@RestController
@RequestMapping("/api/chat")
@Log4j
@RequiredArgsConstructor
public class ChatRestController {
	// FIXME: 무한 스크롤 만들어야 함
	private final ChatService chatService;
	
	/**
	 * 채팅방 ID 조회 또는 생성
	 * - targetUserId와 현재 로그인 유저(userId) 기준으로 채팅방 ID를 가져옴
	 * - 존재하지 않으면 새로 생성
	 * - Redis에 참여자 정보 저장
	 *
	 * @param targetUserId  채팅 상대방의 사용자 ID
	 * @param request       HttpServletRequest (JWT 인증 필터에서 userId를 setAttribute로 저장해 둠)
	 * @return 채팅방 ID
	 */
	@GetMapping(value="/chatRoomId")
    public ResponseEntity<String> getChatRoomId(@RequestParam String targetUserId, HttpServletRequest request) {

    	String userId = (String)request.getAttribute("userId");
    	
    	if(userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
    	if(targetUserId == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);

    	String chatRoomId = chatService.getChatRoomId(userId, targetUserId);
    	
    	chatService.addParticipant(chatRoomId, targetUserId, userId);
    	
    	return ResponseEntity.ok(chatRoomId);
    }
	
	/**
	 * 특정 채팅방의 메시지 목록 조회
	 * - 최근 메시지 100건 가져옴
	 * - 각 메시지에 mine 필드 세팅 (현재 유저가 보낸 메시지인지 여부)
	 *
	 * @param chatRoomId   채팅방 ID
	 * @param request      HttpServletRequest (userId 포함)
	 * @return 메시지 리스트
	 */
    @GetMapping("/messages")
    public ResponseEntity<List<ChatVO>> messages(@RequestParam String chatRoomId, HttpServletRequest request) {
        
    	try {
    		// page, size
        	Pageable pageable = PageRequest.of(1, 100);
        	        
        	List<ChatVO> messages = chatService.getMessages(chatRoomId, pageable);
        	
        	String userId = (String) request.getAttribute("userId");
        	
        	for (ChatVO message : messages) {
        		message.setMine(message.getSenderId().equals(userId));
            }
        	
            return ResponseEntity.ok(messages);
    	}catch(Exception e){
    		log.error(e.getMessage());
    	}
    	
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);    
    }
    
    /**
     * 채팅 상대방 정보 조회
     * - 채팅방 ID와 현재 로그인한 유저 기준으로 상대방 ID, 이름 반환
     *
     * @param chatRoomId  채팅방 ID
     * @param request     HttpServletRequest (userId 포함)
     * @return partnerId, partnerName 포함한 Map
     */
    @GetMapping(value="/chatPartner")
    public ResponseEntity<Map<String,String>> getChatPartner(@RequestParam String chatRoomId, HttpServletRequest request){
    	
    	String userId = (String)request.getAttribute("userId");
    	
    	Map<String, String> chatPartner = chatService.getChatPartner(chatRoomId, userId);
    	
    	return ResponseEntity.ok(chatPartner);
    }
    
    /**
     * 내가 참여 중인 채팅방 목록 조회
     * - 최근 대화 순으로 정렬되어 반환
     *
     * @param request  HttpServletRequest (userId 포함)
     * @return 채팅방 목록 (ChatVO 리스트)
     */
    @GetMapping(value="/chatRooms")
    public ResponseEntity<List<ChatVO>> getChatRooms(HttpServletRequest request){
    	
        try {
        	String userId = (String) request.getAttribute("userId");
        	
        	if (userId == null) {
        		return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        	}
        	
    		List<ChatVO> chatRooms = chatService.getChatRooms(userId);

			return ResponseEntity.ok().body(chatRooms);
		}catch(Exception e) {
			log.error(e.getMessage());
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
    }
    
}
