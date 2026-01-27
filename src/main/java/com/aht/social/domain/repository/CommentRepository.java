package com.aht.social.domain.repository;

import com.aht.social.domain.entity.Comment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {
    // Lấy comment cấp 1 của bài viết
    List<Comment> findByPostIdAndParentCommentIdIsNull(UUID postId, Pageable pageable);

    // Đếm số lượng phản hồi cho một comment
    int countByParentCommentId(UUID parentCommentId);

    // Cách tối ưu hơn: Query lấy map (commentId -> count) bằng JPQL
    @Query("SELECT c.parentComment.id, COUNT(c) FROM Comment c WHERE c.parentComment.id IN :ids GROUP BY c.parentComment.id")
    List<Object[]> countRepliesByCommentIds(@Param("ids") List<UUID> ids);

    void deleteByPostId(UUID postId);
}
