-- Insert test users
-- Password: 123456 (BCrypt encoded with strength 12)

INSERT INTO users (id, username, email, password_hash, role, is_active, is_verified, created_at, updated_at)
VALUES 
    ('a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'admin', 'admin@test.com', 
     '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4tS.1s1sQ9X2E6Iq', 
     'ADMIN', true, true, NOW(), NOW()),
    
    ('b2c3d4e5-f6a7-8901-bcde-f12345678901', 'user1', 'user1@test.com', 
     '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4tS.1s1sQ9X2E6Iq', 
     'USER', true, true, NOW(), NOW()),
    
    ('c3d4e5f6-a7b8-9012-cdef-123456789012', 'user2', 'user2@test.com', 
     '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4tS.1s1sQ9X2E6Iq', 
     'USER', true, false, NOW(), NOW());
