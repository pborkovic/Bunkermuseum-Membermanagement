-- Create emails table for tracking sent emails
CREATE TABLE emails (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    from_address VARCHAR(255) NOT NULL,
    to_address VARCHAR(255) NOT NULL,
    subject VARCHAR(500) NOT NULL,
    content TEXT NOT NULL,
    user_id UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,

    -- Foreign key constraint to users table
    CONSTRAINT fk_emails_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- Create indexes for performance
CREATE INDEX idx_emails_user_id ON emails(user_id);
CREATE INDEX idx_emails_from_address ON emails(from_address);
CREATE INDEX idx_emails_to_address ON emails(to_address);
CREATE INDEX idx_emails_created_at ON emails(created_at);
CREATE INDEX idx_emails_user_created ON emails(user_id, created_at);

-- Add comment to table
COMMENT ON TABLE emails IS 'Stores all emails sent by the system or users for audit and tracking purposes';
COMMENT ON COLUMN emails.user_id IS 'NULL indicates system-generated email, non-NULL indicates user-sent email';
