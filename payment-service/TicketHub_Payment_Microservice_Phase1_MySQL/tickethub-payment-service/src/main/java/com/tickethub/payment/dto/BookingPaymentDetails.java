package com.tickethub.payment.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record BookingPaymentDetails(
        Long bookingId,
        String customerId,
        BigDecimal amount,
        String currency,
        String status,
        Instant expiryAt
) {}
