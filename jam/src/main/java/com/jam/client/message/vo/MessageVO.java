package com.jam.client.message.vo;

import com.jam.common.vo.CommonVO;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class MessageVO extends CommonVO{
	private Long 	message_no;			// 메세지 번호
	private String 	sender;				// 보낸 사람
	private String 	sender_id;			// 보낸 사람 아이디
	private String 	receiver;			// 받는 사람
	private String 	receiver_id;		// 받는 사람 아이디
	private String 	message_title;		// 메세지 제목
	private String 	message_contents;	// 메세지 내용
	private String 	sendTime;			// 보낸 시간
	private String 	readTime;			// 읽은 시간
	private int		read_chk;			// 메세지 확인 여부
}             