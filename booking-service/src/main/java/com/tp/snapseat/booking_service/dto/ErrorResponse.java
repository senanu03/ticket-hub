package com.tp.snapseat.booking_service.dto;

import java.time.Instant;
import java.util.List;

/** The one standard error format (ST-02 / ST-50). Never carries stack traces or internals. */
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String code,
        String message,
        String path,
        String correlationId,
        List<FieldViolation> details
) {
    public record FieldViolation(String field, String message) {
    }
}
