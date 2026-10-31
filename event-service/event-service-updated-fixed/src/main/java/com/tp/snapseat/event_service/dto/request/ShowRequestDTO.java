package com.tp.snapseat.event_service.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public record ShowRequestDTO(
        @NotNull(message = "Event ID is required")
        Long eventId,

        @NotNull(message = "Screen ID is required")
        Long screenId,

        @NotNull(message = "Start time is required")
        @FutureOrPresent(message = "Start time must be current or future")
        LocalDateTime startTime,

        @NotNull(message = "End time is required")
        @Future(message = "End time must be in the future")
        LocalDateTime endTime,

        @NotNull(message = "Minimum seats is required")
        @Positive(message = "Minimum seats must be greater than zero")
        Integer minSeats,

        @NotNull(message = "Maximum seats is required")
        @Positive(message = "Maximum seats must be greater than zero")
        Integer maxSeats
) {
}
