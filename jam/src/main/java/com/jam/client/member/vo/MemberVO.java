package com.jam.client.member.vo;

import com.jam.common.vo.CommonVO;

import lombok.Data;

@Data
public class MemberVO extends CommonVO{
	private String user_id = "";		// 아이디
	private String user_pw = "";		// 비밀번호
	private String user_name = "";	// 닉네임
	private String phone = "";		// 전화번호
	private String address = "";	// 주소
}