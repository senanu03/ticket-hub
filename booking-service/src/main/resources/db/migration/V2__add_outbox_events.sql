-- V2__add_outbox_events.sql
CREATE TABLE IF NOT EXISTS outbox_events (
    id BIGINT NOT NULL AUTO_INCREMENT,
    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_id BIGINT NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    topic VARCHAR(100) NOT NULL,
    partition_key VARCHAR(100),
    payload TEXT NOT NULL,
    status VARCHAR(50) NOT NULL,
    retry_count INT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    published_at DATETIME(6),
    error_message VARCHAR(1000),
    PRIMARY KEY (id)
);

CREATE INDEX idx_outbox_status_created ON outbox_events (status, created_at);
