-- Create group_members table
CREATE TABLE group_members (
    id UUID PRIMARY KEY,
    group_id UUID NOT NULL,
    user_id UUID NOT NULL,
    role VARCHAR(10) NOT NULL, -- ADMIN, MODERATOR, MEMBER
    joined_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_group_member_group FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE CASCADE,
    CONSTRAINT fk_group_member_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uk_group_member UNIQUE (group_id, user_id)
);

-- Create indexes
CREATE INDEX idx_group_member_group ON group_members(group_id);
CREATE INDEX idx_group_member_user ON group_members(user_id);
