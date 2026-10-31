package com.tickethub.payment.dto;

import com.tickethub.payment.entity.PaymentMethod;
import jakarta.validation.constraints.NotNull;



public record PaymentInitiationRequest(
        @NotNull Long bookingId,
        @NotNull PaymentMethod method
) {}
