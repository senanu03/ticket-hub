package com.tp.snapseat.booking_service.dto;

import com.tp.snapseat.booking_service.enums.PaymentResultStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/** Sent by the Payment Service (service-to-service) once a payment result is verified. */
public record PaymentResultRequest(
        @NotBlank(message = "paymentId is required") @Size(max = 64) String paymentId,
        @NotNull(message = "status is required") PaymentResultStatus status,
        /** Optional safety check: if present it must equal the booking amount. */
        BigDecimal amount
) {
}
