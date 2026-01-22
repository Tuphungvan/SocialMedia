package com.aht.social.domain.repository;

import com.aht.social.domain.entity.Like;
import com.aht.social.domain.enums.TargetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LikeRepository extends JpaRepository<Like, UUID> {
    Optional<Like> findByUserIdAndTargetTypeAndTargetId(UUID userId, TargetType targetType, UUID targetId);
    boolean existsByUserIdAndTargetTypeAndTargetId(UUID userId, TargetType targetType, UUID targetId);
    List<Like> findByUserIdAndTargetTypeAndTargetIdIn(UUID userId, TargetType targetType, List<UUID> targetIds);
}
