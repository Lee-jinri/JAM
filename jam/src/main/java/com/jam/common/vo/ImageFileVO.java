package com.jam.common.vo;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ImageFileVO {
	private Long image_no;
	private String image_name;
	private LocalDateTime created_at;
	private Long post_id;
    private String post_type; 
}