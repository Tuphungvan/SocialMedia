package com.aht.social.application.service;

import com.aht.social.application.dto.event.NotificationEvent;
import com.aht.social.application.dto.request.post.CommentRequest;
import com.aht.social.application.dto.response.comment.CommentResponseDTO;
import com.aht.social.application.dto.response.common.PaginationResponse;
import com.aht.social.application.mapper.CommentMapper;
import com.aht.social.domain.entity.Comment;
import com.aht.social.domain.entity.Post;
import com.aht.social.domain.entity.User;
import com.aht.social.domain.enums.FriendshipStatus;
import com.aht.social.domain.enums.NotificationType;
import com.aht.social.domain.enums.PostPrivacy;
import com.aht.social.domain.repository.CommentRepository;
import com.aht.social.domain.repository.FriendshipRepository;
import com.aht.social.domain.repository.PostRepository;
import com.aht.social.domain.repository.UserRepository;
import com.aht.social.infrastructure.kafka.NotificationProducer;
import com.aht.social.presentation.exception.ResourceNotFoundException;
import com.aht.social.presentation.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Pageable;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final NotificationProducer notificationProducer;


    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new UnauthorizedException("Chưa đăng nhập");
        }

        try {
            UUID userId = UUID.fromString(auth.getName());
            return userRepository.findById(userId)
                    .orElseThrow(() -> new UnauthorizedException("User không tồn tại"));
        } catch (IllegalArgumentException ex) {
            throw new UnauthorizedException("Token không hợp lệ");
        }
    }

    private void validatePostAccess(Post post, User currentUser) {
        PostPrivacy privacy = post.getPrivacy();
        if (privacy == PostPrivacy.PUBLIC) {
            return;
        }

        if (currentUser == null) {
            throw new UnauthorizedException("Bạn cần đăng nhập để xem bài viết này.");
        }

        if (privacy == PostPrivacy.PRIVATE) {
            if (!post.getUser().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException("Đây là bài viết riêng tư");
            }
            return;
        }

        if (privacy == PostPrivacy.FRIENDS) {
            boolean isOwner = post.getUser().getId().equals(currentUser.getId());
            if (!isOwner) {
                boolean isFriend = friendshipRepository.existsByUserIdAndFriendIdAndStatus(
                        currentUser.getId(), post.getUser().getId(), FriendshipStatus.ACCEPTED);
                if (!isFriend) {
                    throw new AccessDeniedException(
                            "Chỉ bạn bè mới có thể xem bài viết này");
                }
            }
        }
    }

    @Transactional
    public CommentResponseDTO addComment(UUID postId, CommentRequest request) {
        User currentUser = getCurrentUser();
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài viết"));

        // 1. Kiểm tra quyền xem bài viết
        validatePostAccess(post, currentUser);

        // 2. Tạo Entity Comment
        Comment comment = new Comment();
        comment.setPost(post);
        comment.setUser(currentUser);
        comment.setContent(request.getContent());

        if (request.getParentCommentId() != null) {
            Comment parent = commentRepository.findById(request.getParentCommentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Bình luận cha không tồn tại"));
            comment.setParentComment(parent);

            // Tăng số lượng reply cho comment cha
            commentRepository.incrementRepliesCount(request.getParentCommentId());
        }

        Comment savedComment = commentRepository.save(comment);

        // 3. Tăng bộ đếm comment của Post (Atomic)
        postRepository.incrementCommentsCount(postId);

        // 4. Bắn Kafka Notification (Mục 1 - Event)
        // Thông báo cho chủ bài viết
        if (!currentUser.getId().equals(post.getUser().getId())) {
            notificationProducer.sendNotification(new NotificationEvent(
                    currentUser.getId(),
                    post.getUser().getId(),
                    NotificationType.COMMENT,
                    postId,
                    currentUser.getUsername() + " đã bình luận về bài viết của bạn."
            ));
        }

        CommentResponseDTO dto = commentMapper.toDTO(savedComment);
        dto.setLiked(false); // Comment mới tạo thì chắc chắn chưa ai like
        return dto;
    }

    @Transactional
    public PaginationResponse<CommentResponseDTO> getCommentsByPostId(UUID postId, Pageable pageable){
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài viết"));
        validatePostAccess(post, getCurrentUser());
        Page<Comment> commentPage = commentRepository.findTopLevelComments(postId, pageable);
        Page<CommentResponseDTO> responsePage = commentPage.map(comment -> {
            CommentResponseDTO dto = commentMapper.toDTO(comment);

            // Check xem user hiện tại đã like comment này chưa (Sẽ dùng LikeRepository để check)
            dto.setLiked(checkIfUserLikedComment(comment.getId(), getCurrentUser().getId()));

            return dto;
        });

        return PaginationResponse.from(responsePage);
    }
    // Hàm hỗ trợ check like (Có thể tối ưu bằng Redis sau này)
    private boolean checkIfUserLikedComment(UUID commentId, UUID userId) {
        // return likeRepository.existsByUserIdAndTargetId(userId, commentId);
        return false; // Tạm thời để false nếu chưa làm API Like Comment
    }
}
