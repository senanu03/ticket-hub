package com.tp.snapseat.booking_service.repository;

import com.tp.snapseat.booking_service.entity.Booking;
import com.tp.snapseat.booking_service.enums.BookingStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByCustomerIdAndIdempotencyKey(String customerId, String idempotencyKey);

    /** Ownership is enforced in the query itself (ST-59). */
    Optional<Booking> findByBookingIdAndCustomerId(Long bookingId, String customerId);

    Page<Booking> findByCustomerId(String customerId, Pageable pageable);

    /** Row lock so payment results, cancel and expiry on the same booking are serialised. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Booking b where b.bookingId = :bookingId")
    Optional<Booking> findByIdForUpdate(@Param("bookingId") Long bookingId);

    @Query("select b.bookingId from Booking b where b.status = :status and b.expiresAt < :now order by b.expiresAt")
    List<Long> findExpiredIds(@Param("status") BookingStatus status, @Param("now") Instant now, Pageable pageable);
}
