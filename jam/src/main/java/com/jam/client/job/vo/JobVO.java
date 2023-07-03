package com.jam.client.job.vo;

import com.jam.common.vo.CommonVO;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class JobVO extends CommonVO {

	private int job_no;
	private String job_title;
	private String job_content;
	private int job_category; // 1:구인 / 2:구직
	private int job_status;   // 1:구인,구직 중 /2:구인,구직 완료
	private String imageFileName;
	private int job_hits;
	private int job_reply_cnt;
	private String job_date;
	private int pay_category; // 1:일급 /2:주급 / 3:월급
	private int pay;
	private String user_id;
	private String user_name;
}