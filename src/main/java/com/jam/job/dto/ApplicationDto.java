package com.jam.job.dto;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.type.Alias;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jam.common.dto.CommonDto;
import com.jam.file.dto.FileAssetDto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@Alias("app")
public class ApplicationDto extends CommonDto {
	private Long application_id;
	private String user_id;
	
	@NotNull @Positive
	private Long post_id;
	
	@NotBlank @Size(max = 120)
	private String title;
	
	@Size(max = 800)
	private String content;
	
	@JsonFormat(pattern = "yyyy/MM/dd HH:mm")
	private LocalDateTime created_at;
	
	@Valid
	private List<FileAssetDto> file_assets;
	
	private String company_user_id;
	
	private int category; 
	
	private Integer job_status;
}
