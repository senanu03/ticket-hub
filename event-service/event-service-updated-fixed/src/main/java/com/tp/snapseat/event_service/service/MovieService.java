package com.tp.snapseat.event_service.service;

import com.tp.snapseat.event_service.dto.request.MovieRequestDTO;
import com.tp.snapseat.event_service.dto.response.MovieResponseDTO;
import jakarta.validation.Valid;

import java.util.List;

public interface MovieService {
    MovieResponseDTO createMovie(@Valid MovieRequestDTO request);

    MovieResponseDTO getMovie(Long id);

    List<MovieResponseDTO> getAllMovies();

    MovieResponseDTO updateMovie(Long id, @Valid MovieRequestDTO request);

    void deleteMovie(Long id);
}
