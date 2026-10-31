-- V1__init_event_schema.sql
-- TicketHub Event Service schema definition

CREATE TABLE IF NOT EXISTS movies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    language VARCHAR(100) NOT NULL,
    duration_min INT NOT NULL,
    genre VARCHAR(100),
    certificate VARCHAR(50),
    poster_url VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    movie_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL,
    CONSTRAINT fk_events_movie FOREIGN KEY (movie_id) REFERENCES movies(id)
);

CREATE TABLE IF NOT EXISTS venues (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    chain VARCHAR(255),
    city VARCHAR(100) NOT NULL,
    address VARCHAR(500),
    pincode VARCHAR(20),
    latitude DECIMAL(10, 7),
    longitude DECIMAL(10, 7),
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS screens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    venue_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    screen_type VARCHAR(50) NOT NULL,
    total_rows INT NOT NULL,
    total_seats INT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_screens_venue FOREIGN KEY (venue_id) REFERENCES venues(id)
);

CREATE TABLE IF NOT EXISTS screen_rows (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    screen_id BIGINT NOT NULL,
    row_label VARCHAR(50) NOT NULL,
    seat_count INT NOT NULL,
    seat_type VARCHAR(50) NOT NULL,
    CONSTRAINT uk_screen_row_label UNIQUE (screen_id, row_label),
    CONSTRAINT fk_screen_rows_screen FOREIGN KEY (screen_id) REFERENCES screens(id)
);

CREATE TABLE IF NOT EXISTS seats (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    screen_id BIGINT NOT NULL,
    screen_row_id BIGINT NOT NULL,
    seat_label VARCHAR(50) NOT NULL,
    seat_type VARCHAR(50) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_screen_seat_label UNIQUE (screen_id, seat_label),
    CONSTRAINT fk_seats_screen FOREIGN KEY (screen_id) REFERENCES screens(id),
    CONSTRAINT fk_seats_screen_row FOREIGN KEY (screen_row_id) REFERENCES screen_rows(id)
);

CREATE TABLE IF NOT EXISTS shows (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id BIGINT NOT NULL,
    screen_id BIGINT NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    status VARCHAR(50) NOT NULL,
    min_seats INT NOT NULL,
    max_seats INT NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_shows_event FOREIGN KEY (event_id) REFERENCES events(id),
    CONSTRAINT fk_shows_screen FOREIGN KEY (screen_id) REFERENCES screens(id)
);

CREATE TABLE IF NOT EXISTS show_prices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    show_id BIGINT NOT NULL,
    seat_type VARCHAR(50) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    CONSTRAINT uk_show_seat_type UNIQUE (show_id, seat_type),
    CONSTRAINT fk_show_prices_show FOREIGN KEY (show_id) REFERENCES shows(id)
);

CREATE TABLE IF NOT EXISTS show_seats (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    show_id BIGINT NOT NULL,
    seat_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_show_seat UNIQUE (show_id, seat_id),
    CONSTRAINT fk_show_seats_show FOREIGN KEY (show_id) REFERENCES shows(id),
    CONSTRAINT fk_show_seats_seat FOREIGN KEY (seat_id) REFERENCES seats(id)
);
