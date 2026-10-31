package com.tp.snapseat.event_service.dto.response;

import com.tp.snapseat.event_service.enums.SeatType;

public record SeatResponseDTO(
        Long id,
        Long screenId,
        Long screenRowId,
        String seatLabel,
        SeatType seatType,
        Boolean active
) {
}
