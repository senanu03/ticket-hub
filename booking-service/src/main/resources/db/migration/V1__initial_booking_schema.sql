CREATE TABLE bookings (
                          booking_id BIGINT NOT NULL AUTO_INCREMENT,
                          customer_id VARCHAR(64) NOT NULL,
                          customer_email VARCHAR(254),
                          idempotency_key VARCHAR(100) NOT NULL,
                          show_id BIGINT NOT NULL,
                          event_id BIGINT,
                          amount DECIMAL(10,2) NOT NULL,
                          currency VARCHAR(3) NOT NULL,
                          status VARCHAR(20) NOT NULL,
                          payment_id VARCHAR(64),
                          created_at DATETIME(6) NOT NULL,
                          updated_at DATETIME(6),
                          expires_at DATETIME(6) NOT NULL,
                          cancelled_at DATETIME(6),
                          version BIGINT,
                          PRIMARY KEY (booking_id),
                          CONSTRAINT uk_booking_customer_idempotency
                              UNIQUE (customer_id, idempotency_key)
);

CREATE INDEX idx_booking_status_expiry
    ON bookings (status, expires_at);

CREATE INDEX idx_booking_customer
    ON bookings (customer_id);


CREATE TABLE booking_seats (
                               booking_seat_id BIGINT NOT NULL AUTO_INCREMENT,
                               booking_id BIGINT NOT NULL,
                               seat_id BIGINT NOT NULL,
                               show_seat_id BIGINT NOT NULL,
                               price DECIMAL(10,2) NOT NULL,
                               PRIMARY KEY (booking_seat_id),
                               CONSTRAINT fk_booking_seats_booking
                                   FOREIGN KEY (booking_id)
                                       REFERENCES bookings (booking_id)
                                       ON DELETE CASCADE
);

CREATE INDEX idx_booking_seat_booking
    ON booking_seats (booking_id);


CREATE TABLE booking_status_history (
                                        id BIGINT NOT NULL AUTO_INCREMENT,
                                        booking_id BIGINT NOT NULL,
                                        old_status VARCHAR(20),
                                        new_status VARCHAR(20) NOT NULL,
                                        changed_at DATETIME(6) NOT NULL,
                                        reason VARCHAR(255),
                                        PRIMARY KEY (id),
                                        CONSTRAINT fk_history_booking
                                            FOREIGN KEY (booking_id)
                                                REFERENCES bookings (booking_id)
                                                ON DELETE CASCADE
);

CREATE INDEX idx_history_booking
    ON booking_status_history (booking_id);


CREATE TABLE show_seats (
                            show_seat_id BIGINT NOT NULL AUTO_INCREMENT,
                            show_id BIGINT NOT NULL,
                            seat_id BIGINT NOT NULL,
                            status VARCHAR(20) NOT NULL,
                            held_by_booking_id BIGINT,
                            locked_at DATETIME(6),
                            lock_expiry DATETIME(6),
                            PRIMARY KEY (show_seat_id),
                            CONSTRAINT uk_show_seat
                                UNIQUE (show_id, seat_id)
);

CREATE INDEX idx_show_seat_holder
    ON show_seats (held_by_booking_id);