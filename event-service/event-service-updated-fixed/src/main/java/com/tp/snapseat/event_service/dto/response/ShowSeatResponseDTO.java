 package com.tp.snapseat.event_service.dto.response;

import com.tp.snapseat.event_service.enums.SeatStatus;
import com.tp.snapseat.event_service.enums.SeatType;

import java.math.BigDecimal;

public record ShowSeatResponseDTO(
        Long id,
        Long showId,
        Long seatId,
        String seatLabel,
        SeatType seatType,
        SeatStatus status,
        BigDecimal price,
        Long version
) {
}

