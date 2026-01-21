package com.aht.social.infrastructure.security;

import com.aht.social.domain.entity.User;
import com.aht.social.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {
    
    private final UserRepository userRepository;
    
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        log.debug("Loading user by userId: {}", userId);
        
        // Parse userId
        UUID userUuid;
        try {
            userUuid = UUID.fromString(userId);
        } catch (IllegalArgumentException e) {
            log.error("Invalid UUID format: {}", userId);
            throw new UsernameNotFoundException("Invalid user ID format");
        }
        
        // Query user
        User user = userRepository.findById(userUuid)
            .orElseThrow(() -> {
                log.error("User not found with id: {}", userId);
                return new UsernameNotFoundException("User not found with id: " + userId);
            });
        
        // Build UserDetails
        return org.springframework.security.core.userdetails.User
            .withUsername(user.getId().toString())
            .password(user.getPasswordHash())
            .authorities("ROLE_" + user.getRole().name())
            .disabled(!user.getIsActive())
            .build();
    }
}
