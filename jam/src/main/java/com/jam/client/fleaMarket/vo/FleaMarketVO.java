package com.jam.client.fleaMarket.vo;

import com.jam.common.vo.CommonVO;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class FleaMarketVO extends CommonVO {
	private Long flea_no;			// 글 번호
	private String flea_title;		// 제목
	private String flea_content;	// 내용
	private int flea_category;		// 0: 판매 1: 구매
	private int sales_status;		// 거래 완료 여부 0: 거래중 1: 거래완료
	private String imageFileName;	// 사진 파일 이름
	private int flea_hits;			// 조회수
	private int flea_reply_cnt;		// 댓글 수
	private String flea_date;		// 작성일
	private int price;				// 가격
	private String user_id;			// 작성자 id
	private String user_name;		// 작성자 닉네임
}
