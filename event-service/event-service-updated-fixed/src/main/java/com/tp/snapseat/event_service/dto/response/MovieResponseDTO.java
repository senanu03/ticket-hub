package com.tp.snapseat.event_service.dto.response;

public record MovieResponseDTO(
        Long id,
        String title,
        String language,
        Integer durationMin,
        String genre,
        String certificate,
        String posterUrl,
        Boolean active
) {
}
