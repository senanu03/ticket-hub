package com.tp.snapseat.event_service.dto.response;

import com.tp.snapseat.event_service.enums.ScreenType;

public record ScreenResponseDTO(
        Long id,
        Long venueId,
        String name,
        ScreenType screenType,
        Integer totalRows,
        Integer totalSeats,
        Boolean active
) {
}
