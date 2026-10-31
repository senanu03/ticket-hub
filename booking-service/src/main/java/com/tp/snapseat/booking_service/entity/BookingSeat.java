package com.tp.snapseat.booking_service.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "booking_seats", indexes = @Index(name = "idx_booking_seat_booking", columnList = "booking_id"))
public class BookingSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingSeatId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    /** Seat id from the Event/Show seat layout. */
    @Column(nullable = false)
    private Long seatId;

    /** Reference to the reservation row (show_seats) owned by this service. */
    @Column(nullable = false)
    private Long showSeatId;

    /** Price snapshot for this seat. */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    protected BookingSeat() {
    }

    BookingSeat(Booking booking, Long seatId, Long showSeatId, BigDecimal price) {
        this.booking = booking;
        this.seatId = seatId;
        this.showSeatId = showSeatId;
        this.price = price;
    }

    public Long getBookingSeatId() { return bookingSeatId; }
    public Booking getBooking() { return booking; }
    public Long getSeatId() { return seatId; }
    public Long getShowSeatId() { return showSeatId; }
    public BigDecimal getPrice() { return price; }
}
