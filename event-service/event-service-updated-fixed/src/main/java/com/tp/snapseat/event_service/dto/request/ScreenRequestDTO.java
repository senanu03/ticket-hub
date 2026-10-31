package com.tp.snapseat.event_service.dto.request;

import com.tp.snapseat.event_service.enums.ScreenType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ScreenRequestDTO(
        @NotNull(message = "Venue ID is required")
        Long venueId,

        @NotBlank(message = "Screen name is required")
        String name,

        @NotNull(message = "Screen type is required")
        ScreenType screenType,

        @NotNull(message = "Total rows is required")
        @Positive(message = "Total rows must be greater than zero")
        Integer totalRows,

        @NotNull(message = "Total seats is required")
        @Positive(message = "Total seats must be greater than zero")
        Integer totalSeats,

        @NotNull(message = "Active status is required")
        Boolean active
) {
}
