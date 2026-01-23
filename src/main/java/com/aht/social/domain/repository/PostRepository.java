package com.aht.social.domain.repository;

import com.aht.social.domain.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {

    @EntityGraph(attributePaths = {"user", "media"})
    Page<Post> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"user", "media"})
    @Query("SELECT p FROM Post p WHERE p.privacy = com.aht.social.domain.enums.PostPrivacy.PUBLIC")
    List<Post> findTopPublicPosts(Pageable pageable);
}
