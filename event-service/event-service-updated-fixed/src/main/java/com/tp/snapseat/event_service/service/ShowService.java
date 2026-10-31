package com.tp.snapseat.event_service.service;

import com.tp.snapseat.event_service.dto.request.ShowRequestDTO;
import com.tp.snapseat.event_service.dto.response.ShowResponseDTO;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ShowService {
    ShowResponseDTO createShow(@Valid ShowRequestDTO request);

    ShowResponseDTO getShow(Long id);

    Page<ShowResponseDTO> getShows(Pageable pageable);

    Page<ShowResponseDTO> getShowsByEvent(Long eventId, Pageable pageable);

    ShowResponseDTO updateShow(Long id, @Valid ShowRequestDTO request);

    void cancelShow(Long id);

    ShowResponseDTO openShow(Long id);
}
