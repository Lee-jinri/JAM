package com.jam.client.chat.service;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jam.client.chat.vo.ChatVO;
import com.jam.global.redis.RedisConfig;

import lombok.extern.slf4j.Slf4j;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = RedisConfig.class)
@Slf4j
public class RedisTest {
	
	@Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RedisTemplate<String, String> stringRedisTemplate;
	/*
	@Test
    public void testRedisSerialization() {
        // 1. 데이터 생성
        ChatVO chat = new ChatVO();
        chat.setMessageId(1L);
        chat.setMessage("Test Message");
        chat.setSenderId("sender");
        chat.setReceiverId("receiver");
        chat.setChatRoomId("testRoom");
        chat.setTimestamp("2024-12-30T05:23:33");

        // 2. 데이터 저장
        String key = "testChatRoom:messages";
        redisTemplate.opsForList().rightPush(key, chat);

        // 3. 데이터 가져오기
        ChatVO result = (ChatVO) redisTemplate.opsForList().leftPop(key);

        // 결과 출력
        log.info("Retrieved ChatVO: {}" + result);
        System.out.println("Expected: " + chat);
        System.out.println("Retrieved: " + result);
        
        if (!chat.equals(result)) {
        	log.info("chat != result");
        }
	}*/
	
	/*
	@Test
	public void saveChat() {
		
		ChatVO chat = new ChatVO();
		chat.setChatRoomId("abcd1234_admin");
		chat.setSenderId("abcd1234");
		chat.setReceiverId("admin");
		chat.setMessage("ㅁㅁ");
		chat.setMessageId(4L);
		
		redisTemplate.opsForList().rightPush("chatRoomId:abcd1234_admin:messages", chat);
		System.out.println(redisTemplate.opsForList().range("chatRoomId:abcd1234_admin:messages", 0, -1));
	}
	*/
	
	
	/*
	 * 1. List<Object>로 데이터 가져오기 (range는 ChatVO로 변환 안댐)
	 * 2. objectMapper로 형변환하기
	 * 3. List<ChatVO>로 리턴~~
	 *
	@Test
	public void getMessages() {
		 // Redis에서 데이터 가져오기
	    List<Object> rawMessages = redisTemplate.opsForList().range("chatRoomId:abcd1234_admin:messages", 0, -1);
	    
	    log.info(rawMessages);
	    
	    ObjectMapper objectMapper = new ObjectMapper();
	    
	    
        List<ChatVO> messages = rawMessages.stream()
            .map(message -> objectMapper.convertValue(message, ChatVO.class))
            .collect(Collectors.toList());

        log.info(messages);
	    // 결과 출력
	    for (ChatVO chat : messages) {
	        log.info("ChatVO: "+ chat);
	    }
	} */
	/*
	@Test
	public void addParticipant() {
		String chatRoomId = "abcd1234_admin";
		String otherUserId = "admin";
		String userId = "abcd1234";
		String key = "chatRoomId:" + chatRoomId + ":participants";
		
		stringRedisTemplate.opsForSet().add(key, userId);
		stringRedisTemplate.opsForSet().add(key, otherUserId);
		
	}
	
	*//*
	@Test
	public void getChatRooms() {
		String userId = "abcd1234";
		
		Set<String> roomIds = stringRedisTemplate.opsForSet().members("user:rooms:"+userId);
		
		List<ChatVO> chatList = new ArrayList<>();
		
		
		for(String r: roomIds) {
			String key = "chatRoomId:" + r + ":participants";
	        Set<String> participants = stringRedisTemplate.opsForSet().members(key);
	        String participant = null;
	        
	        
	        if (participants == null || participants.isEmpty()) {
	            log.error("participants is null.");
	        } 

	        // 상대방 아이디 찾기
	        for (String p : participants) {
	            if (!p.equals(userId)) {
	            	participant = stringRedisTemplate.opsForValue().get("users:name:" + p);
	                 
	            }
	        }
	        
	        
			Object message = redisTemplate.opsForList().index("chatRoomId:" + r + ":messages", -1);
			
			ChatVO chats = (ChatVO) message;
			
			if(participant != null) chats.setParticipant(participant);
			
			if (message != null) {
				chatList.add(chats);
	        }
		}
		
		log.info("chatList: "+chatList);
		
	}
	
	@Test
	public void aaaa() {*/
		/*
		String key = "users:name:admin";
		String userName = stringRedisTemplate.opsForValue().get(key);
		log.info(userName);
		
		
		MemberVO member = new MemberVO();
		member.setUser_name("뭉댕이");
		member.setUser_id("abcd1234");
		
		String key = "users:name:" + member.getUser_id();
		
		stringRedisTemplate.opsForValue().set(key, member.getUser_name());
		
	}*/
}
