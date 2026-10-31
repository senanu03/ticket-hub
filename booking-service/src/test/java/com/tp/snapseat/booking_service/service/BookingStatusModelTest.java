package com.tp.snapseat.booking_service.service;

import com.tp.snapseat.booking_service.entity.Booking;
import com.tp.snapseat.booking_service.enums.BookingStatus;
import com.tp.snapseat.booking_service.exception.InvalidBookingStateException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Every status transition follows the Blueprint status model; terminal states never move. */
class BookingStatusModelTest {

    private Booking pending() {
        Instant now = Instant.parse("2026-10-05T10:00:00Z");
        return Booking.create("alice", null, "key", 1L, 1L, new BigDecimal("10.00"), "INR", now, now.plusSeconds(600));
    }

    @Test
    void pendingPaymentCanMoveToEveryTerminalState() {
        for (BookingStatus target : new BookingStatus[]{BookingStatus.CONFIRMED, BookingStatus.PAYMENT_FAILED,
                BookingStatus.CANCELLED, BookingStatus.EXPIRED}) {
            Booking b = pending();
            b.changeStatus(target, "test", Instant.now());
            assertThat(b.getStatus()).isEqualTo(target);
            assertThat(b.getStatusHistory()).hasSize(2);
        }
    }

    @Test
    void terminalStatesCannotMove() {
        Booking b = pending();
        b.changeStatus(BookingStatus.CONFIRMED, "paid", Instant.now());

        assertThatThrownBy(() -> b.changeStatus(BookingStatus.CANCELLED, "no", Instant.now()))
                .isInstanceOf(InvalidBookingStateException.class);
        assertThatThrownBy(() -> b.changeStatus(BookingStatus.PENDING_PAYMENT, "no", Instant.now()))
                .isInstanceOf(InvalidBookingStateException.class);
    }
}
