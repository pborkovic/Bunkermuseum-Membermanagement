-- Create roles table and user_roles pivot table
-- Generated from Role entity in com.bunkermuseum.membermanagement.model.Role
-- And User-Role many-to-many relationship
-- Author: Philipp Borkovic

-- Create roles table
CREATE TABLE roles (
    id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,

    name VARCHAR(100) NOT NULL,

    -- Primary key constraint
    CONSTRAINT pk_roles PRIMARY KEY (id),

    -- Unique constraints (as defined in @UniqueConstraint annotations)
    CONSTRAINT uk_roles_name UNIQUE (name)
);

-- Create indexes for performance (as defined in @Index annotations)
CREATE INDEX idx_roles_name ON roles(name);
CREATE INDEX idx_roles_deleted_at ON roles(deleted_at);
CREATE INDEX idx_roles_name_deleted ON roles(name, deleted_at);

-- Create user_roles pivot table for many-to-many relationship
CREATE TABLE user_roles (
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    deleted_at TIMESTAMP,

    CONSTRAINT pk_user_roles PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role_id FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- Create indexes for performance (as defined in @Index annotations in @JoinTable)
CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX idx_user_roles_role_id ON user_roles(role_id);
CREATE INDEX idx_user_roles_user_role ON user_roles(user_id, role_id);
CREATE INDEX idx_user_roles_created_at ON user_roles(created_at);
CREATE INDEX idx_user_roles_deleted_at ON user_roles(deleted_at);