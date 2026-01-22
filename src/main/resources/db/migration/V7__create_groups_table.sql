-- Create groups table
CREATE TABLE groups (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    avatar_url VARCHAR(500),
    cover_photo_url VARCHAR(500),
    privacy VARCHAR(10) NOT NULL, -- PUBLIC, PRIVATE
    creator_id UUID NOT NULL,
    members_count INTEGER DEFAULT 0 NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT fk_group_creator FOREIGN KEY (creator_id) REFERENCES users(id) ON DELETE RESTRICT
);

-- Create index
CREATE INDEX idx_group_creator ON groups(creator_id);
