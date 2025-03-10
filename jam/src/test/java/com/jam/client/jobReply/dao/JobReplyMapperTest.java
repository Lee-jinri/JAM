package com.jam.client.jobReply.dao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.jam.client.community.dao.ComMapperTest;
import com.jam.client.jobReply.vo.JobReplyVO;
import com.jam.config.RootConfig;

import lombok.Setter;
import lombok.extern.log4j.Log4j;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = RootConfig.class)
@Log4j
public class JobReplyMapperTest {
	
	@Setter(onMethod_=@Autowired)
	private JobReplyDAO jobreplyDao;
	
	/* 구인 댓글 리스트 
	@Test	
	public void testJobReplyList(){
		
		log.info("구인 댓글 조회");
		log.info(jobreplyDao.replyList(5L));	
	}*/

	/* 구인 댓글 입력 
	@Test
	public void replyInsert() {
		JobReplyVO jobreply_vo = new JobReplyVO();
		jobreply_vo.setJob_no(35L);
		jobreply_vo.setJobReply_content("댓글 입력");
		jobreply_vo.setUser_id("abcd123");
		jobreply_vo.setUser_name("김철수");
		
		log.info(jobreplyDao.replyInsert(jobreply_vo));
	}*/

	/* 구인 댓글 수정
	@Test
	public void replyUpdate() {
		JobReplyVO jobreply_vo = new JobReplyVO();
		jobreply_vo.setJobReply_no(15L);
		jobreply_vo.setJobReply_content("댓글 수정");
		jobreply_vo.setUser_id("abcd1234");
		
		log.info(jobreplyDao.replyUpdate(jobreply_vo));
	}
	 */
	
	/* 구인 댓글 삭제
	@Test
	public void replyDelete() {
		
		log.info(jobreplyDao.replyDelete(15L));
	}
	 */
	
	/* 구인 댓글의 글 번호 조회 
	@Test
	public void replyRead() {
		log.info(jobreplyDao.replyRead(17L));
	}
	*/	
}
