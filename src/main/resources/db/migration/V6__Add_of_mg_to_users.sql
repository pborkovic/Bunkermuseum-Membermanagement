-- Add of_mg field to users table
-- Adds boolean field to track officer member status
-- Author: Philipp Borkovic

ALTER TABLE users
    ADD COLUMN of_mg BOOLEAN NOT NULL DEFAULT FALSE;
