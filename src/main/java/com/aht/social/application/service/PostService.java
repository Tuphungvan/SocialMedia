package com.aht.social.application.service;

import com.aht.social.application.dto.request.post.CreatePostRequestDTO;
import com.aht.social.application.dto.response.common.PaginationResponse;
import com.aht.social.application.dto.response.post.PostResponseDTO;
import com.aht.social.application.mapper.PostMapper;
import com.aht.social.application.mapper.PostMediaMapper;
import com.aht.social.domain.entity.Like;
import com.aht.social.domain.entity.Post;
import com.aht.social.domain.entity.PostMedia;
import com.aht.social.domain.entity.User;
import com.aht.social.domain.enums.TargetType;
import com.aht.social.domain.repository.LikeRepository;
import com.aht.social.domain.repository.PostRepository;
import com.aht.social.domain.repository.UserRepository;
import com.aht.social.presentation.exception.ResourceNotFoundException;
import com.aht.social.presentation.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
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

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth == null || !auth.isAuthenticated()){
            throw new UnauthorizedException("Chưa đăng nhập");
        }

        try{
            UUID userId = UUID.fromString(auth.getName());
            return userRepository.findById(userId)
                    .orElseThrow(() -> new UnauthorizedException("User không tồn tại"));
        }catch (IllegalArgumentException ex){
            throw new UnauthorizedException("Token không hợp lệ");
        }
    }

    @Transactional
    public PostResponseDTO createPost(CreatePostRequestDTO requestDTO){
        User currentUser = getCurrentUser();

        Post post = postMapper.toCreateEntity(requestDTO);
        post.setUser(currentUser);
        post.setEdited(false);

        if (requestDTO.getMedia() != null){
            List<PostMedia> mediaEntities = new ArrayList<>();
            requestDTO.getMedia().forEach(dto -> {
                PostMedia media = postMediaMapper.toEntity(dto);
                media.setPost(post);
                mediaEntities.add(media);
            });
            post.setMedia(mediaEntities);
        }

        Post saved = postRepository.save(post);
        PostResponseDTO response = postMapper.toDTO(saved);
        //set like false cho post mới tạo
        response.setLiked(false);
        return response;
    }

    private void setLikedStatusForPosts(List<PostResponseDTO> posts, UUID userId){
        if(posts.isEmpty()){
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

    /**
     * Helper method để tạo Page từ List (vì Page.of() không có sẵn)
     */
    private Page<PostResponseDTO> createPageFromList(List<PostResponseDTO> content, Page<Post> originalPage) {
        return new org.springframework.data.domain.PageImpl<>(
                content,
                originalPage.getPageable(),
                originalPage.getTotalElements()
        );
    }

    @Transactional(readOnly = true)
    public PaginationResponse<PostResponseDTO> getNewsfeed(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.max(size, 1);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Post> postPage = postRepository.findAll(pageable);

        // Map posts to DTOs
        List<PostResponseDTO> postDTOs = postPage.getContent().stream()
                .map(postMapper::toDTO)
                .collect(Collectors.toList());

        // Check like status nếu user đã đăng nhập
        try {
            User currentUser = getCurrentUser();
            setLikedStatusForPosts(postDTOs, currentUser.getId());
        } catch (UnauthorizedException e) {
            // Nếu chưa đăng nhập, set tất cả isLiked = false
            postDTOs.forEach(post -> post.setLiked(false));
        }

        return PaginationResponse.from(createPageFromList(postDTOs, postPage));
    }

    @Transactional(readOnly = true)
    public PostResponseDTO getPostDetail(UUID postId){
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy post"));

        PostResponseDTO response = postMapper.toDTO(post);

        // Check like status nếu user đã đăng nhập
        try {
            User currentUser = getCurrentUser();
            boolean isLiked = likeRepository.existsByUserIdAndTargetTypeAndTargetId(
                    currentUser.getId(),
                    TargetType.POST,
                    postId
            );
            response.setLiked(isLiked);
        } catch (UnauthorizedException e) {
            // Nếu chưa đăng nhập, set isLiked = false
            response.setLiked(false);
        }

        return response;
    }

}
