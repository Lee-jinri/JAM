package com.jam.client.roomRental.vo;

import com.jam.common.vo.CommonVO;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class RoomRentalVO  extends CommonVO{

	private Long post_id;
	private String title;
	private String content;
	private int status;
	private int view_count;
	private int comment_count;
	private String created_at;
	private int price;
	private int duration;
	
	private String user_id;
	private String user_name;
	
	private String imageFileName;
}                               