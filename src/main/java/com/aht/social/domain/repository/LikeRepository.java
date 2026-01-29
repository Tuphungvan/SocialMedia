package com.aht.social.domain.repository;

import com.aht.social.domain.entity.Like;
import com.aht.social.domain.entity.User;
import com.aht.social.domain.enums.TargetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LikeRepository extends JpaRepository<Like, UUID> {
    boolean existsByUserIdAndTargetTypeAndTargetId(UUID userId, TargetType targetType, UUID targetId);
    List<Like> findByUserIdAndTargetTypeAndTargetIdIn(UUID userId, TargetType targetType, List<UUID> targetIds);
    void deleteByTargetTypeAndTargetId(TargetType targetType, UUID targetId);

    //Kiểm tra xem người dùng đã like hay chưa
    Optional<Like> findByUserIdAndTargetTypeAndTargetId(UUID userId, TargetType targetType, UUID targetId);
    // Xóa khi người dùng unlike
    //Lấy danh sách like phân trang
    @Query("SELECT l.user FROM Like l WHERE l.targetId = :targetId AND l.targetType = 'POST' ORDER BY l.createdAt DESC")
    Page<User> findUserByPostId(@Param("targetId") UUID targetId, Pageable pageable);
}
