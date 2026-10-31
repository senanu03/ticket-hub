package com.tp.snapseat.booking_service.exception;

import org.springframework.http.HttpStatus;

public class BookingNotFoundException extends BookingException {

    public BookingNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, "BOOKING_NOT_FOUND", message);
    }

    public BookingNotFoundException(String message, Throwable cause) {
        super(HttpStatus.NOT_FOUND, "BOOKING_NOT_FOUND", message, cause);
    }
}
