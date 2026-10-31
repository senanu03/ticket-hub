package com.tp.snapseat.booking_service.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ShowResponse(
        Long id,
        Long eventId,
        String eventStatus,
        Long screenId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String status,
        Integer minSeats,
        Integer maxSeats,
        Long version,
        LocalDateTime createdAt
) {
    public ShowResponse(Long id, Long eventId, Long screenId, LocalDateTime startTime, LocalDateTime endTime,
                        String status, Integer minSeats, Integer maxSeats, Long version, LocalDateTime createdAt) {
        this(id, eventId, "PUBLISHED", screenId, startTime, endTime, status, minSeats, maxSeats, version, createdAt);
    }
}
