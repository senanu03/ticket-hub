package com.tp.snapseat.booking_service.dto;

import com.tp.snapseat.booking_service.enums.SeatStatus;

/** Reservation state of one seat. Seats not listed are AVAILABLE. */
public record SeatStateResponse(Long seatId, SeatStatus status) {
}
