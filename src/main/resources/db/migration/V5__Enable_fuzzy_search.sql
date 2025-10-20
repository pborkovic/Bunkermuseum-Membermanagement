-- Enable PostgreSQL trigram extension for fuzzy search
-- This extension provides similarity() and % operator for fuzzy string matching
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- Create GIN indexes for efficient fuzzy search on text fields
-- GIN (Generalized Inverted Index) is optimal for trigram search
CREATE INDEX IF NOT EXISTS idx_users_name_trgm ON users USING GIN (name gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_users_email_trgm ON users USING GIN (email gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_users_phone_trgm ON users USING GIN (phone gin_trgm_ops);

-- Create B-tree indexes for exact and prefix matches (for performance)
CREATE INDEX IF NOT EXISTS idx_users_name_lower ON users (LOWER(name));
CREATE INDEX IF NOT EXISTS idx_users_email_lower ON users (LOWER(email));

-- Comment explaining the fuzzy search setup
COMMENT ON EXTENSION pg_trgm IS 'Provides fuzzy string matching using trigram similarity for user search functionality';
