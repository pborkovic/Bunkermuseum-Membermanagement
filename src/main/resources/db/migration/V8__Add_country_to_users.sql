-- Add country field to user address
-- Migration: V8
-- Date: 2025-11-07

ALTER TABLE users
    ADD COLUMN country VARCHAR(100);
