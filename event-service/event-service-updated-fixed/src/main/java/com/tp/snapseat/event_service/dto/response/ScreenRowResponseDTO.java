package com.tp.snapseat.event_service.dto.response;

import com.tp.snapseat.event_service.enums.SeatType;

public record ScreenRowResponseDTO(
        Long id,
        Long screenId,
        String rowLabel,
        Integer seatCount,
        SeatType seatType
) {
}
