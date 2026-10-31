package com.tp.snapseat.booking_service.dto;

import java.util.List;

public record PagedResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {
}
