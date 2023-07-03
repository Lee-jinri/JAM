package com.jam.client.community.vo;

import org.springframework.web.multipart.MultipartFile;

import com.jam.common.vo.CommonVO;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class CommunityVO extends CommonVO{
	private int	com_no;
	private String com_title;
	private String com_content;
	private MultipartFile file;
	private String imageFileName;
	private int com_hits;
	private int com_reply_cnt;
	private String com_date;
	private String user_id;
	private String user_name;
}  