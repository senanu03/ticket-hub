package com.tp.snapseat.booking_service.exception;

import org.springframework.http.HttpStatus;

public class InvalidRequestException extends BookingException {

    public InvalidRequestException(String message) {
        super(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", message);
    }

    public InvalidRequestException(String message, Throwable cause) {
        super(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", message, cause);
    }
}
