package com.tickethub.payment.dto;

import com.tickethub.payment.entity.PaymentMethod;
import com.tickethub.payment.entity.PaymentProvider;
import com.tickethub.payment.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(
        Long paymentId,
        Long bookingId,
        BigDecimal amount,
        String currency,
        PaymentStatus status,
        PaymentProvider provider,
        PaymentMethod method,
        String providerReference,
        String providerOrderId,
        String providerPaymentId,
        String razorpayKeyId,
        Instant createdAt,
        Instant updatedAt
) {}
