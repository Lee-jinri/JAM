package com.jam.client.message.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jam.client.message.dao.MessageDAO;
import com.jam.client.message.vo.MessageVO;

@Service
public class MessageServiceImpl implements MessageService {

	@Autowired
	private MessageDAO messageDao;

	// 받은 쪽지
	@Override
	public List<MessageVO> receiveMessage(MessageVO m_vo) {
		return messageDao.receiveMessage(m_vo);
	}

	// 보낸 쪽지
	@Override
	public List<MessageVO> sendMessage(MessageVO m_vo) {
		return messageDao.sendMessage(m_vo);
	}
	
	// 받은 쪽지 페이징
	@Override
	public int receiveListCnt(MessageVO m_vo) {
		return messageDao.receiveListCnt(m_vo);
	}
	
	// 보낸 쪽지 페이징
	@Override
	public int sendListCnt(MessageVO m_vo) {
		return messageDao.sendListCnt(m_vo);
	}
	

	// 쪽지 상태 읽음으로 변경
	@Override
	public void message_read(MessageVO message_vo) {
		messageDao.message_read(message_vo);
		
	}

	// 보낸 쪽지 상세페이지
	@Override
	public MessageVO rMessageDetail(MessageVO message_vo) {
		return messageDao.rMessageDetail(message_vo);
	}

	// 받은 쪽지 상세페이지
	@Override
	public MessageVO sMessageDetail(MessageVO message_vo) {
		return messageDao.sMessageDetail(message_vo);
	}

	// 쪽지 발송
	@Override
	public int messageWrite(MessageVO message_vo) {
		return messageDao.messageWrite(message_vo);
	}

	
}
