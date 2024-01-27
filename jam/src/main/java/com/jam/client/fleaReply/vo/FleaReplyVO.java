package com.jam.client.fleaReply.vo;

import lombok.Data;

@Data
public class FleaReplyVO {
	private Long fleaReply_no; 			// 중고거래 댓글 번호
	private Long flea_no; 				// 중고거래 글 번호
	private String fleaReply_content; 	// 중고거래 댓글 내용
	private String fleaReply_date; 		// 중고거래 댓글 작성일
	private String user_id; 			// 중고거래 댓글 작성자 id
	private String user_name; 			// 중고거래 댓글 작성자 닉네임
}
