package com.tp.snapseat.event_service.controller;

import com.tp.snapseat.event_service.dto.response.SeatResponseDTO;
import com.tp.snapseat.event_service.service.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seats")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;

    @PostMapping("/generate/{screenId}")
    public void generate(
            @PathVariable Long screenId) {

        seatService.generateSeats(screenId);
    }

    @GetMapping("/screen/{screenId}")
    public List<SeatResponseDTO> getByScreen(
            @PathVariable Long screenId) {

        return seatService.getSeatsByScreen(screenId);
    }

    @GetMapping("/row/{rowId}")
    public List<SeatResponseDTO> getByRow(
            @PathVariable Long rowId) {

        return seatService.getSeatsByRow(rowId);
    }
}
