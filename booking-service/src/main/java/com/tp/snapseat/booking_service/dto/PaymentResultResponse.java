package com.tp.snapseat.booking_service.dto;

import com.tp.snapseat.booking_service.enums.BookingStatus;
import com.tp.snapseat.booking_service.enums.PaymentResultOutcome;

public record PaymentResultResponse(Long bookingId, BookingStatus bookingStatus, PaymentResultOutcome outcome) {
}
