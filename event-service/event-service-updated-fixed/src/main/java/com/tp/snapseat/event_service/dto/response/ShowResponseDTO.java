package com.tp.snapseat.event_service.dto.response;

import com.tp.snapseat.event_service.enums.ShowStatus;

import java.time.LocalDateTime;

@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public record ShowResponseDTO(
        Long id,
        Long eventId,
        String eventStatus,
        Long screenId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        ShowStatus status,
        Integer minSeats,
        Integer maxSeats,
        Long version,
        LocalDateTime createdAt
) {
    public ShowResponseDTO(Long id, Long eventId, Long screenId, LocalDateTime startTime, LocalDateTime endTime,
                           ShowStatus status, Integer minSeats, Integer maxSeats, Long version, LocalDateTime createdAt) {
        this(id, eventId, null, screenId, startTime, endTime, status, minSeats, maxSeats, version, createdAt);
    }
}
