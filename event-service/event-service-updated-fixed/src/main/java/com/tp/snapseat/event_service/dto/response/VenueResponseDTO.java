package com.tp.snapseat.event_service.dto.response;

import java.math.BigDecimal;

public record VenueResponseDTO(
        Long id,
        String name,
        String chain,
        String city,
        String address,
        String pincode,
        BigDecimal latitude,
        BigDecimal longitude,
        Boolean active
) {
}
