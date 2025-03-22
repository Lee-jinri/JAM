package com.jam.client.member.vo;

import com.jam.common.vo.CommonVO;

public class MemberBoardVO extends CommonVO {
	private Long favorite_id;
	private String user_id;
	private Long board_no;
	private String board_title;
	private BoardType board_type;
	private String created_at;
	
	public String getUser_id() {
		return user_id;
	}
	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}
	public String getBoard_title() {
		return board_title;
	}
	public void setBoard_title(String board_title) {
		this.board_title = board_title;
	}
	public String getCreated_at() {
		return created_at;
	}
	public void setCreated_at(String created_at) {
		this.created_at = created_at;
	}
	
}
