package com.tp.snapseat.event_service.controller;

import com.tp.snapseat.event_service.dto.request.MovieRequestDTO;
import com.tp.snapseat.event_service.dto.response.MovieResponseDTO;
import com.tp.snapseat.event_service.service.MovieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    @PostMapping
    public ResponseEntity<MovieResponseDTO> create(@Valid @RequestBody MovieRequestDTO request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(movieService.createMovie(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovieResponseDTO> get(@PathVariable Long id) {

        return ResponseEntity.ok(
                movieService.getMovie(id)
        );
    }

    @GetMapping
    public ResponseEntity<List<MovieResponseDTO>> getAll() {

        return ResponseEntity.ok(
                movieService.getAllMovies()
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<MovieResponseDTO> update(@PathVariable Long id, @Valid @RequestBody MovieRequestDTO request) {

        return ResponseEntity.ok(
                movieService.updateMovie(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        movieService.deleteMovie(id);

        return ResponseEntity.noContent().build();
    }
}
