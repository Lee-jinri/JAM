package com.jam.file.dto;

import java.time.LocalDateTime;

import org.apache.ibatis.type.Alias;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
@Alias("imageFile")
public class ImageFileDto {
	private Long image_id;
	private String image_name;
	private Long post_id;
    private String post_type; 
    
	@JsonFormat(pattern = "yyyy/MM/dd HH:mm")
	private LocalDateTime created_at;
}