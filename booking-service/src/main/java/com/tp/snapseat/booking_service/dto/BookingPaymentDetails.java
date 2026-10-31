package com.tp.snapseat.booking_service.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record BookingPaymentDetails(
        Long bookingId,
        String customerId,
        BigDecimal amount,
        String currency,
        String status,
        Instant expiryAt
) {
}