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
	private int job_category; // 0:구인 / 1:구직
	private int job_status;   // 0:구인,구직 중 /1:구인,구직 완료
	private String imageFileName;
	private int job_hits;
	private int job_reply_cnt;
	private String job_date;
	private int pay_category; // 0:일급 /1:주급 / 2:월급
	private int pay;
	private String user_id;
	private String user_name;
}