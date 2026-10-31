package com.tp.snapseat.event_service.service.impl;

import com.tp.snapseat.event_service.dto.request.ShowRequestDTO;
import com.tp.snapseat.event_service.dto.response.ShowResponseDTO;
import com.tp.snapseat.event_service.entity.Event;
import com.tp.snapseat.event_service.entity.Screen;
import com.tp.snapseat.event_service.entity.Show;
import com.tp.snapseat.event_service.enums.EventStatus;
import com.tp.snapseat.event_service.enums.ShowStatus;
import com.tp.snapseat.event_service.exception.BusinessException;
import com.tp.snapseat.event_service.exception.ResourceNotFoundException;
import com.tp.snapseat.event_service.mapper.EventMapper;
import com.tp.snapseat.event_service.repository.EventRepository;
import com.tp.snapseat.event_service.repository.ScreenRepository;
import com.tp.snapseat.event_service.repository.ShowRepository;
import com.tp.snapseat.event_service.service.ShowService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShowServiceImpl implements ShowService {

    private final ShowRepository showRepository;
    private final EventRepository eventRepository;
    private final ScreenRepository screenRepository;
    private final EventMapper mapper;

    @Override
    public ShowResponseDTO createShow(ShowRequestDTO request) {
        validateTimeAndSeats(request);

        Event event = getPublishedEvent(request.eventId());
        Screen screen = getActiveScreen(request.screenId());

        if (showRepository.existsOverlappingShow(
                screen.getId(), request.startTime(), request.endTime())) {
            throw new BusinessException(
                    "Another show already exists in this screen during the requested time");
        }

        Show show = new Show();
        show.setEvent(event);
        show.setScreen(screen);
        show.setStartTime(request.startTime());
        show.setEndTime(request.endTime());
        show.setStatus(ShowStatus.SCHEDULED);
        show.setMinSeats(request.minSeats());
        show.setMaxSeats(request.maxSeats());
        show.setCreatedAt(LocalDateTime.now());

        return mapper.toShowResponse(showRepository.save(show));
    }

    @Override
    public ShowResponseDTO getShow(Long id) {
        Show show = showRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Show not found with id: " + id));

        return mapper.toShowResponse(show);
    }

    @Override
    public Page<ShowResponseDTO> getShows(Pageable pageable) {
        return showRepository.findByStatusIn(
                        List.of(ShowStatus.SCHEDULED, ShowStatus.OPEN, ShowStatus.SOLD_OUT),
                        pageable)
                .map(mapper::toShowResponse);
    }

    @Override
    public Page<ShowResponseDTO> getShowsByEvent(Long eventId, Pageable pageable) {
        getPublishedEvent(eventId);

        return showRepository.findByEventIdAndStatusIn(
                        eventId,
                        List.of(ShowStatus.SCHEDULED, ShowStatus.OPEN, ShowStatus.SOLD_OUT),
                        pageable)
                .map(mapper::toShowResponse);
    }

    @Override
    public ShowResponseDTO updateShow(Long id, ShowRequestDTO request) {
        validateTimeAndSeats(request);

        Show show = showRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Show not found with id: " + id));

        if (show.getStatus() == ShowStatus.CANCELLED || show.getStatus() == ShowStatus.COMPLETED) {
            throw new BusinessException("Cancelled or completed show cannot be updated");
        }

        Event event = getPublishedEvent(request.eventId());
        Screen screen = getActiveScreen(request.screenId());

        if (showRepository.existsOverlappingShowForUpdate(
                id, screen.getId(), request.startTime(), request.endTime())) {
            throw new BusinessException(
                    "Another show already exists in this screen during the requested time");
        }

        show.setEvent(event);
        show.setScreen(screen);
        show.setStartTime(request.startTime());
        show.setEndTime(request.endTime());
        show.setMinSeats(request.minSeats());
        show.setMaxSeats(request.maxSeats());

        return mapper.toShowResponse(showRepository.save(show));
    }

    @Override
    public void cancelShow(Long id) {
        Show show = showRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Show not found with id: " + id));

        if (show.getStatus() == ShowStatus.COMPLETED) {
            throw new BusinessException("Completed show cannot be cancelled");
        }

        show.setStatus(ShowStatus.CANCELLED);
        showRepository.save(show);
    }

    private Event getPublishedEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Event not found with id: " + eventId));

        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new BusinessException("Show can only be created for a published event");
        }

        return event;
    }

    private Screen getActiveScreen(Long screenId) {
        Screen screen = screenRepository.findById(screenId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Screen not found with id: " + screenId));

        if (!screen.getActive()) {
            throw new BusinessException("Cannot use an inactive screen");
        }

        return screen;
    }

    private void validateTimeAndSeats(ShowRequestDTO request) {
        if (!request.endTime().isAfter(request.startTime())) {
            throw new BusinessException("End time must be after start time");
        }

        if (request.minSeats() > request.maxSeats()) {
            throw new BusinessException("Minimum seats cannot be greater than maximum seats");
        }
    }




    @Override
    public ShowResponseDTO openShow(Long id) {
        Show show = showRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Show not found with id: " + id));

        if (show.getStatus() != ShowStatus.SCHEDULED) {
            throw new BusinessException(
                    "Only a scheduled show can be opened");
        }

        show.setStatus(ShowStatus.OPEN);

        return mapper.toShowResponse(showRepository.save(show));
    }


}
