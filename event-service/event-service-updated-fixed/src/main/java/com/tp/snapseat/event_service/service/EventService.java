package com.tp.snapseat.event_service.service;

import com.tp.snapseat.event_service.dto.request.EventRequestDTO;
import com.tp.snapseat.event_service.dto.response.EventResponseDTO;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EventService {
    EventResponseDTO createEvent(@Valid EventRequestDTO request);

    EventResponseDTO getEvent(Long id);

    Page<EventResponseDTO> getEvents(String search, Pageable pageable);

    EventResponseDTO updateEvent(Long id, @Valid EventRequestDTO request);

    EventResponseDTO publishEvent(Long id);

    EventResponseDTO cancelEvent(Long id);

    void deleteEvent(Long id);
}
