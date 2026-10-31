package com.tp.snapseat.event_service.service.impl;

import com.tp.snapseat.event_service.dto.request.ScreenRowRequestDTO;
import com.tp.snapseat.event_service.dto.response.ScreenRowResponseDTO;
import com.tp.snapseat.event_service.entity.Screen;
import com.tp.snapseat.event_service.entity.ScreenRow;
import com.tp.snapseat.event_service.exception.BusinessException;
import com.tp.snapseat.event_service.exception.ResourceNotFoundException;
import com.tp.snapseat.event_service.mapper.EventMapper;
import com.tp.snapseat.event_service.repository.ScreenRepository;
import com.tp.snapseat.event_service.repository.ScreenRowRepository;
import com.tp.snapseat.event_service.service.ScreenRowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ScreenRowServiceImpl implements ScreenRowService {

    private final ScreenRowRepository screenRowRepository;
    private final ScreenRepository screenRepository;
    private final EventMapper mapper;

    @Override
    public ScreenRowResponseDTO createRow(ScreenRowRequestDTO request) {

        Screen screen = screenRepository.findById(request.screenId())
                .orElseThrow(() -> new ResourceNotFoundException("Screen not found with id: " + request.screenId()));

        if (screenRowRepository.existsByScreenIdAndRowLabel(request.screenId(), request.rowLabel())) {
            throw new BusinessException("Row " + request.rowLabel() + " already exists in this screen");
        }

        ScreenRow row = new ScreenRow();

        row.setScreen(screen);
        row.setRowLabel(request.rowLabel());
        row.setSeatCount(request.seatCount());
        row.setSeatType(request.seatType());

        return mapper.toScreenRowResponse(screenRowRepository.save(row));
    }

    @Override
    public List<ScreenRowResponseDTO> getRowsByScreen(Long screenId) {

        return screenRowRepository
                .findByScreenIdOrderByRowLabelAsc(screenId)
                .stream()
                .map(mapper::toScreenRowResponse)
                .toList();
    }

    @Override
    public ScreenRowResponseDTO getRow(Long id) {

        ScreenRow row = screenRowRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Screen row not found with id: " + id));

        return mapper.toScreenRowResponse(row);
    }

    @Override
    public void deleteRow(Long id) {

        ScreenRow row = screenRowRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Screen row not found with id: " + id));

        screenRowRepository.delete(row);
    }
}
