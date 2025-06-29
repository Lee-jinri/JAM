package com.jam.client.fleaComment.dao;

import java.util.List;

import com.jam.client.fleaComment.vo.FleaCommentVO;

public interface FleaCommentDAO {

	List<FleaCommentVO> commentList(Long post_id);

	int commentInsert(FleaCommentVO comment);

	int commentUpdate(FleaCommentVO comment);

	FleaCommentVO getPostIdByCommentId(Long comment_id);

	int commentDelete(Long comment_id);

}
