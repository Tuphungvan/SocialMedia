package com.aht.social.domain.enitity;

import com.aht.social.domain.enums.MediaType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "post_media")
@Data
public class PostMedia {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @Enumerated(EnumType.STRING)
    private MediaType mediaType; // IMAGE, VIDEO

    private String mediaUrl;
    private String thumbnailUrl;
    private int orderIndex;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
