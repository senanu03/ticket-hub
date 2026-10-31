package com.tp.snapseat.event_service.mapper;

import com.tp.snapseat.event_service.dto.response.*;
import com.tp.snapseat.event_service.entity.*;
import org.springframework.stereotype.Component;

@Component
public class EventMapper {
    public MovieResponseDTO toMovieResponse(Movie movie) {

        return new MovieResponseDTO(
                movie.getId(),
                movie.getTitle(),
                movie.getLanguage(),
                movie.getDurationMin(),
                movie.getGenre(),
                movie.getCertificate(),
                movie.getPosterUrl(),
                movie.getActive()
        );
    }

    public EventResponseDTO toEventResponse(Event event) {

        return new EventResponseDTO(
                event.getId(),
                event.getMovie().getId(),
                event.getTitle(),
                event.getType(),
                event.getDescription(),
                event.getStatus()
        );
    }

    public VenueResponseDTO toVenueResponse(Venue venue) {

        return new VenueResponseDTO(
                venue.getId(),
                venue.getName(),
                venue.getChain(),
                venue.getCity(),
                venue.getAddress(),
                venue.getPincode(),
                venue.getLatitude(),
                venue.getLongitude(),
                venue.getActive()
        );
    }

    public ScreenResponseDTO toScreenResponse(Screen screen) {

        return new ScreenResponseDTO(
                screen.getId(),
                screen.getVenue().getId(),
                screen.getName(),
                screen.getScreenType(),
                screen.getTotalRows(),
                screen.getTotalSeats(),
                screen.getActive()
        );
    }

    public ScreenRowResponseDTO toScreenRowResponse(ScreenRow row) {

        return new ScreenRowResponseDTO(
                row.getId(),
                row.getScreen().getId(),
                row.getRowLabel(),
                row.getSeatCount(),
                row.getSeatType()
        );
    }

    public SeatResponseDTO toSeatResponse(Seat seat) {

        return new SeatResponseDTO(
                seat.getId(),
                seat.getScreen().getId(),
                seat.getScreenRow().getId(),
                seat.getSeatLabel(),
                seat.getSeatType(),
                seat.getActive()
        );
    }

    public ShowResponseDTO toShowResponse(Show show) {

        return new ShowResponseDTO(
                show.getId(),
                show.getEvent() != null ? show.getEvent().getId() : null,
                show.getEvent() != null && show.getEvent().getStatus() != null ? show.getEvent().getStatus().name() : null,
                show.getScreen() != null ? show.getScreen().getId() : null,
                show.getStartTime(),
                show.getEndTime(),
                show.getStatus(),
                show.getMinSeats(),
                show.getMaxSeats(),
                show.getVersion(),
                show.getCreatedAt()
        );
    }

    public ShowPriceResponseDTO toShowPriceResponse(ShowPrice price) {

        return new ShowPriceResponseDTO(
                price.getId(),
                price.getShow().getId(),
                price.getSeatType(),
                price.getPrice()
        );
    }

    public ShowSeatResponseDTO toShowSeatResponse(ShowSeat showSeat) {

        return new ShowSeatResponseDTO(
                showSeat.getId(),
                showSeat.getShow().getId(),
                showSeat.getSeat().getId(),
                showSeat.getSeat().getSeatLabel(),
                showSeat.getSeat().getSeatType(),
                showSeat.getStatus(),
                showSeat.getPrice(),
                showSeat.getVersion()
        );
    }
}
