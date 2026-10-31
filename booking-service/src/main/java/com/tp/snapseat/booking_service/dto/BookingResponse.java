package com.tp.snapseat.booking_service.dto;

import com.tp.snapseat.booking_service.enums.BookingStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record BookingResponse(
        Long bookingId,
        String customerId,
        Long showId,
        List<BookingSeatResponse> seats,
        BigDecimal amount,
        String currency,
        BookingStatus status,
        String paymentId,
        Instant createdAt,
        Instant expiresAt
) {
}
