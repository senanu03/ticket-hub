package com.tp.snapseat.booking_service.exception;

import org.springframework.http.HttpStatus;

public class InvalidSeatSelectionException extends BookingException {

    public InvalidSeatSelectionException(String message) {
        super(HttpStatus.BAD_REQUEST, "INVALID_SEAT_SELECTION", message);
    }

    public InvalidSeatSelectionException(String message, Throwable cause) {
        super(HttpStatus.BAD_REQUEST, "INVALID_SEAT_SELECTION", message, cause);
    }
}
