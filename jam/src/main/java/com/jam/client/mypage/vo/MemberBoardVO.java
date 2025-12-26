package com.jam.client.mypage.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jam.common.vo.CommonVO;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class MemberBoardVO extends CommonVO {
	
	private String user_id;
	private Long post_id;
	private String title;
	private Integer comment_count;
	private Integer view_count;
	
	@JsonFormat(pattern = "yyyy/MM/dd HH:mm")
	private String created_at;
}
