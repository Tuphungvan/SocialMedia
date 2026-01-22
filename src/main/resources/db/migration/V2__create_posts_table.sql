-- Create posts table
CREATE TABLE posts (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    content TEXT,
    privacy VARCHAR(20) NOT NULL, -- PUBLIC, FRIENDS, PRIVATE
    location VARCHAR(255),
    feeling VARCHAR(255),
    is_edited BOOLEAN DEFAULT FALSE NOT NULL,
    likes_count INTEGER DEFAULT 0 NOT NULL,
    comments_count INTEGER DEFAULT 0 NOT NULL,
    shares_count INTEGER DEFAULT 0 NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT fk_post_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX idx_post_user ON posts(user_id);
CREATE INDEX idx_post_privacy_created ON posts(privacy, created_at);
