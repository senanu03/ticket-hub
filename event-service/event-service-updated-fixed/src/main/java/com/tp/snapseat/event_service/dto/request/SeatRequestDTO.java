package com.tp.snapseat.event_service.dto.request;

import com.tp.snapseat.event_service.enums.SeatType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SeatRequestDTO(
        @NotNull(message = "Screen ID is required")
        Long screenId,

        @NotNull(message = "Screen row ID is required")
        Long screenRowId,

        @NotBlank(message = "Seat label is required")
        String seatLabel,

        @NotNull(message = "Seat type is required")
        SeatType seatType,

        @NotNull(message = "Active status is required")
        Boolean active
) {
}
