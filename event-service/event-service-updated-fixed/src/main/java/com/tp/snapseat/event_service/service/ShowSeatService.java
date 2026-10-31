package com.tp.snapseat.event_service.service;

import com.tp.snapseat.event_service.dto.response.ShowSeatResponseDTO;

import java.util.List;

public interface ShowSeatService {
    void generateShowSeats(Long showId);

    List<ShowSeatResponseDTO> getSeatMap(Long showId);

    List<ShowSeatResponseDTO> getAvailableSeats(Long showId);
}
