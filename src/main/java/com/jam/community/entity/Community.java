package com.jam.community.entity;

import java.time.LocalDateTime;

import com.jam.member.entity.Member;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "community")
@Getter
@Setter
public class Community {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "community_seq")
	@SequenceGenerator(name = "community_seq", sequenceName = "SEQ_COMMUNITY", allocationSize = 1)
	@Column(name = "post_id")
	private Long postId;
	
	@Column(name = "title")
	private String title;

	@Column(name = "content")
	private String content;

	@Column(name = "view_count")
	private int viewCount;

	@Column(name = "comment_count")
	private int commentCount;

	@Column(name = "created_at")
	private LocalDateTime createdAt;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private Member member;
}
