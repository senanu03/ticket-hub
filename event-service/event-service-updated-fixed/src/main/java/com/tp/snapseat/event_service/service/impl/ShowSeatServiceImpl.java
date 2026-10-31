package com.tp.snapseat.event_service.service.impl;

import com.tp.snapseat.event_service.dto.response.ShowSeatResponseDTO;
import com.tp.snapseat.event_service.entity.Seat;
import com.tp.snapseat.event_service.entity.Show;
import com.tp.snapseat.event_service.entity.ShowPrice;
import com.tp.snapseat.event_service.entity.ShowSeat;
import com.tp.snapseat.event_service.enums.SeatStatus;
import com.tp.snapseat.event_service.enums.SeatType;
import com.tp.snapseat.event_service.exception.BusinessException;
import com.tp.snapseat.event_service.exception.ResourceNotFoundException;
import com.tp.snapseat.event_service.mapper.EventMapper;
import com.tp.snapseat.event_service.repository.SeatRepository;
import com.tp.snapseat.event_service.repository.ShowPriceRepository;
import com.tp.snapseat.event_service.repository.ShowRepository;
import com.tp.snapseat.event_service.repository.ShowSeatRepository;
import com.tp.snapseat.event_service.service.ShowSeatService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShowSeatServiceImpl implements ShowSeatService {

    private final ShowSeatRepository showSeatRepository;
    private final ShowRepository showRepository;
    private final SeatRepository seatRepository;
    private final ShowPriceRepository showPriceRepository;
    private final EventMapper mapper;

    @Override
    @Transactional
    public void generateShowSeats(Long showId) {

        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new ResourceNotFoundException("Show not found with id: " + showId));

        List<Seat> seats = seatRepository.findByScreenIdAndActiveTrue(show.getScreen().getId());

        if (seats.isEmpty()) {
            throw new BusinessException("No seats configured for the show screen");
        }

        List<ShowPrice> prices = showPriceRepository.findByShowId(showId);

        Map<SeatType, java.math.BigDecimal> priceMap = prices.stream()
                .collect(Collectors.toMap(ShowPrice::getSeatType, ShowPrice::getPrice));

        for (Seat seat : seats) {

            if (showSeatRepository.existsByShowIdAndSeatId(showId, seat.getId())) {
                continue;
            }

            var price = priceMap.get(seat.getSeatType());

            if (price == null) {
                throw new BusinessException("Price not configured for seat type " + seat.getSeatType());
            }

            ShowSeat showSeat = new ShowSeat();

            showSeat.setShow(show);
            showSeat.setSeat(seat);
            showSeat.setStatus(SeatStatus.AVAILABLE);
            showSeat.setPrice(price);

            showSeatRepository.save(showSeat);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShowSeatResponseDTO> getSeatMap(Long showId) {

        if (!showRepository.existsById(showId)) {
            throw new ResourceNotFoundException("Show not found with id: " + showId);
        }

        return showSeatRepository
                .findByShowId(showId)
                .stream()
                .map(mapper::toShowSeatResponse)
                .toList();
    }



    @Override
    @Transactional(readOnly = true)
    public List<ShowSeatResponseDTO> getAvailableSeats(Long showId) {

        if (!showRepository.existsById(showId)) {
            throw new ResourceNotFoundException("Show not found with id: " + showId);
        }

        return showSeatRepository
                .findByShowIdAndStatus(showId, SeatStatus.AVAILABLE)
                .stream()
                .map(mapper::toShowSeatResponse)
                .toList();
    }
}
