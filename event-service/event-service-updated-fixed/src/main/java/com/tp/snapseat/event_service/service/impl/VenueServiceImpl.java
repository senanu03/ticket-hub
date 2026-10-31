package com.tp.snapseat.event_service.service.impl;

import com.tp.snapseat.event_service.dto.request.VenueRequestDTO;
import com.tp.snapseat.event_service.dto.response.VenueResponseDTO;
import com.tp.snapseat.event_service.entity.Venue;
import com.tp.snapseat.event_service.exception.ResourceNotFoundException;
import com.tp.snapseat.event_service.mapper.EventMapper;
import com.tp.snapseat.event_service.repository.VenueRepository;
import com.tp.snapseat.event_service.service.VenueService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VenueServiceImpl implements VenueService {

    private final VenueRepository venueRepository;
    private final EventMapper mapper;

    @Override
    public VenueResponseDTO createVenue(VenueRequestDTO request) {

        Venue venue = new Venue();

        venue.setName(request.name());
        venue.setChain(request.chain());
        venue.setCity(request.city());
        venue.setAddress(request.address());
        venue.setPincode(request.pincode());
        venue.setLatitude(request.latitude());
        venue.setLongitude(request.longitude());
        venue.setActive(request.active());

        return mapper.toVenueResponse(venueRepository.save(venue));
    }

    @Override
    public VenueResponseDTO getVenue(Long id) {

        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found with id: " + id));

        return mapper.toVenueResponse(venue);
    }

    @Override
    public Page<VenueResponseDTO> getVenues(String city, Pageable pageable) {

        Page<Venue> venues;

        if (city == null || city.isBlank()) {
            venues = venueRepository.findByActiveTrue(pageable);
        }
        else {
            venues = venueRepository.findByCityIgnoreCaseAndActiveTrue(city, pageable);
        }

        return venues.map(mapper::toVenueResponse);
    }

    @Override
    public VenueResponseDTO updateVenue(Long id, VenueRequestDTO request) {

        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found with id: " + id));

        venue.setName(request.name());
        venue.setChain(request.chain());
        venue.setCity(request.city());
        venue.setAddress(request.address());
        venue.setPincode(request.pincode());
        venue.setLatitude(request.latitude());
        venue.setLongitude(request.longitude());
        venue.setActive(request.active());

        return mapper.toVenueResponse(venueRepository.save(venue));
    }

    @Override
    public void deleteVenue(Long id) {

        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found with id: " + id));

        venue.setActive(false);

        venueRepository.save(venue);
    }
}
