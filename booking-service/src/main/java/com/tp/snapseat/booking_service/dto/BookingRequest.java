package com.tp.snapseat.booking_service.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/** Create-booking request. No customer id and no amount: identity comes from the token, price from the show. */
public record BookingRequest(
        @NotNull(message = "showId is required") Long showId,
        @NotEmpty(message = "at least one seat is required") List<@NotNull(message = "seatId must not be null") Long> seatIds
) {
}
