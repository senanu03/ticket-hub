package com.tp.snapseat.event_service.controller;

import com.tp.snapseat.event_service.dto.request.VenueRequestDTO;
import com.tp.snapseat.event_service.dto.response.VenueResponseDTO;
import com.tp.snapseat.event_service.service.VenueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/venues")
@RequiredArgsConstructor
public class VenueController {

    private final VenueService venueService;

    @PostMapping
    public VenueResponseDTO create(@Valid @RequestBody VenueRequestDTO request) {

        return venueService.createVenue(request);
    }

    @GetMapping("/{id}")
    public VenueResponseDTO get(@PathVariable Long id) {

        return venueService.getVenue(id);
    }

    @GetMapping
    public Page<VenueResponseDTO> getAll(@RequestParam(required = false) String city,

            @PageableDefault(
                    page = 0,
                    size = 10,
                    sort = "id"
            )
            Pageable pageable) {

        return venueService.getVenues(city, pageable);
    }

    @PutMapping("/{id}")
    public VenueResponseDTO update(@PathVariable Long id, @Valid @RequestBody VenueRequestDTO request) {

        return venueService.updateVenue(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {

        venueService.deleteVenue(id);
    }
}
