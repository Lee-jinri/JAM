package com.jam.file.vo;

import java.sql.Timestamp;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class FileAssetVO {
	private Long file_id;
	private Long post_id;
	private String post_type;
	
	@NotBlank @Size(max=500)
	private String file_key;
	
	@NotBlank @Size(max=255)
	private String file_name;
	
	@NotBlank
	private String file_type;
	
	@NotNull @Positive
	private Long file_size;
	private Timestamp created_at;
}