package com.jam.client.chat.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.jam.client.chat.vo.ChatVO;
import com.jam.config.JsonUtils;
import com.jam.config.RedisConfig;

import lombok.extern.log4j.Log4j;

@Service
@Log4j
public class ChatServiceImpl implements ChatService {
	
	private RedisTemplate<String, Object> redisTemplate;
    private RedisTemplate<String, String> stringRedisTemplate;
    private ObjectMapper objectMapper;
    private UserServiceAdapter userServiceAdapter;

    @Autowired
    public void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Autowired
    public void setStringRedisTemplate(RedisTemplate<String, String> stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Autowired
    public void setUserServiceAdapter(UserServiceAdapter userServiceAdapter) {
        this.userServiceAdapter = userServiceAdapter;
    }
    
	/*
	 * 사용자 아이디를 키로 참여한 채팅방 저장
	 * 메시지 전송 시점에 저장함
	 * 계속 저장해도 상관X Set 중복 허용 안하니까
	 * */
	@Override
	public void addUserToChatRoom(String userId, String targetUserId, String chatRoomId) {
	    
		// 사용자Id에 채팅방 Id 저장  
		stringRedisTemplate.opsForSet().add("user:rooms:" + userId, chatRoomId);
		stringRedisTemplate.opsForSet().add("user:rooms:"+ targetUserId, chatRoomId);
	}

	// 사용자 채팅방 Id 조회
	public Set<String> getChatRoomId(String userId) {

		Set<String> rooms = stringRedisTemplate.opsForSet().members("user:rooms:" + userId);

		return rooms != null ? rooms : new HashSet<>();
	}

	/* @Parameter boolean active - true(채팅방 활성화) false(채팅방 비활성화, 채팅 목록에서 채팅방 나가기 했을 때)
	 * 채팅방 입장,  채팅 전송, 채팅방 나가기 할 때 실행됨 */
	@Override
	public void addParticipant(String chatRoomId, String participant, String userId) {
		try {
			String otherUserId = getTargetUserId(participant);
			
			stringRedisTemplate.opsForSet().add("chatRoomId:" + chatRoomId + ":participants", otherUserId);
			stringRedisTemplate.opsForSet().add("chatRoomId:" + chatRoomId + ":participants", userId);

		}catch(Exception e) {
			log.error(e.getMessage());
		}
	}
	
	@Override
	public String getParticipant(String chatRoomId, String userId) {
		try {
			Set<String> participants = stringRedisTemplate.opsForSet().members(chatRoomId + ":participants");
			
			if (participants != null) {
		        // 현재 사용자 ID를 제외한 나머지 ID 반환
		        for (String participant : participants) {
		            if (!participant.equals(userId)) {
		            	log.info(participant);
		            	
		                return participant;
		            }
		        }
		    }
			
		}catch(Exception e) {
			log.error(e.getMessage());
		}
		return null;
	}
	
	@Override
	// 사용자 채팅방 목록과 마지막 메시지, 상대방 Id 조회
	public List<ChatVO> getChatRooms(String userId){
		
		Set<String> roomIds = stringRedisTemplate.opsForSet().members("user:rooms:" + userId);

		List<ChatVO> chatList = new ArrayList<>();
		
		for (String r : roomIds) {
			Object message = redisTemplate.opsForList().index("chatRoomId:"+ r +":messages", -1);
					
			if(message != null) chatList.add((ChatVO)message);
		}
		
		log.info("getChatRooms : " +chatList);
		return chatList;
	}
	
	// 채팅방에 채팅 정보 추가// 채팅 정보 조회// 사용자 채팅방 목록 조회// 사용자 채팅방 목록 추가
	@Override
	public void addMessageToChatRoom(String chatRoomId, ChatVO chat) {
		
		try {
	        redisTemplate.opsForList().rightPush("room:" + chatRoomId + ":messages", chat);
	    } catch (Exception e) {
	        throw new RuntimeException("Failed to save chat message", e);
	    }
		
	}
	

	
	// 채팅 정보 조회
	/*
	@Override
	public List<ChatVO> getChatMessages(String chatRoomId, Pageable pageable) {
		long start = pageable.getOffset();
	    long end = start + pageable.getPageSize() - 1;

	    List<Object> range = redisTemplate.opsForList().range("room:" + chatRoomId + ":messages", start, end);

	    // 변환 로직이 필요하다면 아래와 같이 처리합니다.
	    return convertToChatMessageList(range);
	}*/
	
	@Override
	public List<String> getChatMessages(String chatRoomId, Pageable pageable) {
		long start = pageable.getOffset();
	    long end = start + pageable.getPageSize() - 1;

	    log.info("start :" + start);
	    log.info("end : " + end);
	    
	    List<String> previousMessages = stringRedisTemplate.opsForList().range("chatRoomId:" + chatRoomId + ":messages", start, end);
	    
	    log.info(previousMessages);
	    
	    // List<Object> range = redisTemplate.opsForList().range("room:" + chatRoomId + ":messages", start, end);
	    
	    return previousMessages;
	}
	
	@Override
	public String getTargetUserId(String userName) {
		return userServiceAdapter.getUserId(userName);
	}
	
	@Override
	public void saveChat(ChatVO chat) {
		try {
			
			String chatRoomId = chat.getChatRoomId();
			
			chat.setMessageId(getNextMessageId(chatRoomId));
			
			chat.setChatDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            chat.setStatus("unread");
            
            log.info(chat);
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

		// 이거 잘못됨
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
		
	
	private List<ChatVO> convertToChatMessageList(List<Object> objects) {
        return objects.stream()
                .map(obj -> {
                    try {
                        // JSON 문자열을 ChatVO 객체로 변환
                        return objectMapper.readValue((String) obj, ChatVO.class);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to convert JSON to ChatVO", e);
                    }
                })
                .collect(Collectors.toList());
    }
	
	/*
	private List<ChatVO> convertToChatMessageList(List<Object> objects) {
        return objects.stream()
                .map(obj -> {
                    try {
                        // JSON 문자열을 ChatMessage 객체로 변환
                        return objectMapper.readValue(obj.toString(), ChatVO.class);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to convert JSON to ChatMessage", e);
                    }
                })
                .collect(Collectors.toList());
    }*/

	// 발신자와 수신자 함께하는 채팅방이 존재하는지 확인
	@Override
	public boolean getCommonChatRooms(String userId1, String userId2) {
		
		Set<String> user1Rooms = getChatRoomId(userId1);
        Set<String> user2Rooms = getChatRoomId(userId2);
        user1Rooms.retainAll(user2Rooms);  // 교집합 구하기
       
        return !user1Rooms.isEmpty();
	}
	
	
	
	// 채팅방 Id 생성
	@Override
	public String getChatRoomId(String userId, String targetUserId) {
		
		// 유효성 검사: userId나 targetUserId가 null이거나 빈 문자열인 경우 예외 처리
	    if (userId == null || userId.isEmpty() || targetUserId == null || targetUserId.isEmpty()) {
	        return null;
	    }
	    
		// ASCII 값 비교를 위해 문자열을 사전순으로 정렬
	    if (userId.compareTo(targetUserId) < 0) {
	        // userId가 더 작으면 userId가 앞에 오도록 설정
	        return userId + "_" + targetUserId;
	    } else {
	        // targetUserId가 더 작거나 같으면 targetUserId가 앞에 오도록 설정
	        return targetUserId + "_" + userId;
	    }
	}
	
	private <T> Optional<T> getData(String key, Class<T> classType) {
	    String jsonData = (String) stringRedisTemplate.opsForValue().get(key);

	    try {
	        if (StringUtils.hasText(jsonData)) {
	            return Optional.ofNullable(objectMapper.readValue(jsonData, classType));
	        }
	        return Optional.empty();
	    } catch (JsonProcessingException e) {
	        throw new RuntimeException(e);
	    }
	}

	@Override
	public ChatVO convertToChatMessageVO(String payload) {
		ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(payload, ChatVO.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert payload to VO", e);
        }
	}

	@Override
	public List<ChatVO> getMessagesByRoomId(Long chatRoomId) {
		// TODO Auto-generated method stub
		return null;
	}

	

}
