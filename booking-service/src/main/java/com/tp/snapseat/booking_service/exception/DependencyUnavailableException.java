package com.tp.snapseat.booking_service.exception;

import org.springframework.http.HttpStatus;

public class DependencyUnavailableException extends BookingException {

    public DependencyUnavailableException(String message) {
        super(HttpStatus.SERVICE_UNAVAILABLE, "DEPENDENCY_UNAVAILABLE", message);
    }

    public DependencyUnavailableException(String message, Throwable cause) {
        super(HttpStatus.SERVICE_UNAVAILABLE, "DEPENDENCY_UNAVAILABLE", message, cause);
    }
}
