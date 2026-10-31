package com.tp.snapseat.event_service.dto.request;

import com.tp.snapseat.event_service.enums.SeatType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ScreenRowRequestDTO(
        @NotNull(message = "Screen ID is required")
        Long screenId,

        @NotBlank(message = "Row label is required")
        String rowLabel,

        @NotNull(message = "Seat count is required")
        @Positive(message = "Seat count must be greater than zero")
        Integer seatCount,

        @NotNull(message = "Seat type is required")
        SeatType seatType
) {
}
