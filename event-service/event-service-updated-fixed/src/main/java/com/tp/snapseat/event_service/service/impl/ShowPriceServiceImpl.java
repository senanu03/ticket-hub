package com.tp.snapseat.event_service.service.impl;

import com.tp.snapseat.event_service.dto.request.ShowPriceRequestDTO;
import com.tp.snapseat.event_service.dto.response.ShowPriceResponseDTO;
import com.tp.snapseat.event_service.entity.Show;
import com.tp.snapseat.event_service.entity.ShowPrice;
import com.tp.snapseat.event_service.exception.BusinessException;
import com.tp.snapseat.event_service.exception.ResourceNotFoundException;
import com.tp.snapseat.event_service.mapper.EventMapper;
import com.tp.snapseat.event_service.repository.ShowPriceRepository;
import com.tp.snapseat.event_service.repository.ShowRepository;
import com.tp.snapseat.event_service.service.ShowPriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShowPriceServiceImpl implements ShowPriceService {

    private final ShowPriceRepository showPriceRepository;
    private final ShowRepository showRepository;
    private final EventMapper mapper;

    @Override
    public ShowPriceResponseDTO createPrice(ShowPriceRequestDTO request) {

        Show show = showRepository.findById(request.showId())
                .orElseThrow(() -> new ResourceNotFoundException("Show not found with id: " + request.showId()));

        if (showPriceRepository.existsByShowIdAndSeatType(request.showId(), request.seatType())) {
            throw new BusinessException("Price already exists for seat type " + request.seatType());
        }

        ShowPrice price = new ShowPrice();

        price.setShow(show);
        price.setSeatType(request.seatType());
        price.setPrice(request.price());

        return mapper.toShowPriceResponse(showPriceRepository.save(price));
    }

    @Override
    public List<ShowPriceResponseDTO> getPricesByShow(Long showId) {

        return showPriceRepository
                .findByShowId(showId)
                .stream()
                .map(mapper::toShowPriceResponse)
                .toList();
    }

    @Override
    public ShowPriceResponseDTO updatePrice(Long id, ShowPriceRequestDTO request) {

        ShowPrice price = showPriceRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Show price not found with id: " + id));

        price.setSeatType(request.seatType());
        price.setPrice(request.price());

        return mapper.toShowPriceResponse(showPriceRepository.save(price));
    }
}
