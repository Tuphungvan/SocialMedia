-- Create likes table
CREATE TABLE likes (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    target_type VARCHAR(10) NOT NULL, -- POST, COMMENT
    target_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_like_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uk_like_user_target UNIQUE (user_id, target_type, target_id)
);

-- Create indexes
CREATE INDEX idx_like_user ON likes(user_id);
CREATE INDEX idx_like_target ON likes(target_type, target_id);
