package com.jam.client.message.dao;

import java.util.List;

import com.jam.client.message.vo.MessageVO;

public interface MessageDAO {

	// 받은 쪽지
	public List<MessageVO> receiveMessage(MessageVO m_vo);

	// 보낸 쪽지
	public List<MessageVO> sendMessage(MessageVO m_vo);
	
	// 받은 쪽지 페이징
	public int receiveListCnt(MessageVO m_vo);

	// 보낸 쪽지 페이징
	public int sendListCnt(MessageVO m_vo);
		
	// 쪽지 상태 읽음으로 변경
	public void message_read(Long message_no);

	// 받은 쪽지 상세
	public MessageVO receiveMsgDetail(Long message_no);

	// 보낸 쪽지 상세 페이지
	public MessageVO sendMsgDetail(Long message_no);

	// 쪽지 발송
	public int messageWrite(MessageVO message_vo);

	
	
	
}
