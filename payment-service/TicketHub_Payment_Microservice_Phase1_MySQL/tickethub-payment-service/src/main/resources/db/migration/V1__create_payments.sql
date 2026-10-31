CREATE TABLE payments (
                          id CHAR(36) NOT NULL,
                          booking_id CHAR(36) NOT NULL,
                          customer_id VARCHAR(100) NOT NULL,
                          amount DECIMAL(19,2) NOT NULL,
                          currency VARCHAR(3) NOT NULL,
                          status VARCHAR(30) NOT NULL,
                          provider VARCHAR(30) NOT NULL,
                          payment_method VARCHAR(30),
                          provider_reference VARCHAR(100),
                          provider_order_id VARCHAR(100),
                          provider_payment_id VARCHAR(100),
                          idempotency_key VARCHAR(100) NOT NULL,
                          created_at TIMESTAMP(6) NOT NULL,
                          updated_at TIMESTAMP(6) NOT NULL,
                          version BIGINT NOT NULL DEFAULT 0,

                          PRIMARY KEY (id),
                          CONSTRAINT uk_payment_booking UNIQUE (booking_id),
                          CONSTRAINT uk_payment_idempotency UNIQUE (idempotency_key),
                          CONSTRAINT uk_payment_provider_ref UNIQUE (provider_reference),
                          CONSTRAINT ck_payment_status CHECK (status IN ('INITIATED','PENDING','SUCCESS','FAILED','CANCELLED','REFUNDED'))
);

CREATE INDEX idx_payment_customer ON payments(customer_id);
CREATE INDEX idx_payment_status ON payments(status);
CREATE INDEX idx_payment_provider_order ON payments(provider_order_id);
CREATE INDEX idx_payment_provider_payment ON payments(provider_payment_id);
