package com.jam.client.fleaMarket.vo;

import com.jam.common.vo.CommonVO;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class FleaMarketVO extends CommonVO {
	private int flea_no;
	private String flea_title;
	private String flea_content;
	private int flea_category;
	private int sales_status;
	private String imageFileName;
	private int flea_hits;
	private int flea_reply_cnt;
	private String flea_date;
	private int price;
	private String user_id;
	private String user_name;
}
