package com.tp.snapseat.event_service.service.impl;

import com.tp.snapseat.event_service.dto.request.ScreenRequestDTO;
import com.tp.snapseat.event_service.dto.response.ScreenResponseDTO;
import com.tp.snapseat.event_service.entity.Screen;
import com.tp.snapseat.event_service.entity.Venue;
import com.tp.snapseat.event_service.exception.ResourceNotFoundException;
import com.tp.snapseat.event_service.mapper.EventMapper;
import com.tp.snapseat.event_service.repository.ScreenRepository;
import com.tp.snapseat.event_service.repository.VenueRepository;
import com.tp.snapseat.event_service.service.ScreenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ScreenServiceImpl implements ScreenService {

    private final ScreenRepository screenRepository;
    private final VenueRepository venueRepository;
    private final EventMapper mapper;

    @Override
    public ScreenResponseDTO createScreen(ScreenRequestDTO request) {

        Venue venue = venueRepository.findById(request.venueId())
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found with id: " + request.venueId()));

        Screen screen = new Screen();

        screen.setVenue(venue);
        screen.setName(request.name());
        screen.setScreenType(request.screenType());
        screen.setTotalRows(request.totalRows());
        screen.setTotalSeats(request.totalSeats());
        screen.setActive(request.active());

        return mapper.toScreenResponse(screenRepository.save(screen));
    }

    @Override
    public ScreenResponseDTO getScreen(Long id) {

        Screen screen = screenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Screen not found with id: " + id));

        return mapper.toScreenResponse(screen);
    }

    @Override
    public List<ScreenResponseDTO> getScreensByVenue(Long venueId) {

        return screenRepository
                .findByVenueIdAndActiveTrue(venueId)
                .stream()
                .map(mapper::toScreenResponse)
                .toList();
    }

    @Override
    public ScreenResponseDTO updateScreen(Long id, ScreenRequestDTO request) {

        Screen screen = screenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Screen not found with id: " + id));

        Venue venue = venueRepository.findById(request.venueId())
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found with id: " + request.venueId()));

        screen.setVenue(venue);
        screen.setName(request.name());
        screen.setScreenType(request.screenType());
        screen.setTotalRows(request.totalRows());
        screen.setTotalSeats(request.totalSeats());
        screen.setActive(request.active());

        return mapper.toScreenResponse(screenRepository.save(screen));
    }

    @Override
    public void deleteScreen(Long id) {

        Screen screen = screenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Screen not found with id: " + id));

        screen.setActive(false);

        screenRepository.save(screen);
    }
}
