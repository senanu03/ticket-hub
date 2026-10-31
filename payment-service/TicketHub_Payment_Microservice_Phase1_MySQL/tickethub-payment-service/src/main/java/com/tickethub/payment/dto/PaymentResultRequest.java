package com.tickethub.payment.dto;

import com.tickethub.payment.entity.PaymentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PaymentResultRequest(
        @NotBlank String paymentId,
        @NotNull PaymentStatus status,
        BigDecimal amount,
        String providerReference,
        String providerPaymentId,
        String reason
) {}