package com.tp.snapseat.event_service.service;

import com.tp.snapseat.event_service.dto.request.ScreenRowRequestDTO;
import com.tp.snapseat.event_service.dto.response.ScreenRowResponseDTO;
import jakarta.validation.Valid;

import java.util.List;

public interface ScreenRowService {
    ScreenRowResponseDTO createRow(@Valid ScreenRowRequestDTO request);

    ScreenRowResponseDTO getRow(Long id);

    List<ScreenRowResponseDTO> getRowsByScreen(Long screenId);

    void deleteRow(Long id);
}
