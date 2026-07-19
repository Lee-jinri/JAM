package com.jam.community.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.jam.community.entity.Community;

public interface CommunityRepository extends JpaRepository<Community, Long>, CommunityRepositoryCustom  {
	@Modifying
    @Query("UPDATE Community c SET c.viewCount = c.viewCount + 1 WHERE c.postId = :postId")
    void incrementViewCount(@Param("postId") Long postId);
	
	@Modifying
	@Query("UPDATE Community c SET c.commentCount = c.commentCount + :amount WHERE c.postId = :postId")
	void updateCommentCount(@Param("postId") Long postId, @Param("amount") int amount);
	
	@Modifying
	@Query("DELETE FROM Community c WHERE c.member.userId = :userId AND c.postId IN :postIds")
	void deleteMyPosts(@Param("userId") String userId, @Param("postIds") List<Long> postIds);

	long countByPostIdInAndMember_UserId(List<Long> postIds, String userId);
}
