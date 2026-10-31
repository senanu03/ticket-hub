package com.tp.snapseat.event_service.service;

import com.tp.snapseat.event_service.dto.request.ScreenRequestDTO;
import com.tp.snapseat.event_service.dto.response.ScreenResponseDTO;
import jakarta.validation.Valid;

import java.util.List;

public interface ScreenService {
    ScreenResponseDTO createScreen(@Valid ScreenRequestDTO request);

    ScreenResponseDTO getScreen(Long id);

    List<ScreenResponseDTO> getScreensByVenue(Long venueId);

    ScreenResponseDTO updateScreen(Long id, @Valid ScreenRequestDTO request);

    void deleteScreen(Long id);
}
