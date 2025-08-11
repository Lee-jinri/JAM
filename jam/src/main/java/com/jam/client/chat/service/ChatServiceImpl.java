package com.jam.client.chat.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jam.client.chat.vo.ChatVO;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;

@Service
@Log4j
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisTemplate<String, String> stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final UserServiceAdapter userServiceAdapter;
    
	// 사용자 채팅방 Id 조회
	public Set<String> getChatRoomId(String userId) {

		Set<String> rooms = stringRedisTemplate.opsForSet().members("user:rooms:" + userId);

		return rooms != null ? rooms : new HashSet<>();
	}

	@Override
	public void addParticipant(String chatRoomId, String otherUserId, String userId) {
		try {
			String otherUserName = stringRedisTemplate.opsForValue().get("users:name:" + otherUserId);
			String userName = stringRedisTemplate.opsForValue().get("users:name:" + userId);

			if(otherUserName == null) {
				otherUserName = getUserName(otherUserId);
		        stringRedisTemplate.opsForValue().set("users:name:" + otherUserId, otherUserName); 
			}
			
			if(userName == null) {
				userName = getUserName(userId);
				stringRedisTemplate.opsForValue().set("users:name:"+userId, userName);
			}
			
			// 키: chatRoomId:{chatRoomId}:participants
			// 필드: userId, 값: nickname
			stringRedisTemplate.opsForHash().put("chatRoomId:" + chatRoomId + ":participants", userId, userName);
			stringRedisTemplate.opsForHash().put("chatRoomId:" + chatRoomId + ":participants", otherUserId, otherUserName);

		}catch(Exception e) {
			log.error(e.getMessage());
		}
	}
	
	private String getUserName(String userId) {
		return userServiceAdapter.getUserName(userId);
	}
	
	@Override
	public String getUserNameFromRedis(String userId) {
		String key = "users:name:"+userId;
    	String userName = stringRedisTemplate.opsForValue().get(key);
    	
    	if(userName == null) userName = getUserName(userId);
    	
        return userName;
	}

	@Override
	public Map<String, String> getChatPartner(String chatRoomId, String userId) {
		try {
			Map<Object, Object> participants =
					stringRedisTemplate.opsForHash().entries("chatRoomId:" + chatRoomId + ":participants");

			Map<String, String> result = new HashMap<>();
			
			for (Map.Entry<Object, Object> entry : participants.entrySet()) {
				String id = (String) entry.getKey();
				String name = (String) entry.getValue();

				if (!id.equals(userId)) {
					result.put("chatPartnerId", id);
					result.put("chatPartnerName", name);
				}
			}
			return result;
		}catch(Exception e) {
			log.error(e.getMessage());
		}
		return null;
	}
	
	// 사용자 채팅방 목록과 마지막 메시지, 상대방 Id 조회
	@Override
	public List<ChatVO> getChatRooms(String userId) {
		Set<String> roomIds = stringRedisTemplate.opsForSet().members("user:rooms:" + userId);
		List<ChatVO> chatList = new ArrayList<>();

		// 방이 없으면 빈 리스트 반환
		if (roomIds == null || roomIds.isEmpty()) {
			return chatList;
		}

		for (String roomId : roomIds) {
			// 마지막 메시지 가져오기 (-1: 마지막 요소)
			Object obj = redisTemplate.opsForList().index("chatRoomId:" + roomId + ":messages", -1);
			if (obj == null) {
				// FIXME: 메시지 없는 방은 목록에서 뺄지 말지??
				continue;
			}

			ChatVO last;
			try {
				last = (ChatVO) obj; 
			} catch (ClassCastException e) {
				log.error("캐스팅 실패. Redis 직렬화 타입 확인 필요", e);
				continue;
			}

			// 상대방 정보 붙이기
			Map<String, String> partnerInfo = getChatPartner(roomId, userId);
			last.setPartner(partnerInfo != null ? partnerInfo.getOrDefault("chatPartnerName", "") : "");

			chatList.add(last);
		}

		// 최근 메시지 순 정렬
		chatList.sort((a, b) -> {
			String da = a.getChatDate() == null ? "" : a.getChatDate();
			String db = b.getChatDate() == null ? "" : b.getChatDate();
			return db.compareTo(da); // 내림차순
		});

		return chatList;
	}
	
	@Override
	public boolean ensureRoomOnFirstMessage(String chatRoomId, String userId, String partnerId) {
	    String listKey = "chatRoomId:" + chatRoomId + ":messages";
	    Long size = redisTemplate.opsForList().size(listKey);
        
	    // 첫 메시지면 채팅방 참여자 등록
	    if (size == null || size == 0) {
	    	stringRedisTemplate.opsForSet().add("user:rooms:" + userId, chatRoomId);
	        stringRedisTemplate.opsForSet().add("user:rooms:" + partnerId, chatRoomId);
	        return true;
	    }
	    
	    return false; // 이미 메시지 있음
	}
	
	@Override
	public void saveChat(ChatVO chat) {
		try {
			
			String chatRoomId = chat.getChatRoomId();
			
			chat.setMessageId(getNextMessageId(chatRoomId));
			
			chat.setChatDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            
			redisTemplate.opsForList().rightPush("chatRoomId:" + chatRoomId + ":messages", chat);
		}catch(Exception e) {
			log.error(e);
		}
	}
	
	public long getNextMessageId(String chatRoomId) {
	    String key = "message:id:" + chatRoomId;
	    return stringRedisTemplate.opsForValue().increment(key);
	}
	
	// 상대방과 나의 기존 채팅 존재 여부 확인 -> 있으면 채팅 반환 없으면 null
	@Override
	public List<ChatVO> getMessages(String chatRoomId, Pageable pageable) {

		long start = pageable.getOffset();
		long end = start + pageable.getPageSize() - 1;

		String key = "chatRoomId:" + chatRoomId + ":messages";

		// FIXME: 확인 할 것
		log.info("start : " + start);
		log.info("enddd : " + end);

		// 리스트의 길이를 가져옴
		long listSize = redisTemplate.opsForList().size(key);

		// 끝에서부터 100개의 시작 인덱스 계산
		long startIndex = Math.max(0, listSize - 100);
		
		List<Object> rawMessages = redisTemplate.opsForList().range(key, startIndex, listSize -1);

		List<ChatVO> messages = rawMessages.stream()
				.map(message -> objectMapper.convertValue(message, ChatVO.class))
				.collect(Collectors.toList());
		
		return messages;
	}

	// 채팅방 Id 생성
	@Override
	public String getChatRoomId(String userId, String targetUserId) {
		
		String chatRoomId = null;
		// ASCII 값 비교를 위해 문자열을 사전순으로 정렬
	    if (userId.compareTo(targetUserId) < 0) {
	        // userId가 더 작으면 userId가 앞에 오도록 설정
	    	chatRoomId = userId + "_" + targetUserId;
	    } else {
	        // targetUserId가 더 작거나 같으면 targetUserId가 앞에 오도록 설정
	    	chatRoomId = targetUserId + "_" + userId;
	    }
	    
	    return chatRoomId;
	}
	
	// 채팅방 존재하는지
	@Override
	public boolean roomExists(String chatRoomId) {
		String key = "chatRoomId:" + chatRoomId + ":participants";
		return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
	}

	// 채팅방에 참여한 멤버인지
	@Override
	public boolean isMemberOfRoom(String userId, String chatRoomId) {
		String key = "chatRoomId:" + chatRoomId + ":participants";
		return Boolean.TRUE.equals(stringRedisTemplate.opsForHash().hasKey(key, userId));
	}
}
