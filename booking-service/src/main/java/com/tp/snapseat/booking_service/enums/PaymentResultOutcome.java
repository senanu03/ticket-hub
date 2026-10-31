package com.tp.snapseat.booking_service.enums;

/** What Booking did with a reported payment result. Returned so Payment can react (e.g. refund in Phase 2). */
public enum PaymentResultOutcome {
    /** Booking moved to CONFIRMED and seats to BOOKED. */
    CONFIRMED,
    /** Booking moved to PAYMENT_FAILED and seats were released. */
    PAYMENT_FAILED_RECORDED,
    /** Same result was already applied (duplicate / retried call). No change. */
    ALREADY_PROCESSED,
    /** Payment succeeded but the reservation had expired; booking NOT confirmed (SC-12). */
    REJECTED_EXPIRED,
    /** Payment succeeded but the booking is cancelled / failed; booking NOT confirmed. */
    REJECTED_NOT_PENDING,
    /** Booking is already confirmed by a different payment. */
    DUPLICATE_PAYMENT
}
