-- Create bookings table
-- Generated from Booking entity in com.bunkermuseum.membermanagement.model.Booking
-- Author: Philipp Borkovic

CREATE TABLE bookings (
    id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,

    expected_purpose VARCHAR(255),
    expected_amount DECIMAL(10,2),
    received_at TIMESTAMP,
    actual_purpose VARCHAR(255),
    actual_amount DECIMAL(10,2),
    ofMG VARCHAR(255),
    note VARCHAR(255),
    account_statement_page VARCHAR(255),
    code VARCHAR(255),
    user_id UUID,

    -- Primary key constraint
    CONSTRAINT pk_bookings PRIMARY KEY (id),

    -- Foreign key constraint
    CONSTRAINT fk_bookings_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Create indexes for performance (as defined in @Index annotations)
CREATE INDEX idx_bookings_received_at ON bookings(received_at);
CREATE INDEX idx_bookings_user_id ON bookings(user_id);
