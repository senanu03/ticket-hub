package com.tp.snapseat.event_service.repository;

import com.tp.snapseat.event_service.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByScreenIdAndActiveTrue(Long screenId);

    List<Seat> findByScreenRowIdAndActiveTrue(Long rowId);

    boolean existsByScreenIdAndSeatLabel(Long screenId, String seatLabel);
}
