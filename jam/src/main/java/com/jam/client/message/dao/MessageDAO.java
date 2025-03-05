package com.jam.client.message.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.jam.client.message.vo.MessageVO;

public interface MessageDAO {

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
	public MessageVO receiveMsgDetail(@Param("message_no") Long message_no, @Param("receiver_id") String receiver_id);

	// 보낸 쪽지 상세 페이지
	public MessageVO sendMsgDetail(@Param("message_no") Long message_no, @Param("sender_id") String sender_id);

	// 쪽지 발송
	public int send(MessageVO message);

	
	
	
}
