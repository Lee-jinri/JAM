package com.jam.client.job.vo;

import java.sql.Timestamp;
import java.util.List;

import com.jam.file.vo.FileAssetVO;

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
public class ApplicationVO {
	private Long application_id;
	private String user_id;
	
	@NotNull @Positive
	private Long post_id;
	
	@NotBlank @Size(max = 120)
	private String title;
	
	@Size(max = 800)
	private String content;
	private Timestamp created_at;
	
	@NotEmpty @Valid
	private List<FileAssetVO> file_assets;
	
	private String company_id;
}