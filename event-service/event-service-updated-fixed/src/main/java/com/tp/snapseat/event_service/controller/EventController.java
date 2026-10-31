package com.tp.snapseat.event_service.controller;

import com.tp.snapseat.event_service.dto.request.EventRequestDTO;
import com.tp.snapseat.event_service.dto.response.EventResponseDTO;
import com.tp.snapseat.event_service.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping
    public EventResponseDTO create(@Valid @RequestBody EventRequestDTO request) {
        return eventService.createEvent(request);
    }

    @GetMapping("/{id}")
    public EventResponseDTO get(@PathVariable Long id) {
        return eventService.getEvent(id);
    }

    @GetMapping
    public Page<EventResponseDTO> getAll(
            @RequestParam(required = false) String search,
            @PageableDefault(page = 0, size = 10, sort = "id") Pageable pageable) {
        return eventService.getEvents(search, pageable);
    }

    @PutMapping("/{id}")
    public EventResponseDTO update(
            @PathVariable Long id,
            @Valid @RequestBody EventRequestDTO request) {
        return eventService.updateEvent(id, request);
    }

    @PatchMapping("/{id}/publish")
    public EventResponseDTO publish(@PathVariable Long id) {
        return eventService.publishEvent(id);
    }

    @PatchMapping("/{id}/cancel")
    public EventResponseDTO cancel(@PathVariable Long id) {
        return eventService.cancelEvent(id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        eventService.deleteEvent(id);
    }
}
