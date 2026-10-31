package com.tp.snapseat.booking_service.entity;

import com.tp.snapseat.booking_service.enums.BookingStatus;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "booking_status_history", indexes = @Index(name = "idx_history_booking", columnList = "booking_id"))
public class BookingStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private BookingStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus newStatus;

    @Column(nullable = false)
    private Instant changedAt;

    @Column(length = 255)
    private String reason;

    protected BookingStatusHistory() {
    }

    BookingStatusHistory(Booking booking, BookingStatus oldStatus, BookingStatus newStatus,
                         Instant changedAt, String reason) {
        this.booking = booking;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.changedAt = changedAt;
        this.reason = reason;
    }

    public Long getId() { return id; }
    public BookingStatus getOldStatus() { return oldStatus; }
    public BookingStatus getNewStatus() { return newStatus; }
    public Instant getChangedAt() { return changedAt; }
    public String getReason() { return reason; }
}
