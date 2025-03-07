package com.jam.client.job.vo;

import java.util.List;

import com.jam.common.vo.CommonVO;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class JobVO extends CommonVO {

	private Long job_no;
	private String job_title;
	private String job_content;
	private int job_category; // 0:기업 구인 1: 멤버 구인
	private int job_status;   // 0:구인중 1:구인 완료
	private String imageFileName;
	private int job_hits;
	private int job_reply_cnt;
	private String job_date;
	private Integer pay_category; // 0:시급 1:월급 
	private Integer pay;
	private String position;
	private List<String> positions;
	
	private String city;
	private String gu;
	private String dong;
	
	private String user_id;
	private String user_name;
}