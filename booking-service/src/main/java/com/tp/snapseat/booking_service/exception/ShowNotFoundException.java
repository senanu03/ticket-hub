package com.tp.snapseat.booking_service.exception;

import org.springframework.http.HttpStatus;

public class ShowNotFoundException extends BookingException {

    public ShowNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, "SHOW_NOT_FOUND", message);
    }

    public ShowNotFoundException(String message, Throwable cause) {
        super(HttpStatus.NOT_FOUND, "SHOW_NOT_FOUND", message, cause);
    }
}
