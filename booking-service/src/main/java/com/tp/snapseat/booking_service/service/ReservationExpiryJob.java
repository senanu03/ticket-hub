package com.tp.snapseat.booking_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Periodically expires unpaid bookings and frees their seats (ST-09). Safe with several instances (row locks + status re-check). */
@Component
public class ReservationExpiryJob {

    private static final Logger log = LoggerFactory.getLogger(ReservationExpiryJob.class);

    private final BookingService bookingService;

    public ReservationExpiryJob(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @Scheduled(fixedDelayString = "${booking.reservation.expiry-scan-interval-ms:30000}",
            initialDelayString = "${booking.reservation.expiry-scan-interval-ms:30000}")
    public void run() {
        try {
            int expired = bookingService.expireDueBookings();
            if (expired > 0) {
                log.info("Reservation expiry run finished: expiredBookings={}", expired);
            }
        } catch (RuntimeException ex) {
            log.error("Reservation expiry run failed", ex);
        }
    }
}
