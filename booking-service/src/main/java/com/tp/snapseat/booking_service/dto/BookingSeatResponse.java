package com.tp.snapseat.booking_service.dto;

import java.math.BigDecimal;

public record BookingSeatResponse(Long seatId, BigDecimal price) {
}
