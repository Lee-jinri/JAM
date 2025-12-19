package com.jam.client.job.vo;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jam.common.vo.CommonVO;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class JobVO extends CommonVO {
	private Long post_id;
	private String title;  
	private String content; 
	
	private int category; // 0:기업 구인 1: 멤버 구인
	private int status;   // 0:구인중 1:구인 완료
	private Integer view_count;
	private Integer pay_category; // 0:건당 1:시급 2:월급
	private Integer pay;
	private String position;
	private List<String> positions;
	
	@JsonFormat(pattern = "yyyy/MM/dd HH:mm")
	private LocalDateTime created_at; 
		
	private String user_id;
	private String user_name;
	
	private String company_name;
	private boolean isAuthor;
	
	private int applyCount;
}