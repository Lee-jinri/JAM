package com.jam.client.message.dao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.jam.client.community.dao.ComMapperTest;
import com.jam.client.message.vo.MessageVO;
import com.jam.config.RootConfig;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = RootConfig.class)
@Slf4j
public class MessageMapperTest {

	@Setter(onMethod_=@Autowired)
	private MessageDAO messageDao;
	
	/* 받은 쪽지
	@Test
	public void testReceiveMessage() {
		
		String receiver_id = "abcd123";
		
		log.info("받은 쪽지 Test" +messageDao.receiveMessage(receiver_id));
	} */
	
	/* 보낸 쪽지
	@Test
	public void testSendMessage() {
		
		String sender_id = "abcd123";
		
		log.info("보낸 쪽지 Test" +messageDao.sendMessage(sender_id));
	} */
	
	/* 받은 쪽지 detail 
	@Test
	public void testRMessageDetail() {
		MessageVO vo = new MessageVO();
		vo.setMessage_no(1);
		log.info(messageDao.rMessageDetail(vo));
	}
	*/
	
	/* 보낸 쪽지 detail 
	@Test
	public void testSMessageDetail() {
		MessageVO vo = new MessageVO();
		vo.setMessage_no(1);
		log.info(messageDao.sMessageDetail(vo));
	}*/
	
	/* 쪽지 보내기 
	@Test
	public void testMessageWrite() {
		MessageVO vo = new MessageVO();
		vo.setSender("김철수");
		vo.setSender_id("abcd123");
		vo.setReceiver_id("moong123");
		vo.setReceiver("이뭉이");
		vo.setMessage_contents("쪽지 보내기 test 내용");
		vo.setMessage_title("쪽지 보내기 test 제목");
		
		int count = messageDao.messageWrite(vo);
		log.info("쪽지 보내기 " + count);
		log.info(messageDao.messageWrite(vo));
	}*/
	
	
}
