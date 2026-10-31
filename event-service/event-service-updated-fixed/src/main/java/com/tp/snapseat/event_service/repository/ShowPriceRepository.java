package com.tp.snapseat.event_service.repository;

import com.tp.snapseat.event_service.entity.ShowPrice;
import com.tp.snapseat.event_service.enums.SeatType;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ShowPriceRepository extends JpaRepository<ShowPrice, Long> {
    boolean existsByShowIdAndSeatType(@NotNull(message = "Show ID is required") Long aLong, @NotNull(message = "Seat type is required") SeatType seatType);

    List<ShowPrice> findByShowId(Long showId);
}
