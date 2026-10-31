package com.tp.snapseat.event_service.controller;

import com.tp.snapseat.event_service.dto.request.ScreenRequestDTO;
import com.tp.snapseat.event_service.dto.response.ScreenResponseDTO;
import com.tp.snapseat.event_service.service.ScreenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/screens")
@RequiredArgsConstructor
public class ScreenController {

    private final ScreenService screenService;

    @PostMapping
    public ScreenResponseDTO create(@Valid @RequestBody ScreenRequestDTO request) {

        return screenService.createScreen(request);
    }

    @GetMapping("/{id}")
    public ScreenResponseDTO get(@PathVariable Long id) {

        return screenService.getScreen(id);
    }

    @GetMapping("/venue/{venueId}")
    public List<ScreenResponseDTO> getByVenue(@PathVariable Long venueId) {

        return screenService.getScreensByVenue(venueId);
    }

    @PutMapping("/{id}")
    public ScreenResponseDTO update(@PathVariable Long id, @Valid @RequestBody ScreenRequestDTO request) {

        return screenService.updateScreen(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {

        screenService.deleteScreen(id);
    }
}
