-- Create users table
CREATE TABLE users (
    id UUID PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255),
    phone_number VARCHAR(20) UNIQUE,
    avatar_url VARCHAR(500),
    cover_photo_url VARCHAR(500),
    bio TEXT,
    date_of_birth DATE,
    gender VARCHAR(10),
    is_verified BOOLEAN DEFAULT FALSE NOT NULL,
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    google_id VARCHAR(100) UNIQUE,
    facebook_id VARCHAR(100) UNIQUE,
    last_login_at TIMESTAMP,
    role VARCHAR(20) DEFAULT 'USER' NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

-- Create indexes
CREATE INDEX idx_email ON users(email);
CREATE INDEX idx_username ON users(username);
CREATE INDEX idx_phone_number ON users(phone_number);
CREATE INDEX idx_google_id ON users(google_id);
CREATE INDEX idx_facebook_id ON users(facebook_id);
