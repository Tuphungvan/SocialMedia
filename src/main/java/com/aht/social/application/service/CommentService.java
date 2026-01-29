package com.aht.social.application.service;

import com.aht.social.application.dto.event.NotificationEvent;
import com.aht.social.application.dto.request.comment.CommentRequestDTO;
import com.aht.social.application.dto.request.comment.CommentUpdateRequest;
import com.aht.social.application.dto.response.comment.CommentResponseDTO;
import com.aht.social.application.dto.response.common.PaginationResponse;
import com.aht.social.application.mapper.CommentMapper;
import com.aht.social.domain.entity.Comment;
import com.aht.social.domain.entity.Like;
import com.aht.social.domain.entity.Post;
import com.aht.social.domain.entity.User;
import com.aht.social.domain.enums.FriendshipStatus;
import com.aht.social.domain.enums.NotificationType;
import com.aht.social.domain.enums.PostPrivacy;
import com.aht.social.domain.enums.TargetType;
import com.aht.social.domain.repository.*;
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

import java.util.Optional;
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
    private final LikeRepository likeRepository;

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

    // Hàm hỗ trợ check like (Có thể tối ưu bằng Redis sau này)
    private boolean checkIfUserLikedComment(UUID commentId, UUID userId) {
        return likeRepository.existsByUserIdAndTargetTypeAndTargetId(
                userId, TargetType.COMMENT, commentId);
    }

    @Transactional
    public CommentResponseDTO addComment(UUID postId, CommentRequestDTO request) {
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
    public PaginationResponse<CommentResponseDTO> getCommentsByPostId(UUID postId, Pageable pageable) {
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

    @Transactional
    public CommentResponseDTO updateComment(UUID postId, UUID commentId, CommentUpdateRequest request) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay binh luan"));
        // Kiểm tra comment có thuộc postId truyền vào không
        if (!comment.getPost().getId().equals(postId)) {
            throw new AccessDeniedException("Bình luận không thuộc bài viết này");
        }
        User user = getCurrentUser();
        if (!comment.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Ban khong co quyen sua binh luan nay");
        }
        comment.setContent(request.getContent());
        comment.setEdited(true);

        Comment updatedComment = commentRepository.save(comment);
        CommentResponseDTO dto = commentMapper.toDTO(updatedComment);
        dto.setLiked(checkIfUserLikedComment(commentId, user.getId()));
        return dto;
    }

    @Transactional
    public void deleteComment(UUID postId, UUID commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bình luận"));

        if (!comment.getPost().getId().equals(postId)) {
            throw new AccessDeniedException("Bình luận không thuộc bài viết này");
        }

        User currentUser = getCurrentUser();
        Post post = comment.getPost();

        // 1. Kiểm tra quyền xóa (Chủ comment hoặc Chủ bài viết)
        boolean isCommentOwner = comment.getUser().getId().equals(currentUser.getId());
        boolean isPostOwner = post.getUser().getId().equals(currentUser.getId());

        if (!isCommentOwner && !isPostOwner) {
            throw new AccessDeniedException("Bạn không có quyền xóa bình luận này");
        }

        // 2. Tính toán số lượng comment sẽ bị xóa khỏi Post
        // Tổng xóa = 1 (chính nó) + số lượng con của nó
        int totalDeleted = 1 + comment.getRepliesCount();

        // 3. Cập nhật bộ đếm của Post
        postRepository.decrementCommentsCountByAmount(post.getId(), totalDeleted);

        // 4. Nếu comment này là một Reply, phải giảm repliesCount của cha nó
        if (comment.getParentComment() != null) {
            commentRepository.decrementRepliesCount(comment.getParentComment().getId());
        }

        // 5. Thực hiện xóa trong Database
        // Vì bạn đã có index idx_comment_parent, việc xóa này sẽ rất nhanh
        commentRepository.deleteByParentCommentId(commentId); // Xóa các con
        commentRepository.delete(comment); // Xóa chính nó
    }

    @Transactional
    public void toggleLikeComment(UUID postId, UUID commentId) {
        User currentUser = getCurrentUser();
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bình luận"));

        if (!comment.getPost().getId().equals(postId)) {
            throw new AccessDeniedException("Bình luận không thuộc bài viết này");
        }

        Optional<Like> existingLike = likeRepository.findByUserIdAndTargetTypeAndTargetId(
                currentUser.getId(), TargetType.COMMENT, commentId);
        if (existingLike.isPresent()) {
            // 1. Nếu đã like -> Xóa like (Unlike)
            likeRepository.delete(existingLike.get());
            commentRepository.decrementLikesCount(commentId);
        } else {
            // 2. Nếu chưa like -> Tạo mới like
            Like like = new Like();
            like.setUser(currentUser);
            like.setTargetId(commentId);
            like.setTargetType(TargetType.COMMENT);
            likeRepository.save(like);

            commentRepository.incrementLikesCount(commentId);

            // 3. Kafka Notification (Mục 1 - Event)
            // Không thông báo nếu chủ comment tự like comment của mình
            if (!currentUser.getId().equals(comment.getUser().getId())) {
                notificationProducer.sendNotification(new NotificationEvent(
                        currentUser.getId(),
                        comment.getUser().getId(),
                        NotificationType.LIKE, // Bạn có thể dùng chung LIKE hoặc tạo LIKE_COMMENT
                        comment.getPost().getId(), // Click vào thông báo sẽ dẫn về bài viết
                        currentUser.getUsername() + " đã thích bình luận của bạn."
                ));
            }
        }
    }

    @Transactional
    public CommentResponseDTO replyComment(UUID postId, UUID parentId, CommentRequestDTO request) {
        User currentUser = getCurrentUser();

        // 1. Tìm comment cha
        Comment parentComment = commentRepository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException("Bình luận gốc không tồn tại"));

        Post post = parentComment.getPost();

        if (!parentComment.getPost().getId().equals(postId)) {
            throw new AccessDeniedException("Bình luận không thuộc bài viết này");
        }
        // 2. Tạo reply mới
        Comment reply = new Comment();
        reply.setPost(post);
        reply.setUser(currentUser);
        reply.setContent(request.getContent());
        reply.setParentComment(parentComment);

        Comment savedReply = commentRepository.save(reply);

        // 3. Cập nhật các bộ đếm (Atomic)
        commentRepository.incrementRepliesCount(parentId);
        postRepository.incrementCommentsCount(post.getId());

        // 4. Bắn Kafka Notifications (Đa tầng)
        // Thông báo cho chủ bài viết
        if (!currentUser.getId().equals(post.getUser().getId())) {
            notificationProducer.sendNotification(new NotificationEvent(
                    currentUser.getId(), post.getUser().getId(), NotificationType.COMMENT,
                    post.getId(), currentUser.getUsername() + " đã bình luận về bài viết của bạn."
            ));
        }

        // Thông báo cho chủ comment cha (Người bị reply)
        if (!currentUser.getId().equals(parentComment.getUser().getId())
                && !parentComment.getUser().getId().equals(post.getUser().getId())) {
            notificationProducer.sendNotification(new NotificationEvent(
                    currentUser.getId(), parentComment.getUser().getId(), NotificationType.REPLY_COMMENT,
                    post.getId(), currentUser.getUsername() + " đã phản hồi bình luận của bạn."
            ));
        }

        return commentMapper.toDTO(savedReply);
    }

    @Transactional(readOnly = true)
    public PaginationResponse<CommentResponseDTO> getRepliesByCommentId(
            UUID postId, UUID commentId, Pageable pageable){
        Comment parentComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Bình luận không tồn tại"));

        if (!parentComment.getPost().getId().equals(postId)) {
            throw new AccessDeniedException("Bình luận không thuộc bài viết này");
        }

        Page<Comment> repliesPage = commentRepository.findRepliesByParentId(commentId, pageable);
        User currentUser = getCurrentUser();
        Page<CommentResponseDTO> responsePage = repliesPage.map(reply -> {
            CommentResponseDTO dto = commentMapper.toDTO(reply);
            dto.setLiked(checkIfUserLikedComment(reply.getId(), currentUser.getId()));
            return dto;
        });

        return PaginationResponse.from(responsePage);
    }
}
