package com.tp.snapseat.booking_service.exception;

import com.tp.snapseat.booking_service.enums.BookingStatus;
import org.springframework.http.HttpStatus;

public class InvalidBookingStateException extends BookingException {

    public InvalidBookingStateException(String message) {
        super(HttpStatus.CONFLICT, "INVALID_BOOKING_STATE", message);
    }

    public InvalidBookingStateException(Long bookingId, BookingStatus from, BookingStatus to) {
        super(HttpStatus.CONFLICT, "INVALID_BOOKING_STATE",
                "Booking " + bookingId + " cannot move from " + from + " to " + to);
    }
}
