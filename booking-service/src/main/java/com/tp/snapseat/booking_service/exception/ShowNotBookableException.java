package com.tp.snapseat.booking_service.exception;

import org.springframework.http.HttpStatus;

public class ShowNotBookableException extends BookingException {

    public ShowNotBookableException(String message) {
        super(HttpStatus.CONFLICT, "SHOW_NOT_BOOKABLE", message);
    }

    public ShowNotBookableException(String message, Throwable cause) {
        super(HttpStatus.CONFLICT, "SHOW_NOT_BOOKABLE", message, cause);
    }
}
