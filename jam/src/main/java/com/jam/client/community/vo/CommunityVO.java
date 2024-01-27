package com.jam.client.community.vo;


import com.jam.common.vo.CommonVO;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class CommunityVO extends CommonVO{
	
	/* 커뮤니티 게시판 */
	private Long com_no;				// 글 번호
	private String com_title;		// 제목
	private String com_content;		// 내용
	private String imageFileName;	// 사진 파일 이름
	private int com_hits;			// 조회수
	private int com_reply_cnt;		// 댓글 수
	private String com_date;		// 작성일
	private String user_id;			// 작성자 id
	private String user_name;		// 작성자 닉네임
}  