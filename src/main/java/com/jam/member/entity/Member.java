package com.jam.member.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "member")
@Getter
@Setter
public class Member {

	@Id
    @Column(name = "user_id")
    private String userId;
	
	@Column(name = "user_pw", nullable = false)
    private String userPw;
	
	@Column(name = "user_name", nullable = false, unique = true)
    private String userName;
	
	@Column(name = "address")
    private String address;

    @Column(name = "phone", unique = true)
    private String phone;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "refreshToken")
    private String refreshToken;

    @Column(name = "social_login")
    private Integer socialLogin;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
