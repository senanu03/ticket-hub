package com.tp.snapseat.event_service.service;

import com.tp.snapseat.event_service.dto.request.ShowPriceRequestDTO;
import com.tp.snapseat.event_service.dto.response.ShowPriceResponseDTO;
import jakarta.validation.Valid;

import java.util.List;

public interface ShowPriceService {
    ShowPriceResponseDTO createPrice(@Valid ShowPriceRequestDTO request);

    List<ShowPriceResponseDTO> getPricesByShow(Long showId);

    ShowPriceResponseDTO updatePrice(Long id, @Valid ShowPriceRequestDTO request);
}
