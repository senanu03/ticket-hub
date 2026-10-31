package com.tp.snapseat.booking_service.enums;

/** Booking workflow state (Blueprint section 6). Only PENDING_PAYMENT can move; everything else is terminal in Phase 1. */
public enum BookingStatus {
    PENDING_PAYMENT,
    CONFIRMED,
    PAYMENT_FAILED,
    CANCELLED,
    EXPIRED;

    public boolean canTransitionTo(BookingStatus next) {
        return this == PENDING_PAYMENT && next != PENDING_PAYMENT;
    }
}
