package com.tp.snapseat.event_service.dto.request;

import com.tp.snapseat.event_service.enums.EventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EventRequestDTO(
        @NotNull(message = "Movie ID is required")
        Long movieId,

        @NotBlank(message = "Title is required")
        String title,

        @NotNull(message = "Event type is required")
        EventType type,

        String description
) {
}
