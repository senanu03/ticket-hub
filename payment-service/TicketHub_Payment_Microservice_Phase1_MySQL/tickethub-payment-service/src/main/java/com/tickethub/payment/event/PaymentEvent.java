package com.tickethub.payment.event;

import com.tickethub.payment.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentEvent(
        Long eventId,
        Long paymentId,
        Long bookingId,
        String customerId,
        BigDecimal amount,
        String currency,
        PaymentStatus status,
        String provider,
        String providerReference,
        Instant occurredAt
) {}

