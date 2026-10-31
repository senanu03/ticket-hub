package com.tp.snapseat.event_service.service;

import com.tp.snapseat.event_service.dto.response.SeatResponseDTO;

import java.util.List;

public interface SeatService {
    void generateSeats(Long screenId);

    List<SeatResponseDTO> getSeatsByScreen(Long screenId);

    List<SeatResponseDTO> getSeatsByRow(Long rowId);
}
