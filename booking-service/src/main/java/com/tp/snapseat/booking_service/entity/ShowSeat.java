package com.tp.snapseat.booking_service.entity;

import com.tp.snapseat.booking_service.enums.SeatStatus;
import jakarta.persistence.*;

import java.time.Instant;

/**
 * Reservation state of one seat of one show. Owned by Booking (Blueprint section 4).
 * The unique (show_id, seat_id) constraint plus row locks make double reservation impossible at DB level.
 */
@Entity
@Table(
        name = "show_seats",
        uniqueConstraints = @UniqueConstraint(name = "uk_show_seat", columnNames = {"show_id", "seat_id"}),
        indexes = @Index(name = "idx_show_seat_holder", columnList = "held_by_booking_id"))
public class ShowSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long showSeatId;

    @Column(name = "show_id", nullable = false)
    private Long showId;

    @Column(name = "seat_id", nullable = false)
    private Long seatId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SeatStatus status;

    /** Booking currently holding / owning this seat. */
    @Column(name = "held_by_booking_id")
    private Long heldByBookingId;

    private Instant lockedAt;

    /** A RESERVED seat whose expiry is in the past can be taken by another booking. */
    private Instant lockExpiry;

    protected ShowSeat() {
    }

    public static ShowSeat available(Long showId, Long seatId) {
        ShowSeat s = new ShowSeat();
        s.showId = showId;
        s.seatId = seatId;
        s.status = SeatStatus.AVAILABLE;
        return s;
    }

    public boolean isAvailableAt(Instant now) {
        if (status == SeatStatus.AVAILABLE) {
            return true;
        }
        return status == SeatStatus.RESERVED && lockExpiry != null && lockExpiry.isBefore(now);
    }

    public boolean isActivelyReservedAt(Instant now) {
        return status == SeatStatus.RESERVED && lockExpiry != null && !lockExpiry.isBefore(now);
    }

    public void reserve(Long bookingId, Instant now, Instant expiry) {
        this.status = SeatStatus.RESERVED;
        this.heldByBookingId = bookingId;
        this.lockedAt = now;
        this.lockExpiry = expiry;
    }

    public void release() {
        this.status = SeatStatus.AVAILABLE;
        this.heldByBookingId = null;
        this.lockedAt = null;
        this.lockExpiry = null;
    }

    public void markBooked() {
        this.status = SeatStatus.BOOKED;
        this.lockExpiry = null;
    }

    public Long getShowSeatId() { return showSeatId; }
    public Long getShowId() { return showId; }
    public Long getSeatId() { return seatId; }
    public SeatStatus getStatus() { return status; }
    public Long getHeldByBookingId() { return heldByBookingId; }
    public Instant getLockedAt() { return lockedAt; }
    public Instant getLockExpiry() { return lockExpiry; }
}
