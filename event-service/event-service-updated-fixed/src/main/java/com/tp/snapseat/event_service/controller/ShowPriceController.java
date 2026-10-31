package com.tp.snapseat.event_service.controller;

import com.tp.snapseat.event_service.dto.request.ShowPriceRequestDTO;
import com.tp.snapseat.event_service.dto.response.ShowPriceResponseDTO;
import com.tp.snapseat.event_service.service.ShowPriceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/show-prices")
@RequiredArgsConstructor
public class ShowPriceController {

    private final ShowPriceService showPriceService;

    @PostMapping
    public ShowPriceResponseDTO create(@Valid @RequestBody ShowPriceRequestDTO request) {

        return showPriceService.createPrice(request);
    }

    @GetMapping("/show/{showId}")
    public List<ShowPriceResponseDTO> getByShow(@PathVariable Long showId) {

        return showPriceService.getPricesByShow(showId);
    }

    @PutMapping("/{id}")
    public ShowPriceResponseDTO update(@PathVariable Long id, @Valid @RequestBody ShowPriceRequestDTO request) {

        return showPriceService.updatePrice(id, request);
    }
}
