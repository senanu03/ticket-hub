package com.tickethub.payment.dto;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(
        Instant timestamp,
        int status,
        String code,
        String message,
        String path,
        String correlationId,
        List<FieldErrorItem> errors
) {
    public record FieldErrorItem(String field, String message) {}
}
