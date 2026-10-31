ALTER TABLE payments ADD COLUMN booking_notified BOOLEAN NOT NULL DEFAULT FALSE;
CREATE INDEX idx_payment_booking_notified ON payments(booking_notified, status);
