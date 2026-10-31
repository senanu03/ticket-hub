package com.tp.snapseat.event_service.repository;

import com.tp.snapseat.event_service.entity.ShowSeat;
import com.tp.snapseat.event_service.enums.SeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ShowSeatRepository extends JpaRepository<ShowSeat, Long> {
    boolean existsByShowIdAndSeatId(Long showId, Long seatId);

    List<ShowSeat> findByShowId(Long showId);

    List<ShowSeat> findByShowIdAndStatus(Long showId, SeatStatus seatStatus);
}
