package com.jam.client.job.vo;

import java.time.LocalDateTime;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jam.common.vo.CommonVO;
import com.jam.file.vo.FileAssetVO;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class ApplicationVO extends CommonVO{
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
	
	@NotEmpty @Valid
	private List<FileAssetVO> file_assets;
	
	private String company_user_id;
	
	private int category; 
	
	private Integer job_status;
}