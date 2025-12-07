package com.jam.client.mypage.vo;

import com.jam.common.vo.CommonVO;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class MemberBoardVO extends CommonVO {
	
	private String user_id;
	private Long post_id;
	private String title;
	private String created_at;
	private Integer comment_count;
	private Integer view_count;
}
