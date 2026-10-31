package com.tp.snapseat.booking_service.exception;

import org.springframework.http.HttpStatus;

public class AmountMismatchException extends BookingException {

    public AmountMismatchException(String message) {
        super(HttpStatus.CONFLICT, "AMOUNT_MISMATCH", message);
    }

    public AmountMismatchException(String message, Throwable cause) {
        super(HttpStatus.CONFLICT, "AMOUNT_MISMATCH", message, cause);
    }
}
