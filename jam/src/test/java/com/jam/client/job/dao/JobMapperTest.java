package com.jam.client.job.dao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.jam.client.job.vo.JobVO;

import lombok.Setter;
import lombok.extern.log4j.Log4j;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/main/webapp/WEB-INF/spring/root-context.xml")
@Log4j
public class JobMapperTest {
	
	@Setter(onMethod_=@Autowired)
	private JobDAO jobDao;
	
	/* list
	@Test
	public void testJobList() {
		JobVO vo = new JobVO();
		
		log.info("JOB 조회");
		log.info(jobDao.jobList(vo));
	}*/
	
	
	/* detail
	@Test
	public void testJobDatail() {
		JobVO vo = new JobVO();
		
		log.info("글 상세");
		vo.setJob_no(2);
		log.info(jobDao.jobDetail(vo));
	} */
	
	/* insert 
	@Test
	public void testJobInsert() {
		JobVO vo = new JobVO();
		
		log.info("글 입력");
		
		vo.setJob_title("?번째 글");
		vo.setJob_content("?번째 글");
		vo.setUser_id("abcd123");
		vo.setUser_name("김철수");
		vo.setJob_hits(0);
		vo.setJob_reply_cnt(0);
		log.info(jobDao.jobInsert(vo));
		
	}*/
	
	/* update 
	@Test
	public void testJobUpdate() {
		JobVO vo = new JobVO();
		
		log.info("글 수정");
		vo.setJob_no(4);
		vo.setJob_title("?번째 글 수정");
		vo.setJob_content("?번째 글 수정");
		log.info(jobDao.jobUpdate(vo));
	}*/
	
	/* delete
	@Test
	public void testJobDelete() {
		JobVO vo = new JobVO();
		vo.setJob_no(4);
		log.info("글 삭제 " + jobDao.jobDelete(vo));
	}*/ 
}
