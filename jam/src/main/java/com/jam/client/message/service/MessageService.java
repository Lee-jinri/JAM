package com.jam.client.message.service;

import java.util.List;

import javax.servlet.http.Cookie;

import com.jam.client.message.vo.MessageVO;

public interface MessageService {

	// 받은 쪽지
	public List<MessageVO> receiveMessage(MessageVO message);
	
	// 보낸 쪽지
	public List<MessageVO> sendMessage(MessageVO message);
	
	// 받은 쪽지 페이징
	public int receiveListCnt(MessageVO message);

	// 보낸 쪽지 페이징
	public int sendListCnt(MessageVO message);

	// 쪽지 상태 읽음으로 변경
	public void message_read(Long message_no);

	// 받은 쪽지 상세
	public MessageVO receiveMsgDetail(Long message_no, String receiver_id);

	// 보낸 쪽지 상세
	public MessageVO sendMsgDetail(Long message_no, String sender_id);

	// 쪽지 발송
	public int send(MessageVO message);

}
