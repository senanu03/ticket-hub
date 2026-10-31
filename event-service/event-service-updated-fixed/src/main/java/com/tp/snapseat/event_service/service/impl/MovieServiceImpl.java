package com.tp.snapseat.event_service.service.impl;

import com.tp.snapseat.event_service.dto.request.MovieRequestDTO;
import com.tp.snapseat.event_service.dto.response.MovieResponseDTO;
import com.tp.snapseat.event_service.entity.Movie;
import com.tp.snapseat.event_service.exception.ResourceNotFoundException;
import com.tp.snapseat.event_service.mapper.EventMapper;
import com.tp.snapseat.event_service.repository.MovieRepository;
import com.tp.snapseat.event_service.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;
    private final EventMapper mapper;

    @Override
    public MovieResponseDTO createMovie(MovieRequestDTO request) {

        Movie movie = new Movie();

        movie.setTitle(request.title());
        movie.setLanguage(request.language());
        movie.setDurationMin(request.durationMin());
        movie.setGenre(request.genre());
        movie.setCertificate(request.certificate());
        movie.setPosterUrl(request.posterUrl());
        movie.setActive(request.active());

        return mapper.toMovieResponse(movieRepository.save(movie));
    }

    @Override
    public MovieResponseDTO getMovie(Long id) {

        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + id));

        return mapper.toMovieResponse(movie);
    }

    @Override
    public List<MovieResponseDTO> getAllMovies() {

        return movieRepository.findByActiveTrue()
                .stream()
                .map(mapper::toMovieResponse)
                .toList();
    }

    @Override
    public MovieResponseDTO updateMovie(Long id, MovieRequestDTO request) {

        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + id));

        movie.setTitle(request.title());
        movie.setLanguage(request.language());
        movie.setDurationMin(request.durationMin());
        movie.setGenre(request.genre());
        movie.setCertificate(request.certificate());
        movie.setPosterUrl(request.posterUrl());
        movie.setActive(request.active());

        return mapper.toMovieResponse(movieRepository.save(movie));
    }

    @Override
    public void deleteMovie(Long id) {

        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + id));

        movie.setActive(false);

        movieRepository.save(movie);
    }
}
