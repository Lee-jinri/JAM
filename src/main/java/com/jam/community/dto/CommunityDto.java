package com.jam.community.dto;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.type.Alias;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jam.common.dto.CommonDto;
import com.jam.file.dto.FileAssetDto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@Alias("community")
public class CommunityDto extends CommonDto{
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

	private List<FileAssetDto> file_assets;
	private List<String> deleted_keys;
}
