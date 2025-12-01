-- Create password_setup_tokens table for user password setup flow
-- When an admin creates a user, a token is generated and emailed to them
-- Users click the link in the email to set their initial password

CREATE TABLE IF NOT EXISTS password_setup_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_password_setup_tokens_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

-- Index for efficient token lookup
CREATE INDEX idx_password_setup_tokens_token ON password_setup_tokens(token);

-- Index for efficient user lookup
CREATE INDEX idx_password_setup_tokens_user_id ON password_setup_tokens(user_id);

-- Index for cleanup of expired tokens
CREATE INDEX idx_password_setup_tokens_expires_at ON password_setup_tokens(expires_at);
