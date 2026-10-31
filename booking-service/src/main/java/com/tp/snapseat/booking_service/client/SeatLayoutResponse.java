package com.tp.snapseat.booking_service.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SeatLayoutResponse(
        Long id,
        Long showId,
        Long seatId,
        String seatLabel,
        String seatType,
        String status,
        BigDecimal price,
        Long version
) {
    public SeatLayoutResponse(Long seatId, String row, int number, String category) {
        this(seatId, 1L, seatId, row + number, category, "AVAILABLE", new BigDecimal("250.00"), 1L);
    }
}

