-- Add registration form fields to users table
-- Adds personal information fields required for user registration
-- Author: Philipp Borkovic

ALTER TABLE users
    ADD COLUMN salutation VARCHAR(20),
    ADD COLUMN academic_title VARCHAR(50),
    ADD COLUMN rank VARCHAR(50),
    ADD COLUMN birthday DATE,
    ADD COLUMN phone VARCHAR(20),
    ADD COLUMN street VARCHAR(255),
    ADD COLUMN city VARCHAR(100),
    ADD COLUMN postal_code VARCHAR(10);
