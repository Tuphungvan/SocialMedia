package com.aht.social.domain.repository;

import com.aht.social.domain.enitity.PostMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
@Repository
public interface PostMediaRepository extends JpaRepository<PostMedia, UUID> {
}
