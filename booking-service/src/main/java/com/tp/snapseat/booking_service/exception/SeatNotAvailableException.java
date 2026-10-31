package com.tp.snapseat.booking_service.exception;

import org.springframework.http.HttpStatus;

public class SeatNotAvailableException extends BookingException {

    public SeatNotAvailableException(String message) {
        super(HttpStatus.CONFLICT, "SEAT_NOT_AVAILABLE", message);
    }

    public SeatNotAvailableException(String message, Throwable cause) {
        super(HttpStatus.CONFLICT, "SEAT_NOT_AVAILABLE", message, cause);
    }
}
