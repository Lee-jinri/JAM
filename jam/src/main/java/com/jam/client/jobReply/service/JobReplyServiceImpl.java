package com.jam.client.jobReply.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jam.client.job.dao.JobDAO;
import com.jam.client.jobReply.dao.JobReplyDAO;
import com.jam.client.jobReply.vo.JobReplyVO;

import lombok.Setter;

@Service
public class JobReplyServiceImpl implements JobReplyService {

	private final static int reply_Add = 1;
	private final static int reply_Del = -1;
	
	@Setter(onMethod_=@Autowired)
	private JobReplyDAO jobReplyDao;
	
	@Setter(onMethod_=@Autowired)
	private JobDAO jobDao;
	
	
	@Override
	public List<JobReplyVO> jobReplyList(Long job_no) {
		List<JobReplyVO> list = jobReplyDao.replyList(job_no);
		
		return list;
	}

	@Transactional
	@Override
	public int replyInsert(JobReplyVO jrvo) {
		
		// 댓글 개수 증가
		jobDao.updateReplyCnt(jrvo.getJob_no(), reply_Add);
		return jobReplyDao.replyInsert(jrvo);
	}

	@Override
	public int replyUpdate(JobReplyVO jrvo) {
		return jobReplyDao.replyUpdate(jrvo);
	}

	@Transactional
	@Override
	public int replyDelete(Long jobReply_no) {
		
		// 댓글 개수 감소
		JobReplyVO vo = jobReplyDao.replyRead(jobReply_no);
		jobDao.updateReplyCnt(vo.getJob_no(), reply_Del);
				
		return jobReplyDao.replyDelete(jobReply_no);
	}


}
