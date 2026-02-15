package com.jam.file.dto;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.type.Alias;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Alias("file")
public class FileAssetDto {
	private Long file_id;
	private Long post_id;
	private String post_type;
	
	@NotBlank @Size(max=500)
	private String file_key;
	
	private List<String> file_keys;
	
	@NotBlank @Size(max=255)
	private String file_name;
	
	@NotBlank
	private String file_type;
	
	@NotNull @Positive
	private Long file_size;
	
	@JsonFormat(pattern = "yyyy/MM/dd HH:mm")
	private LocalDateTime created_at;
	
	private FileCategory file_category;
}
