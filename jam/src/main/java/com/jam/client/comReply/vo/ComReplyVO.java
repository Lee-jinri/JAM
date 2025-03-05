package com.jam.client.comReply.vo;


import lombok.Data;

@Data
public class ComReplyVO {
	private Long comReply_no; 			// 커뮤니티 댓글 번호
	private Long com_no; 				// 커뮤니티 글 번호
	private String comReply_content; 	// 커뮤니티 댓글 내용
	private String comReply_date; 		// 커뮤니티 댓글 작성일
	private String user_id; 			// 커뮤니티 댓글 작성자 id
	private String user_name; 			// 커뮤니티 댓글 작성자 닉네임
}
