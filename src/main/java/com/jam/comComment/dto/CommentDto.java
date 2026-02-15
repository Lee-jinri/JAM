package com.jam.comComment.dto;

import java.time.LocalDateTime;
import org.apache.ibatis.type.Alias;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

@Data
@Alias("comComment")
public class CommentDto {
	private Long comment_id; 
	private Long post_id;
	private String content;
	private String user_id;
	private String user_name;

	@JsonFormat(pattern = "yyyy/MM/dd HH:mm")
	private LocalDateTime created_at;
	
	private boolean isAuthor;
}
