package com.jam.file.vo;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class ImageFileVO {
	private Long image_id;
	private String image_name;
	private Long post_id;
    private String post_type; 
    
	@JsonFormat(pattern = "yyyy/MM/dd HH:mm")
	private LocalDateTime created_at;
}