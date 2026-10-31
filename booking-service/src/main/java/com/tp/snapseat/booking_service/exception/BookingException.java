package com.tp.snapseat.booking_service.exception;

import org.springframework.http.HttpStatus;

/** Base type for controlled business errors. Carries the HTTP status and a stable machine-readable code. */
public abstract class BookingException extends RuntimeException {

    private final HttpStatus status;
    private final String code;

    protected BookingException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    protected BookingException(HttpStatus status, String code, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
        this.code = code;
    }

    public HttpStatus getStatus() { return status; }
    public String getCode() { return code; }
}
