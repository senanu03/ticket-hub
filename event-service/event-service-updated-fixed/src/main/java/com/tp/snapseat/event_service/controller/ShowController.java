package com.tp.snapseat.event_service.controller;

import com.tp.snapseat.event_service.dto.request.ShowRequestDTO;
import com.tp.snapseat.event_service.dto.response.ShowResponseDTO;
import com.tp.snapseat.event_service.dto.response.ShowSeatResponseDTO;
import com.tp.snapseat.event_service.service.ShowSeatService;
import com.tp.snapseat.event_service.service.ShowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shows")
@RequiredArgsConstructor
public class ShowController {

    private final ShowService showService;
    private final ShowSeatService showSeatService;

    @PostMapping
    public ShowResponseDTO create(@Valid @RequestBody ShowRequestDTO request) {
        return showService.createShow(request);
    }

    @GetMapping
    public Page<ShowResponseDTO> getAll(
            @PageableDefault(page = 0, size = 10, sort = "startTime") Pageable pageable) {
        return showService.getShows(pageable);
    }

    @GetMapping("/{id}")
    public ShowResponseDTO get(@PathVariable Long id) {
        return showService.getShow(id);
    }

    @GetMapping("/{showId}/seats")
    public List<ShowSeatResponseDTO> getSeats(@PathVariable Long showId) {
        return showSeatService.getSeatMap(showId);
    }

    @GetMapping("/event/{eventId}")
    public Page<ShowResponseDTO> getByEvent(
            @PathVariable Long eventId,
            @PageableDefault(page = 0, size = 10, sort = "startTime") Pageable pageable) {
        return showService.getShowsByEvent(eventId, pageable);
    }

    @PutMapping("/{id}")
    public ShowResponseDTO update(
            @PathVariable Long id,
            @Valid @RequestBody ShowRequestDTO request) {
        return showService.updateShow(id, request);
    }

    @PatchMapping("/{id}/cancel")
    public void cancel(@PathVariable Long id) {
        showService.cancelShow(id);
    }


    @PatchMapping("/{id}/open") public ShowResponseDTO open(@PathVariable Long id) { return showService.openShow(id); }
}
