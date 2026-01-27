package com.aht.social.domain.repository;

import com.aht.social.domain.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {

    @EntityGraph(attributePaths = {"user", "media"})
    Page<Post> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"user", "media"})
    @Query("SELECT p FROM Post p WHERE p.privacy = com.aht.social.domain.enums.PostPrivacy.PUBLIC")
    List<Post> findTopPublicPosts(Pageable pageable);

    @Modifying
    @Query("UPDATE Post p SET p.likesCount = CASE WHEN :isLike = true THEN p.likesCount + 1 ELSE p.likesCount - 1 END " +
            "WHERE p.id = :postId AND (:isLike = true OR p.likesCount > 0)")
    void updateLikesCount(@Param("postId") UUID postId, @Param("isLike") boolean isLike);

    @Transactional
    @Modifying
    @Query("UPDATE Post p SET p.sharesCount = p.sharesCount + 1 WHERE p.id = :postId")
    void updateSharesCount(@Param("postId") UUID postId);
}
