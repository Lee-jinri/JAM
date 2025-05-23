package com.jam.client.message.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.jam.client.message.dao.MessageDAO;
import com.jam.client.message.vo.MessageVO;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;

@Service
@RequiredArgsConstructor
@Log4j
public class MessageServiceImpl implements MessageService {

	private final MessageDAO messageDao;

	// 받은 쪽지
	@Override
	public List<MessageVO> receiveMessage(MessageVO message) {
		return messageDao.receiveMessage(message);
	}

	// 보낸 쪽지
	@Override
	public List<MessageVO> sendMessage(MessageVO message) {
		return messageDao.sendMessage(message);
	}
	
	// 받은 쪽지 페이징
	@Override
	public int receiveListCnt(MessageVO message) {
		return messageDao.receiveListCnt(message);
	}
	
	// 보낸 쪽지 페이징
	@Override
	public int sendListCnt(MessageVO message) {
		return messageDao.sendListCnt(message);
	}
	

	// 쪽지 상태 읽음으로 변경
	@Override
	public void message_read(Long message_no) {
		messageDao.message_read(message_no);
		
	}

	// 받은 쪽지 상세페이지
	@Override
	public MessageVO receiveMsgDetail(Long message_no, String receiver_id) {
		return messageDao.receiveMsgDetail(message_no, receiver_id);
	}

	// 보낸 쪽지 상세페이지
	@Override
	public MessageVO sendMsgDetail(Long message_no, String sender_id) {
		return messageDao.sendMsgDetail(message_no, sender_id);
	}

	// 쪽지 발송
	@Override
	public int send(MessageVO message) {
		return messageDao.send(message);
	}

	
	
}
