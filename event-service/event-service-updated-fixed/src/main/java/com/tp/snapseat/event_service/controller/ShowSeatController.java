package com.tp.snapseat.event_service.controller;

import com.tp.snapseat.event_service.dto.response.ShowSeatResponseDTO;
import com.tp.snapseat.event_service.service.ShowSeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/show-seats")
@RequiredArgsConstructor
public class ShowSeatController {

    private final ShowSeatService showSeatService;

    @PostMapping("/generate/{showId}")
    public void generate(@PathVariable Long showId) {

        showSeatService.generateShowSeats(showId);
    }

    @GetMapping("/show/{showId}")
    public List<ShowSeatResponseDTO> getSeatMap(@PathVariable Long showId) {

        return showSeatService.getSeatMap(showId);
    }

    @GetMapping("/show/{showId}/available")
    public List<ShowSeatResponseDTO> getAvailableSeats(@PathVariable Long showId) {

        return showSeatService.getAvailableSeats(showId);
    }
}
