package com.jam.client.jobReply.vo;

import lombok.Data;

@Data
public class JobReplyVO {

	private Long jobReply_no;			// 댓글 번호
	private Long job_no;				// 구인 글 번호
	private String jobReply_content;	// 댓글 내용
	private String jobReply_date;		// 댓글 작성일
	private String user_id;				// 댓글 작성자 id	
	private String user_name;			// 댓글 작성자 닉네임
}
