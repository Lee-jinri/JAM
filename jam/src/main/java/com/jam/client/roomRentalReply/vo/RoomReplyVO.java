package com.jam.client.roomRentalReply.vo;

import lombok.Data;

@Data
public class RoomReplyVO {

	private Long roomReply_no;			// 합주실/연습실 댓글 번호
	private Long roomRental_no; 			//합주실/연습실 글 번호
	private String roomReply_content; 	// 합주실/연습실 댓글 내용
	private String roomReply_date; 		//합주실/연습실 댓글 작성일
	private String user_id; 			// 합주실/연습실 댓글 작성자 id
	private String user_name; 			//합주실/연습실 댓글 작성자 닉네임
}