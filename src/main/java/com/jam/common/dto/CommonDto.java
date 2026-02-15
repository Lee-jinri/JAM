package com.jam.common.dto;

import lombok.Data;

@Data
public class CommonDto {
	private int pageNum = 0;
	private int amount = 0;
	
	private String search_category = "";
	private String search = "";
	private String keyword = "";
	
	private String city;
	private String gu;
	private String dong;
	
	private String start_date = "";
	private String end_date = "";
	
	private int favorite_id;
	private boolean isFavorite;  // 즐겨찾기 여부
	
	private String board_type; // community, roomRental, job, fleaMarket
	
	private String period;
	
	public CommonDto() {
		this(1, 10);
	}
	
	public CommonDto(int pageNum, int amount) {
		this.pageNum = pageNum;
		this.amount = amount;
	}
}
