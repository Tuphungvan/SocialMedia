package com.aht.social.domain.repository;

import com.aht.social.domain.entity.Friendship;
import com.aht.social.domain.enums.FriendshipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, UUID> {
    @Query("SELECT COUNT(f) > 0 FROM Friendship f WHERE " +
            "((f.user.id = :userId AND f.friend.id = :friendId) OR " +
            "(f.user.id = :friendId AND f.friend.id = :userId)) " +
            "AND f.status = 'ACCEPTED'")
    boolean existsByUserIdAndFriendIdAndStatus(UUID userId, UUID friendId, FriendshipStatus status);
}
