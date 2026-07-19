package com.jam.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jam.member.entity.Member;

public interface MemberRepository extends JpaRepository<Member, String>{

}
