package com.aht.social.application.service;

import com.aht.social.application.dto.request.post.CreatePostRequestDTO;
import com.aht.social.application.dto.request.post.PostMediaRequestDTO;
import com.aht.social.application.dto.request.post.UpdatePostRequestDTO;
import com.aht.social.application.dto.response.comment.CommentResponseDTO;
import com.aht.social.application.dto.response.common.PaginationResponse;
import com.aht.social.application.dto.response.post.PostDetailResponseDTO;
import com.aht.social.application.dto.response.post.PostResponseDTO;
import com.aht.social.application.mapper.CommentMapper;
import com.aht.social.application.mapper.PostMapper;
import com.aht.social.application.mapper.PostMediaMapper;
import com.aht.social.domain.entity.*;
import com.aht.social.domain.enums.FriendshipStatus;
import com.aht.social.domain.enums.PostPrivacy;
import com.aht.social.domain.enums.TargetType;
import com.aht.social.domain.repository.*;
import com.aht.social.presentation.exception.ResourceNotFoundException;
import com.aht.social.presentation.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.security.access.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {
    private final PostRepository postRepository;
    private final PostMapper postMapper;
    private final UserRepository userRepository;
    private final PostMediaMapper postMediaMapper;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final FriendshipRepository friendshipRepository;

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

    @Transactional
    public PostResponseDTO createPost(CreatePostRequestDTO requestDTO) {
        User currentUser = getCurrentUser();

        Post post = postMapper.toCreateEntity(requestDTO);
        post.setUser(currentUser);
        post.setEdited(false);

        if (requestDTO.getMedia() != null) {
            List<PostMedia> mediaEntities = new ArrayList<>();
            requestDTO.getMedia().forEach(dto -> {
                PostMedia media = postMediaMapper.toEntity(dto);
                media.setPost(post);
                mediaEntities.add(media);
            });
            post.setMedia(mediaEntities);
        }
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        Post saved = postRepository.save(post);
        PostResponseDTO response = postMapper.toDTO(saved);
        //set like false cho post mới tạo
        response.setLiked(false);
        return response;
    }

    private void setLikedStatusForPosts(List<PostResponseDTO> posts, UUID userId) {
        if (posts.isEmpty()) {
            return;
        }

        //Lay danh sach post IDs
        List<UUID> postIds = posts.stream()
                .map(PostResponseDTO::getId)
                .toList();

        //Query all likes of user for posts in one
        List<Like> likes = likeRepository.findByUserIdAndTargetTypeAndTargetIdIn
                (userId, TargetType.POST, postIds);

        //create set have post IDs was liked
        Set<UUID> likedPostIds = likes.stream()
                .map(Like::getTargetId)
                .collect(Collectors.toSet());

        //set isLiked for each post
        posts.forEach(post -> {
            post.setLiked(likedPostIds.contains(post.getId()));
        });
    }

    @Transactional(readOnly = true)
    public PaginationResponse<PostResponseDTO> getNewsfeed(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.max(size, 1);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = auth != null && auth.isAuthenticated()
                && !"anonymousUser".equals(auth.getPrincipal());

        List<PostResponseDTO> postDTOs;
        long totalElements;

        if (isAuthenticated) {
            // --- TRƯỜNG HỢP ĐÃ ĐĂNG NHẬP: Lấy theo thời gian mới nhất (Newsfeed chuẩn) ---
            Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<Post> postPage = postRepository.findAll(pageable);

            totalElements = postPage.getTotalElements();
            postDTOs = postPage.getContent().stream()
                    .map(postMapper::toDTO)
                    .collect(Collectors.toList());

            // Check Like
            User currentUser = getCurrentUser();
            setLikedStatusForPosts(postDTOs, currentUser.getId());

        } else {
            // --- TRƯỜNG HỢP CHƯA ĐĂNG NHẬP: Shuffle ngẫu nhiên bài PUBLIC ---
            // Lấy nhiều hơn một chút (ví dụ lấy 50 bài) để shuffle cho sướng
            Pageable limitProvider = PageRequest.of(0, 50, Sort.by(Sort.Direction.DESC, "createdAt"));
            List<Post> publicPosts = postRepository.findTopPublicPosts(limitProvider);

            // Xáo trộn danh sách
            Collections.shuffle(publicPosts);

            // Cắt danh sách theo size và page (Phân trang thủ công trên list đã shuffle)
            int start = Math.min(safePage * safeSize, publicPosts.size());
            int end = Math.min(start + safeSize, publicPosts.size());

            List<Post> pagedList = publicPosts.subList(start, end);
            totalElements = publicPosts.size();

            postDTOs = pagedList.stream()
                    .map(postMapper::toDTO)
                    .peek(dto -> dto.setLiked(false)) // Khách thì chắc chắn chưa like
                    .collect(Collectors.toList());
        }

        // Đóng gói vào PageImpl để PaginationResponse.from() hoạt động được
        Page<PostResponseDTO> resultPage = new PageImpl<>(
                postDTOs,
                PageRequest.of(safePage, safeSize),
                totalElements
        );

        return PaginationResponse.from(resultPage);
    }

    private void validatePostAccess(Post post, User currentUser) {
        PostPrivacy privacy = post.getPrivacy();
        if(privacy == PostPrivacy.PUBLIC){
            return;
        }

        if(currentUser == null){
            throw new UnauthorizedException("Bạn cần đăng nhập để xem bài viết này.");
        }

        if (privacy == PostPrivacy.PRIVATE){
            if (!post.getUser().getId().equals(currentUser.getId())){
                throw new AccessDeniedException("Đây là bài viết riêng tư");
            }
            return;
        }

        if(privacy == PostPrivacy.FRIENDS){
            boolean isOwner = post.getUser().getId().equals(currentUser.getId());
            if(!isOwner){
                boolean isFriend = friendshipRepository.existsByUserIdAndFriendIdAndStatus(
                        currentUser.getId(), post.getUser().getId(), FriendshipStatus.ACCEPTED);
                if (!isFriend){
                    throw new AccessDeniedException(
                            "Chỉ bạn bè mới có thể xem bài viết này");
                }
            }
        }
    }

    @Transactional(readOnly = true)
    public PostDetailResponseDTO getPostDetail(UUID postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Bài viết không tồn tại"));
        User currentUser = null;
        try {
            currentUser = getCurrentUser();
        } catch (UnauthorizedException ignored) {
            // Khách vãng lai
        }

        // 3. Kiểm tra quyền xem (Access Control Logic)
        validatePostAccess(post, currentUser);

        // 4. Nếu vượt qua kiểm tra, tiến hành Map DTO và lấy Comment
        PostDetailResponseDTO response = postMapper.toDetailDTO(post);

        // 1. Lấy comment cấp 1
        Pageable pageable = PageRequest.of(0, 10,
                Sort.by(Sort.Direction.DESC, "createdAt"));
        List<Comment> parentComments = commentRepository
                .findByPostIdAndParentCommentIdIsNull(postId, pageable);

        // 2. Map sang DTO
        List<CommentResponseDTO> commentDTOs = commentMapper.toDTOList(parentComments);

        if (!commentDTOs.isEmpty()) {
            List<UUID> commentIds = commentDTOs.stream().map(CommentResponseDTO::getId).toList();

            // 3. Lấy replies count cho mỗi comment (Tối ưu hóa query)
            Map<UUID, Long> replyCounts = commentRepository.countRepliesByCommentIds(commentIds)
                    .stream()
                    .collect(Collectors.toMap(
                            obj -> (UUID) obj[0],
                            obj -> (Long) obj[1]
                    ));

            // 4. Kiểm tra trạng thái Like và set Reply Count
            Set<UUID> likedCommentIds = new HashSet<>();

            if (currentUser != null) {
                likedCommentIds = likeRepository.findByUserIdAndTargetTypeAndTargetIdIn(
                                currentUser.getId(), TargetType.COMMENT, commentIds)
                        .stream().map(Like::getTargetId).collect(Collectors.toSet());
            }

            for (CommentResponseDTO dto : commentDTOs) {
                dto.setRepliesCount(replyCounts.getOrDefault(dto.getId(), 0L).intValue());
                dto.setLiked(likedCommentIds.contains(dto.getId()));
            }
        }

        if (currentUser != null) {
            response.setLiked(likeRepository.existsByUserIdAndTargetTypeAndTargetId(
                    currentUser.getId(), TargetType.POST, postId));
        }
        response.setComments(commentDTOs);
        return response;
    }

    @Transactional
    public PostResponseDTO updatePost(UUID postId,UpdatePostRequestDTO requestDTO){
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài viết"));
        User user = getCurrentUser();
        if(!post.getUser().getId().equals(user.getId())){
            throw new AccessDeniedException("Bạn không có quyền chỉnh sửa bài viết này");
        }

        if (requestDTO.getContent() != null) post.setContent(requestDTO.getContent());
        if (requestDTO.getPrivacy() != null) post.setPrivacy(requestDTO.getPrivacy());

        // Nếu bạn muốn cho phép người dùng "Xóa" feeling, hãy bỏ check null hoặc dùng StringUtils
        post.setFeeling(requestDTO.getFeeling());
        post.setLocation(requestDTO.getLocation());

        post.setEdited(true);
        post.setUpdatedAt(LocalDateTime.now());

        if (requestDTO.getNewMediaUrls() != null){
            post.getMedia().clear();

            for (PostMediaRequestDTO request : requestDTO.getNewMediaUrls()){
                PostMedia postMedia = postMediaMapper.toEntity(request);
                postMedia.setPost(post);
                post.getMedia().add(postMedia);
            }
        }

        Post saved = postRepository.save(post);
        return postMapper.toDTO(saved);
    }

}
