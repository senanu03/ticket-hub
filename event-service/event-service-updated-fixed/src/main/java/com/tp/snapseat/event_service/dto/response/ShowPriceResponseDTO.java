package com.tp.snapseat.event_service.dto.response;

import com.tp.snapseat.event_service.enums.SeatType;

import java.math.BigDecimal;

public record ShowPriceResponseDTO(
        Long id,
        Long showId,
        SeatType seatType,
        BigDecimal price
) {
}
