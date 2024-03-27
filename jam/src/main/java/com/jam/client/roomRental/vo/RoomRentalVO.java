package com.jam.client.roomRental.vo;

import com.jam.common.vo.CommonVO;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class RoomRentalVO  extends CommonVO{

	private Long roomRental_no;
	private String roomRental_title;
	private String roomRental_content;
	private int roomRental_status;
	private String imageFileName;
	private int roomRental_hits;
	private int roomRental_reply_cnt;
	private String roomRental_date;
	private int roomRental_price;
	
	private String city;
	private String gu;
	private String dong;
	
	private String user_id;
	private String user_name;
}                               