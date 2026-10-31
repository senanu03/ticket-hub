package com.tp.snapseat.event_service.service;

import com.tp.snapseat.event_service.dto.request.VenueRequestDTO;
import com.tp.snapseat.event_service.dto.response.VenueResponseDTO;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface VenueService {
    VenueResponseDTO createVenue(@Valid VenueRequestDTO request);

    VenueResponseDTO getVenue(Long id);

    Page<VenueResponseDTO> getVenues(String city, Pageable pageable);

    VenueResponseDTO updateVenue(Long id, @Valid VenueRequestDTO request);

    void deleteVenue(Long id);
}
