package com.jam.client.community.vo;


import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jam.common.vo.CommonVO;
import com.jam.file.vo.FileAssetVO;

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
	
	@JsonFormat(pattern = "yyyy/MM/dd HH:mm")
	private LocalDateTime created_at;
	
	private String user_id;			
	private String user_name;		
	
	private Integer popularity_score; // 인기글 점수

	private List<FileAssetVO> file_assets;
	private List<String> deleted_keys;
}  