package com.tp.snapseat.event_service.controller;

import com.tp.snapseat.event_service.dto.request.ScreenRowRequestDTO;
import com.tp.snapseat.event_service.dto.response.ScreenRowResponseDTO;
import com.tp.snapseat.event_service.service.ScreenRowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/screen-rows")
@RequiredArgsConstructor
public class ScreenRowController {

    private final ScreenRowService screenRowService;

    @PostMapping
    public ScreenRowResponseDTO create(@Valid @RequestBody ScreenRowRequestDTO request) {

        return screenRowService.createRow(request);
    }

    @GetMapping("/{id}")
    public ScreenRowResponseDTO get(@PathVariable Long id) {

        return screenRowService.getRow(id);
    }

    @GetMapping("/screen/{screenId}")
    public List<ScreenRowResponseDTO> getByScreen(@PathVariable Long screenId) {

        return screenRowService.getRowsByScreen(screenId);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {

        screenRowService.deleteRow(id);
    }
}
