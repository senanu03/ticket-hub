package com.tp.snapseat.booking_service.repository;

import com.tp.snapseat.booking_service.entity.ShowSeat;
import com.tp.snapseat.booking_service.enums.SeatStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ShowSeatRepository extends JpaRepository<ShowSeat, Long> {

    /** Locks the rows in ascending seat order (consistent order avoids deadlocks). */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from ShowSeat s where s.showId = :showId and s.seatId in :seatIds order by s.seatId")
    List<ShowSeat> lockSeats(@Param("showId") Long showId, @Param("seatIds") Collection<Long> seatIds);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from ShowSeat s where s.heldByBookingId = :bookingId and s.status = :status order by s.showId, s.seatId")
    List<ShowSeat> lockHeldBy(@Param("bookingId") Long bookingId, @Param("status") SeatStatus status);

    @Query("select s.seatId from ShowSeat s where s.showId = :showId and s.seatId in :seatIds")
    List<Long> findExistingSeatIds(@Param("showId") Long showId, @Param("seatIds") Collection<Long> seatIds);

    List<ShowSeat> findByShowIdAndStatusIn(Long showId, Collection<SeatStatus> statuses);
}
