package com.jam.community.repository;

import static com.jam.community.entity.QCommunity.community;
import static com.jam.member.entity.QMember.member;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import com.jam.community.dto.CommunityListResponseDto;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CommunityRepositoryImpl implements CommunityRepositoryCustom {

	private final JPAQueryFactory queryFactory;
	
	@Override
	public Page<CommunityListResponseDto> getMyPosts(String userId, String keyword, Pageable pageable) {
		
		List<CommunityListResponseDto> content = queryFactory
                .select(Projections.constructor(CommunityListResponseDto.class,
                        community.postId,
                        community.title,
                        community.createdAt,
                        community.commentCount,
                        community.viewCount,
                        member.userId,
                        member.userName))
                .from(community)
                .join(member).on(community.member.userId.eq(member.userId))
                .where(
                        community.member.userId.eq(userId),
                        titleContains(keyword)
                )
                .orderBy(community.postId.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        	Long total = queryFactory
                .select(community.count())
                .from(community)
                .where(
                        community.member.userId.eq(userId),
                        titleContains(keyword)
                )
                .fetchOne();
        	
        	long totalCount = (total != null) ? total : 0L;
        return new PageImpl<>(content, pageable, totalCount);
	}
	
	private BooleanExpression titleContains(String keyword) {
	    return StringUtils.hasText(keyword) ? community.title.containsIgnoreCase(keyword) : null;
	}
}
