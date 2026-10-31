package com.tp.snapseat.booking_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "booking")
public class BookingProperties {

    private final Reservation reservation = new Reservation();
    private final Events events = new Events();

    public Reservation getReservation() { return reservation; }
    public Events getEvents() { return events; }

    public static class Reservation {
        /** How long a PENDING_PAYMENT booking holds its seats (ST-09: configurable). */
        private Duration expiry = Duration.ofMinutes(10);
        /** Maximum seats in one booking. */
        private int maxSeatsPerBooking = 10;
        /** Max bookings expired per scheduler run. */
        private int expiryBatchSize = 100;

        public Duration getExpiry() { return expiry; }
        public void setExpiry(Duration expiry) { this.expiry = expiry; }
        public int getMaxSeatsPerBooking() { return maxSeatsPerBooking; }
        public void setMaxSeatsPerBooking(int maxSeatsPerBooking) { this.maxSeatsPerBooking = maxSeatsPerBooking; }
        public int getExpiryBatchSize() { return expiryBatchSize; }
        public void setExpiryBatchSize(int expiryBatchSize) { this.expiryBatchSize = expiryBatchSize; }
    }

    public static class Events {
        private boolean enabled = true;
        private String bookingConfirmedTopic = "booking.confirmed";

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getBookingConfirmedTopic() { return bookingConfirmedTopic; }
        public void setBookingConfirmedTopic(String t) { this.bookingConfirmedTopic = t; }
    }
}
