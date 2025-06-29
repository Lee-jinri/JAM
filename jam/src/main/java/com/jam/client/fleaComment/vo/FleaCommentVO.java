package com.jam.client.fleaComment.vo;

import lombok.Data;

@Data
public class FleaCommentVO {
	private Long comment_id;
	private Long post_id;
    private String content;
    private String created_at;
    private String user_id;
    private String user_name;  
}
