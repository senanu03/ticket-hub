package com.tp.snapseat.event_service.dto.request;

import com.tp.snapseat.event_service.enums.SeatType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ShowPriceRequestDTO(
        @NotNull(message = "Show ID is required")
        Long showId,

        @NotNull(message = "Seat type is required")
        SeatType seatType,

        @NotNull(message = "Price is required")
        @DecimalMin(
                value = "0.0",
                inclusive = false,
                message = "Price must be greater than zero"
        )
        BigDecimal price
) {
}
