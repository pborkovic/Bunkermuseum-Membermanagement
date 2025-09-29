-- Create users table
-- Generated from User entity in com.bunkermuseum.membermanagement.model.User
-- Author: Philipp Borkovic

CREATE TABLE users (
    id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,

    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    email_verified_at TIMESTAMP,
    password VARCHAR(255),
    avatar_path VARCHAR(500),
    google_id VARCHAR(255),
    microsoft_id VARCHAR(255),

    -- Primary key constraint
    CONSTRAINT pk_users PRIMARY KEY (id),

    -- Unique constraints (as defined in @UniqueConstraint annotations)
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT uk_users_google_id UNIQUE (google_id),
    CONSTRAINT uk_users_microsoft_id UNIQUE (microsoft_id)
);

-- Create indexes for performance (as defined in @Index annotations)
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_name ON users(name);
CREATE INDEX idx_users_deleted_at ON users(deleted_at);
CREATE INDEX idx_users_name_deleted ON users(name, deleted_at);