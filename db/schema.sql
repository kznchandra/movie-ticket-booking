-- Create show table

-- Drop tables in correct order (reverse of creation, respecting foreign key constraints)
DROP TABLE IF EXISTS booking_seat CASCADE;
DROP TABLE IF EXISTS booking CASCADE;
DROP TABLE IF EXISTS seat_inventory CASCADE;
DROP TABLE IF EXISTS show CASCADE;
DROP TABLE IF EXISTS outbox_events CASCADE;


-- Create show table
CREATE TABLE IF NOT EXISTS show
(
    id
                BIGSERIAL
        PRIMARY
            KEY,
    name
                VARCHAR
                    (
                    255
                    ) NOT NULL,
    description TEXT,
    venue VARCHAR
                    (
                    255
                    ) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    status VARCHAR
                    (
                    50
                    ) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create seat_inventory table
CREATE TABLE IF NOT EXISTS seat_inventory
(
    id
               BIGSERIAL
        PRIMARY
            KEY,
    show_id
               BIGINT
                         NOT
                             NULL,
    seat_number
               VARCHAR
                   (
                   50
                   ) NOT NULL,
    seat_type VARCHAR
                   (
                   50
                   ) NOT NULL,
    price DECIMAL
                   (
                   10,
                   2
                   ) NOT NULL,
    status VARCHAR
                   (
                   50
                   ) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_seat_inventory_show FOREIGN KEY
        (
         show_id
            ) REFERENCES show
            (
             id
                ) ON DELETE CASCADE,
    CONSTRAINT uk_show_seat UNIQUE
        (
         show_id,
         seat_number
            )
);

-- Create booking table
CREATE TABLE IF NOT EXISTS booking
(
    id
                   BIGSERIAL
        PRIMARY
            KEY,
    user_id
                   BIGINT
                           NOT
                               NULL,
    show_id
                   BIGINT
                           NOT
                               NULL,
    booking_reference
                   VARCHAR
                       (
                       100
                       ) NOT NULL UNIQUE,
    total_amount DECIMAL
                       (
                       10,
                       2
                       ) NOT NULL,
    status VARCHAR
                       (
                       50
                       ) NOT NULL,
    payment_status VARCHAR
                       (
                       50
                       ),
    booking_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_booking_show FOREIGN KEY
        (
         show_id
            ) REFERENCES show
            (
             id
                )
);

-- Create booking_seat table (junction table)
CREATE TABLE IF NOT EXISTS booking_seat
(
    id
               BIGSERIAL
        PRIMARY
            KEY,
    booking_id
               BIGINT
                         NOT
                             NULL,
    seat_inventory_id
               BIGINT
                         NOT
                             NULL,
    price
               DECIMAL
                   (
                   10,
                   2
                   ) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_booking_seat_booking FOREIGN KEY
        (
         booking_id
            ) REFERENCES booking
            (
             id
                ) ON DELETE CASCADE,
    CONSTRAINT fk_booking_seat_inventory FOREIGN KEY
        (
         seat_inventory_id
            ) REFERENCES seat_inventory
            (
             id
                ),
    CONSTRAINT uk_booking_seat UNIQUE
        (
         booking_id,
         seat_inventory_id
            )
);

-- Create outbox_events table (for transactional outbox pattern)
CREATE TABLE IF NOT EXISTS outbox_events
(
    id
                  BIGSERIAL
        PRIMARY
            KEY,
    aggregate_type
                  VARCHAR
                      (
                      255
                      ) NOT NULL,
    aggregate_id VARCHAR
                      (
                      255
                      ) NOT NULL,
    event_type VARCHAR
                      (
                      255
                      ) NOT NULL,
    payload JSONB NOT NULL,
    status VARCHAR
                      (
                      50
                      ) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP,
    retry_count INT NOT NULL DEFAULT 0,
    error_message TEXT
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_seat_inventory_show_id ON seat_inventory (show_id);
CREATE INDEX IF NOT EXISTS idx_seat_inventory_status ON seat_inventory (status);
CREATE INDEX IF NOT EXISTS idx_booking_user_id ON booking (user_id);
CREATE INDEX IF NOT EXISTS idx_booking_show_id ON booking (show_id);
CREATE INDEX IF NOT EXISTS idx_booking_status ON booking (status);
CREATE INDEX IF NOT EXISTS idx_booking_seat_booking_id ON booking_seat (booking_id);
CREATE INDEX IF NOT EXISTS idx_booking_seat_inventory_id ON booking_seat (seat_inventory_id);
CREATE INDEX IF NOT EXISTS idx_outbox_events_status ON outbox_events (status);
CREATE INDEX IF NOT EXISTS idx_outbox_events_created_at ON outbox_events (created_at);
-- Create show table
