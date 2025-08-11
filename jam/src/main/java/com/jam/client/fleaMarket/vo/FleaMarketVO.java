package com.jam.client.fleaMarket.vo;

import com.jam.common.vo.CommonVO;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class FleaMarketVO extends CommonVO {
	private Long post_id;			// 글 번호
	private String title;			// 제목
	private String content;			// 설명
	private int category_id;		
	private int sales_status;		// 거래 완료 여부 0: 거래중 1: 거래완료
	private int view_count;			// 조회수
	private String created_at;		// 작성일
	private int price;				// 가격
	private String user_id;			// 작성자 id
	private String user_name;		// 작성자 닉네임
	private String thumbnail;
}
