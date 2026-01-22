-- Create post_media table
CREATE TABLE post_media (
    id UUID PRIMARY KEY,
    post_id UUID NOT NULL,
    media_type VARCHAR(10) NOT NULL, -- IMAGE, VIDEO
    media_url VARCHAR(500),
    thumbnail_url VARCHAR(500),
    order_index INTEGER DEFAULT 0 NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_post_media_post FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE
);

-- Create index
CREATE INDEX idx_post_media_post ON post_media(post_id);
