package com.tp.snapseat.event_service.service.impl;

import com.tp.snapseat.event_service.dto.response.SeatResponseDTO;
import com.tp.snapseat.event_service.entity.Screen;
import com.tp.snapseat.event_service.entity.ScreenRow;
import com.tp.snapseat.event_service.entity.Seat;
import com.tp.snapseat.event_service.exception.BusinessException;
import com.tp.snapseat.event_service.exception.ResourceNotFoundException;
import com.tp.snapseat.event_service.mapper.EventMapper;
import com.tp.snapseat.event_service.repository.ScreenRepository;
import com.tp.snapseat.event_service.repository.ScreenRowRepository;
import com.tp.snapseat.event_service.repository.SeatRepository;
import com.tp.snapseat.event_service.service.SeatService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SeatServiceImpl implements SeatService {

    private final SeatRepository seatRepository;
    private final ScreenRepository screenRepository;
    private final ScreenRowRepository screenRowRepository;
    private final EventMapper mapper;

    @Override
    public List<SeatResponseDTO> getSeatsByScreen(Long screenId) {

        return seatRepository
                .findByScreenIdAndActiveTrue(screenId)
                .stream()
                .map(mapper::toSeatResponse)
                .toList();
    }

    @Override
    public List<SeatResponseDTO> getSeatsByRow(Long rowId) {

        return seatRepository
                .findByScreenRowIdAndActiveTrue(rowId)
                .stream()
                .map(mapper::toSeatResponse)
                .toList();
    }

    @Override
    @Transactional
    public void generateSeats(Long screenId) {

        Screen screen = screenRepository.findById(screenId)
                .orElseThrow(() -> new ResourceNotFoundException("Screen not found with id: " + screenId));

        List<ScreenRow> rows = screenRowRepository.findByScreenId(screenId);

        if (rows.isEmpty()) {
            throw new BusinessException("No rows configured for this screen");
        }

        for (ScreenRow row : rows) {

            for (int i = 1; i <= row.getSeatCount(); i++) {

                String seatLabel = row.getRowLabel() + i;

                if (seatRepository.existsByScreenIdAndSeatLabel(screenId, seatLabel)) {
                    continue;
                }

                Seat seat = new Seat();

                seat.setScreen(screen);
                seat.setScreenRow(row);
                seat.setSeatLabel(seatLabel);
                seat.setSeatType(row.getSeatType());
                seat.setActive(true);

                seatRepository.save(seat);
            }
        }
    }
}
