package com.aht.social.domain.repository;

import com.aht.social.domain.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {
    // Lấy comment cấp 1 của bài viết
    List<Comment> findByPostIdAndParentCommentIdIsNull(UUID postId, Pageable pageable);

    //Query lấy map (commentId -> count) bằng JPQL
    @Query("SELECT c.parentComment.id, COUNT(c) FROM Comment c WHERE c.parentComment.id IN :ids GROUP BY c.parentComment.id")
    List<Object[]> countRepliesByCommentIds(@Param("ids") List<UUID> ids);

    void deleteByPostId(UUID postId);

    @Transactional
    @Modifying
    @Query("UPDATE Comment c SET c.repliesCount = c.repliesCount + 1 WHERE c.id = :commentId")
    void incrementRepliesCount(@Param("commentId") UUID commentId);

    // Lấy comment cấp 1 (không có parent) của một bài viết
    @Query("SELECT c FROM Comment c JOIN FETCH c.user WHERE c.post.id = :postId AND c.parentComment IS NULL ORDER BY c.createdAt DESC")
    Page<Comment> findTopLevelComments(@Param("postId") UUID postId, Pageable pageable);

    // Lấy các phản hồi (replies) của một comment cụ thể
    @Query("SELECT c FROM Comment c JOIN FETCH c.user WHERE c.parentComment.id = :parentId ORDER BY c.createdAt ASC")
    Page<Comment> findReplies(@Param("parentId") UUID parentId, Pageable pageable);

    // Xóa tất cả các reply con khi comment cha bị xóa
    void deleteByParentCommentId(UUID parentId);

    @Transactional
    @Modifying
    @Query("UPDATE Comment c SET c.repliesCount = c.repliesCount - 1 WHERE c.id = :parentId AND c.repliesCount > 0")
    void decrementRepliesCount(@Param("parentId") UUID parentId);

    @Transactional
    @Modifying
    @Query("UPDATE Comment c SET c.likesCount = c.likesCount + 1 WHERE c.id = :id")
    void incrementLikesCount(@Param("id") UUID id);

    @Transactional
    @Modifying
    @Query("UPDATE Comment c SET c.likesCount = c.likesCount - 1 WHERE c.id = :id AND c.likesCount > 0")
    void decrementLikesCount(@Param("id") UUID id);

    @Query("SELECT c FROM Comment c WHERE c.parentComment.id = :parentId ORDER BY c.createdAt ASC")
    Page<Comment> findRepliesByParentId(@Param("parentId") UUID parentId, Pageable pageable);
}
