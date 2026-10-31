package com.tp.snapseat.event_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record VenueRequestDTO(
        @NotBlank(message = "Venue name is required")
        String name,

        String chain,

        @NotBlank(message = "City is required")
        String city,

        String address,

        String pincode,

        BigDecimal latitude,

        BigDecimal longitude,

        @NotNull(message = "Active status is required")
        Boolean active
) {
}
