package com.jam.client.community.vo;


import com.jam.common.vo.CommonVO;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class CommunityVO extends CommonVO{
	
	/* 커뮤니티 게시판 */
	private Long post_id;
	private String title;
	private String content;
	
	private int view_count;
	private int comment_count;
	private String created_at;
	
	private String user_id;			
	private String user_name;		
	
	private String imageFileName;	// 사진 파일
}  