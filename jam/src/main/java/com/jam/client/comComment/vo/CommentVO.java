package com.jam.client.comComment.vo;


import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class CommentVO {
	private Long comment_id; 			// 커뮤니티 댓글 번호
	private Long post_id; 				// 커뮤니티 글 번호
	private String content; 			// 커뮤니티 댓글 내용
	private String user_id; 			// 커뮤니티 댓글 작성자 id
	private String user_name; 			// 커뮤니티 댓글 작성자 닉네임

	@JsonFormat(pattern = "yyyy/MM/dd HH:mm")
	private LocalDateTime created_at; 	// 커뮤니티 댓글 작성일
	
	private boolean isAuthor;
}