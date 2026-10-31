package com.tp.snapseat.event_service.dto.response;

import com.tp.snapseat.event_service.enums.EventStatus;
import com.tp.snapseat.event_service.enums.EventType;

public record EventResponseDTO(
        Long id,
        Long movieId,
        String title,
        EventType type,
        String description,
        EventStatus status
) {
}
