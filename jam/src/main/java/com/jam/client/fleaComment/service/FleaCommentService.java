package com.jam.client.fleaComment.service;

import java.util.List;

import com.jam.client.fleaComment.vo.FleaCommentVO;

public interface FleaCommentService {

	public List<FleaCommentVO> commentList(Long post_id);
	
	public int commentInsert(FleaCommentVO comment);
	
	public int commentUpdate(FleaCommentVO comment);
	
	public int commentDelete(Long comment_id);
}