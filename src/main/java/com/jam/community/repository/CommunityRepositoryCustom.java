package com.jam.community.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.jam.community.dto.CommunityListResponseDto;

public interface CommunityRepositoryCustom {
	Page<CommunityListResponseDto> getMyPosts(String userId, String keyword, Pageable pageable);
}
