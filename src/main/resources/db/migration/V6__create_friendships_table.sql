-- Create friendships table
CREATE TABLE friendships (
    id UUID PRIMARY KEY,
    requester_id UUID NOT NULL,
    addressee_id UUID NOT NULL,
    status VARCHAR(10) NOT NULL, -- PENDING, ACCEPTED, BLOCKED, DENY
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT fk_friendship_requester FOREIGN KEY (requester_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_friendship_addressee FOREIGN KEY (addressee_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uk_friendship_pair UNIQUE (requester_id, addressee_id),
    CONSTRAINT chk_friendship_different_users CHECK (requester_id != addressee_id)
);

-- Create indexes
CREATE INDEX idx_friendship_requester ON friendships(requester_id);
CREATE INDEX idx_friendship_addressee ON friendships(addressee_id);
CREATE INDEX idx_friendship_status ON friendships(status);
