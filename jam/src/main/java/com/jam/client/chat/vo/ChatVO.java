package com.jam.client.chat.vo;

import java.util.Collections;
import java.util.List;


import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class ChatVO {

	private Long messageId;				// 메시지의 고유 식별자. 데이터베이스에서 자동으로 생성되는 일련번호나 UUID가 사용될 수 있습니다.
	
	private String message;
	private List<String> messages;
	
    private String senderId;
    private String senderName;
    private String receiverId;
    private String receiverName;
    private String chatDate;
    private String chatRoomId;			
    
    private String partner;
    private boolean mine;
    
    public ChatVO(String chatRoomId) {
        this.chatRoomId = chatRoomId;
        this.messages = Collections.emptyList(); 
    }
    
    public ChatVO(String chatRoomId, List<String> messages) {
        this.chatRoomId = chatRoomId;
        this.messages = messages;
    }
    
    public enum Type {
        ENTER,   // 사용자가 채팅방에 입장할 때
        LEAVE,   // 사용자가 채팅방에서 나갈 때
        MESSAGE  // 사용자가 일반 메시지를 보낼 때
    }
    
    private Type type; // 메시지 유형 필드 text, image, file?
    private int page;
    private int size;
}
