package com.tp.snapseat.booking_service.exception;

import org.springframework.http.HttpStatus;

public class IdempotencyConflictException extends BookingException {

    public IdempotencyConflictException(String message) {
        super(HttpStatus.CONFLICT, "IDEMPOTENCY_KEY_REUSED", message);
    }

    public IdempotencyConflictException(String message, Throwable cause) {
        super(HttpStatus.CONFLICT, "IDEMPOTENCY_KEY_REUSED", message, cause);
    }
}
