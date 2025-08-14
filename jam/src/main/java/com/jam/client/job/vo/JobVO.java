package com.jam.client.job.vo;

import java.util.List;

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
		
	private String created_at; 
	private Integer hits;
	private Integer pay_category; // 0:시급 1:월급 
	private Integer pay;
	private String position;
	private List<String> positions;
		
	private String user_id;
	private String user_name;
	
	private boolean isAuthor;
}