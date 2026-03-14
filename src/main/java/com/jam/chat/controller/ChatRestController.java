package com.jam.chat.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jam.chat.dto.ChatDto;
import com.jam.chat.dto.ChatRoomListDto;
import com.jam.chat.service.ChatRoomFacade;
import com.jam.chat.service.ChatService;
import com.jam.fleaMarket.service.FleaMarketService;
import com.jam.global.exception.BadRequestException;
import com.jam.global.exception.NotFoundException;
import com.jam.global.exception.UnauthorizedException;
import com.jam.member.dto.MemberDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/chat")
@Slf4j
@RequiredArgsConstructor
public class ChatRestController {
	// TODO: 무한 스크롤
	private final ChatService chatService;
	private final ChatRoomFacade chatRoomFacade;
	private final FleaMarketService fleaService;
	
    /**
     * 내가 참여 중인 채팅방 목록 조회
     * 
     * - 현재 로그인한 사용자의 채팅방 목록을 조회
	 * 
	 * @param user	현재 로그인한 사용자
	 * @return List<ChatRoomListVO> 채팅 상대방 정보와 마제막 메시지, 전송 시간
     */
    @GetMapping(value="/chatRooms")
    public ResponseEntity<List<ChatRoomListDto>> getChatRooms(
    		@AuthenticationPrincipal MemberDto user){
    	
    	if (user == null) {
    		throw new UnauthorizedException("로그인이 필요한 서비스 입니다. 로그인 페이지로 이동하겠습니까?");
    	}
    	
    	List<ChatRoomListDto> chatRooms = chatService.getChatRooms(user.getUser_id());
		return ResponseEntity.ok().body(chatRooms);
    }
    
	/**
	 * 채팅방 ID 조회 또는 생성
	 * 
	 * - 게시글 ID를 기반으로 작성자(상대방)와 현재 사용자 간의 채팅방을 확인합니다.
	 * - 기존 채팅방이 존재하지 않으면 새 채팅방을 생성한 후 ID를 반환합니다.
	 *
	 * @param postId  	게시글 ID
	 * @param user		현재 로그인한 사용자
	 * @return 채팅방 ID
	 */
	@GetMapping(value="/chatRoomId")
    public ResponseEntity<Long> getChatRoomId(
    		@RequestParam Long postId,
			@AuthenticationPrincipal MemberDto user) {

    	if(user == null || user.getUser_id() == null) throw new UnauthorizedException("로그인이 필요한 서비스 입니다. 로그인 페이지로 이동하겠습니까?");
    	if(postId == null) throw new BadRequestException("잘못된 요청입니다. 잠시 후 다시 시도하세요.");

		String targetUserId = fleaService.getWriterIdByPostId(postId);
		if(targetUserId == null) throw new NotFoundException("존재하지 않는 게시글입니다.");
		
    	Long roomId = chatRoomFacade.getOrCreateChatRoomId(user.getUser_id(), targetUserId);
    	
    	return ResponseEntity.ok(roomId);
    }
	
	/**
	 * 특정 채팅방의 메시지 목록 조회
	 * 
	 * - 각 메시지에 대해 현재 사용자가 보낸 메시지인지 여부(mine) 설정
	 *
	 * @param roomId	채팅방 ID
	 * @param user		현재 로그인한 사용자 정보
	 * @return 메시지 목록
	 */
    @GetMapping("/messages")
    public ResponseEntity<List<ChatDto>> messages(
    		@RequestParam Long roomId,
			@AuthenticationPrincipal MemberDto user) {
        
    	if(user == null || user.getUser_id() == null) throw new UnauthorizedException("로그인이 필요한 서비스 입니다. 로그인 페이지로 이동하겠습니까?");
    	
    	List<ChatDto> messages = chatService.getMessages(roomId, user.getUser_id());
    	
        return ResponseEntity.ok(messages);   
    }
}