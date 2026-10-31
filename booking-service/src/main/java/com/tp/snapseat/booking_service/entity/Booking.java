package com.tp.snapseat.booking_service.entity;

import com.tp.snapseat.booking_service.enums.BookingStatus;
import com.tp.snapseat.booking_service.exception.InvalidBookingStateException;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "bookings",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_booking_customer_idempotency",
                columnNames = {"customer_id", "idempotency_key"}),
        indexes = {
                @Index(name = "idx_booking_status_expiry", columnList = "status,expires_at"),
                @Index(name = "idx_booking_customer", columnList = "customer_id")
        })
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingId;

    /** Authenticated principal (JWT "sub"). Never supplied by the client. */
    @Column(name = "customer_id", nullable = false, length = 64)
    private String customerId;

    /** Copied from the JWT "email" claim so the confirmation event can carry a recipient. */
    @Column(length = 254)
    private String customerEmail;

    @Column(name = "idempotency_key", nullable = false, length = 100)
    private String idempotencyKey;

    @Column(nullable = false)
    private Long showId;

    private Long eventId;

    /** Snapshot: show ticket price x number of seats at creation time. Never recalculated. */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private BookingStatus status;

    /** Payment Service reference that confirmed / failed this booking. */
    @Column(length = 64)
    private String paymentId;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant updatedAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    private Instant cancelledAt;

    @Version
    private Long version;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BookingSeat> seats = new ArrayList<>();

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<BookingStatusHistory> statusHistory = new ArrayList<>();

    protected Booking() {
    }

    public static Booking create(String customerId, String customerEmail, String idempotencyKey,
                                 Long showId, Long eventId, BigDecimal amount, String currency,
                                 Instant now, Instant expiresAt) {
        Booking b = new Booking();
        b.customerId = customerId;
        b.customerEmail = customerEmail;
        b.idempotencyKey = idempotencyKey;
        b.showId = showId;
        b.eventId = eventId;
        b.amount = amount;
        b.currency = currency;
        b.status = BookingStatus.PENDING_PAYMENT;
        b.createdAt = now;
        b.updatedAt = now;
        b.expiresAt = expiresAt;
        b.statusHistory.add(new BookingStatusHistory(b, null, BookingStatus.PENDING_PAYMENT, now, "Booking created"));
        return b;
    }

    public void addSeat(Long seatId, Long showSeatId, BigDecimal price) {
        seats.add(new BookingSeat(this, seatId, showSeatId, price));
    }

    /** The only way status changes: validated against the status model and audited. */
    public void changeStatus(BookingStatus next, String reason, Instant now) {
        if (!status.canTransitionTo(next)) {
            throw new InvalidBookingStateException(bookingId, status, next);
        }
        statusHistory.add(new BookingStatusHistory(this, status, next, now, reason));
        this.status = next;
        this.updatedAt = now;
        if (next == BookingStatus.CANCELLED) {
            this.cancelledAt = now;
        }
    }

    public boolean isExpiredAt(Instant now) {
        return !now.isBefore(expiresAt);
    }

    public List<Long> seatIds() {
        return seats.stream().map(BookingSeat::getSeatId).sorted().toList();
    }

    public Long getBookingId() { return bookingId; }
    public String getCustomerId() { return customerId; }
    public String getCustomerEmail() { return customerEmail; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public Long getShowId() { return showId; }
    public Long getEventId() { return eventId; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public BookingStatus getStatus() { return status; }
    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getCancelledAt() { return cancelledAt; }
    public List<BookingSeat> getSeats() { return seats; }
    public List<BookingStatusHistory> getStatusHistory() { return statusHistory; }
}
