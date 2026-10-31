package com.tp.snapseat.event_service.service.impl;

import com.tp.snapseat.event_service.dto.request.EventRequestDTO;
import com.tp.snapseat.event_service.dto.response.EventResponseDTO;
import com.tp.snapseat.event_service.entity.Event;
import com.tp.snapseat.event_service.entity.Movie;
import com.tp.snapseat.event_service.enums.EventStatus;
import com.tp.snapseat.event_service.exception.BusinessException;
import com.tp.snapseat.event_service.exception.ResourceNotFoundException;
import com.tp.snapseat.event_service.mapper.EventMapper;
import com.tp.snapseat.event_service.repository.EventRepository;
import com.tp.snapseat.event_service.repository.MovieRepository;
import com.tp.snapseat.event_service.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final MovieRepository movieRepository;
    private final EventMapper mapper;

    @Override
    public EventResponseDTO createEvent(EventRequestDTO request) {
        Movie movie = movieRepository.findById(request.movieId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Movie not found with id: " + request.movieId()));

        Event event = new Event();
        event.setMovie(movie);
        event.setTitle(request.title());
        event.setType(request.type());
        event.setDescription(request.description());
        event.setStatus(EventStatus.DRAFT);

        return mapper.toEventResponse(eventRepository.save(event));
    }

    @Override
    public EventResponseDTO getEvent(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Event not found with id: " + id));

        return mapper.toEventResponse(event);
    }

    @Override
    public Page<EventResponseDTO> getEvents(String search, Pageable pageable) {
        Page<Event> events;

        if (search == null || search.isBlank()) {
            events = eventRepository.findByStatus(EventStatus.PUBLISHED, pageable);
        } else {
            events = eventRepository.findByStatusAndTitleContainingIgnoreCase(
                    EventStatus.PUBLISHED, search, pageable);
        }

        return events.map(mapper::toEventResponse);
    }

    @Override
    public EventResponseDTO updateEvent(Long id, EventRequestDTO request) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Event not found with id: " + id));

        if (event.getStatus() == EventStatus.CANCELLED) {
            throw new BusinessException("Cancelled event cannot be updated");
        }

        Movie movie = movieRepository.findById(request.movieId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Movie not found with id: " + request.movieId()));

        event.setMovie(movie);
        event.setTitle(request.title());
        event.setType(request.type());
        event.setDescription(request.description());

        return mapper.toEventResponse(eventRepository.save(event));
    }

    @Override
    public EventResponseDTO publishEvent(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Event not found with id: " + id));

        if (event.getStatus() == EventStatus.CANCELLED) {
            throw new BusinessException("Cancelled event cannot be published");
        }

        event.setStatus(EventStatus.PUBLISHED);
        return mapper.toEventResponse(eventRepository.save(event));
    }

    @Override
    public EventResponseDTO cancelEvent(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Event not found with id: " + id));

        event.setStatus(EventStatus.CANCELLED);
        return mapper.toEventResponse(eventRepository.save(event));
    }

    @Override
    public void deleteEvent(Long id) {
        cancelEvent(id);
    }
}
