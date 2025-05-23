package com.jam.client.job.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.jam.client.community.dao.ComMapperTest;
import com.jam.client.job.vo.JobVO;
import com.jam.common.vo.PageDTO;
import com.jam.config.RootConfig;
import com.jam.global.util.ValueUtils;

import lombok.Setter;
import lombok.extern.log4j.Log4j;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = RootConfig.class)
@Log4j
public class JobMapperTest {
	
	@Setter(onMethod_=@Autowired)
	private JobDAO jobDao;
	
	/* 전체 글 조회 */
	@Test
	public void testJobList() {
		JobVO vo = new JobVO();
		vo.setPageNum(1);
		
		Map<String, Object> result = new HashMap<>();
		
		vo.setUser_id("abcd1234");
		vo.setJob_category(0);
		//vo.setCity("서울");
		//vo.setPositions(List.of("guitar"));

		List<JobVO> jobList = new ArrayList<>();
		// jobList = jobDao.getBoards(vo);
		
		jobList = jobDao.getBoardsWithFavorite(vo);
		
		result.put("jobList", jobList);
		
		int total = jobDao.listCnt(vo);
		PageDTO pageMaker = new PageDTO(vo, total);
		
		result.put("pageMaker", pageMaker);
		
		log.info("result :" +result);
	}
	
	
	/* 상세 페이지 조회
	@Test
	public void testJobDatail() {
		log.info(jobDao.getBoardDetail(33L));
	} */
	
	/* 조회수 증가 
	@Test
	public void testIncreamentReadCnt() {
		jobDao.incrementReadCnt(33L);
	}*/
	
	/* 글 작성 
	@Test
	public void testJobInsert() {
		JobVO vo = new JobVO();
		
		log.info("글 입력");
		
		vo.setJob_title("작성 테스트");
		vo.setJob_content("작성 테스트");
		vo.setUser_id("abcd123");
		vo.setUser_name("김철수");
		vo.setJob_category(1);
		vo.setCity("서울");
		vo.setGu(ValueUtils.emptyToNull(vo.getGu()));
		vo.setDong(ValueUtils.emptyToNull(vo.getDong()));
		
		vo.setPosition("piano");
		log.info(jobDao.writeBoard(vo));
	}
	*/
	/* 수정할 글 정보 
	@Test
	public void testGetBoardById() {
		log.info(jobDao.getBoardById(36L));
	}*/
	
	/* 수정 
	@Test
	public void testJobUpdate() {
		JobVO vo = new JobVO();
		
		log.info("글 수정");
		vo.setJob_no(36L);
		vo.setJob_title("수정 테스트");
		vo.setJob_content("수정 테스트");
		vo.setJob_category(1);
		vo.setPay_category(1);
		vo.setJob_status(1);
		vo.setPay(2000);
		vo.setUser_id("abcd1234");
		vo.setCity("서울");
		vo.setGu("전체");
		vo.setDong("전체");
		vo.setPosition("piano");
		log.info(jobDao.editBoard(vo));
	}
	*/
	
	/* 삭제
	@Test
	public void testJobDelete() {
		log.info("글 삭제 " + jobDao.boardDelete(36L));
	} */
}
