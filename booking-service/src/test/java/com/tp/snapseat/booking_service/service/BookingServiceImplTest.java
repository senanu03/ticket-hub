package com.tp.snapseat.booking_service.service;

import com.tp.snapseat.booking_service.client.EventServiceClient;
import com.tp.snapseat.booking_service.client.SeatLayoutResponse;
import com.tp.snapseat.booking_service.client.ShowResponse;
import com.tp.snapseat.booking_service.config.BookingProperties;
import com.tp.snapseat.booking_service.dto.*;
import com.tp.snapseat.booking_service.entity.Booking;
import com.tp.snapseat.booking_service.entity.ShowSeat;
import com.tp.snapseat.booking_service.enums.BookingStatus;
import com.tp.snapseat.booking_service.enums.PaymentResultOutcome;
import com.tp.snapseat.booking_service.enums.PaymentResultStatus;
import com.tp.snapseat.booking_service.enums.SeatStatus;
import com.tp.snapseat.booking_service.event.BookingConfirmedEvent;
import com.tp.snapseat.booking_service.exception.*;
import com.tp.snapseat.booking_service.repository.BookingRepository;
import com.tp.snapseat.booking_service.repository.ShowSeatRepository;
import com.tp.snapseat.booking_service.security.CurrentUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/** Business-rule tests with in-memory fakes for the repositories (no database, no Spring context). */
class BookingServiceImplTest {

    private static final Long SHOW_ID = 77L;
    private static final CurrentUser ALICE = new CurrentUser("alice", "alice@example.com", false);
    private static final CurrentUser BOB = new CurrentUser("bob", "bob@example.com", false);

    private final Map<Long, Booking> bookings = new LinkedHashMap<>();
    private final Map<Long, ShowSeat> seats = new LinkedHashMap<>();
    private long bookingSequence = 0;

    private BookingRepository bookingRepository;
    private ShowSeatRepository showSeatRepository;
    private EventServiceClient eventClient;
    private ApplicationEventPublisher eventPublisher;
    private MutableClock clock;
    private BookingServiceImpl service;

    @BeforeEach
    void setUp() {
        bookings.clear();
        seats.clear();
        bookingSequence = 0;

        bookingRepository = mock(BookingRepository.class);
        showSeatRepository = mock(ShowSeatRepository.class);
        eventClient = mock(EventServiceClient.class);
        eventPublisher = mock(ApplicationEventPublisher.class);
        clock = new MutableClock(Instant.parse("2026-10-05T10:00:00Z"));

        for (long seatId = 1; seatId <= 5; seatId++) {
            ShowSeat seat = ShowSeat.available(SHOW_ID, seatId);
            ReflectionTestUtils.setField(seat, "showSeatId", 1000 + seatId);
            seats.put(seatId, seat);
        }

        when(eventClient.getShow(SHOW_ID)).thenReturn(show("OPEN", "PUBLISHED", "250.00"));
        when(eventClient.getSeatLayout(SHOW_ID)).thenReturn(List.of(
                new SeatLayoutResponse(1L, "A", 1, "STD"), new SeatLayoutResponse(2L, "A", 2, "STD"),
                new SeatLayoutResponse(3L, "A", 3, "STD"), new SeatLayoutResponse(4L, "A", 4, "STD"),
                new SeatLayoutResponse(5L, "A", 5, "STD")));

        when(showSeatRepository.lockSeats(eq(SHOW_ID), anyCollection())).thenAnswer(inv -> {
            Collection<Long> ids = inv.getArgument(1);
            return seats.values().stream().filter(s -> ids.contains(s.getSeatId())).toList();
        });
        when(showSeatRepository.lockHeldBy(any(), any())).thenAnswer(inv -> {
            Long bookingId = inv.getArgument(0);
            SeatStatus status = inv.getArgument(1);
            return seats.values().stream()
                    .filter(s -> bookingId.equals(s.getHeldByBookingId()) && s.getStatus() == status).toList();
        });
        when(showSeatRepository.findByShowIdAndStatusIn(eq(SHOW_ID), anyCollection())).thenAnswer(inv -> {
            Collection<SeatStatus> statuses = inv.getArgument(1);
            return seats.values().stream().filter(s -> statuses.contains(s.getStatus())).toList();
        });

        org.mockito.stubbing.Answer<Booking> saveAnswer = inv -> {
            Booking b = inv.getArgument(0);
            if (b.getBookingId() == null) {
                ReflectionTestUtils.setField(b, "bookingId", ++bookingSequence);
            }
            bookings.put(b.getBookingId(), b);
            return b;
        };
        when(bookingRepository.save(any(Booking.class))).thenAnswer(saveAnswer);
        when(bookingRepository.saveAndFlush(any(Booking.class))).thenAnswer(saveAnswer);
        when(bookingRepository.findByIdForUpdate(any())).thenAnswer(inv -> Optional.ofNullable(bookings.get((Long) inv.getArgument(0))));
        when(bookingRepository.findByBookingIdAndCustomerId(any(), any())).thenAnswer(inv ->
                Optional.ofNullable(bookings.get((Long) inv.getArgument(0)))
                        .filter(b -> b.getCustomerId().equals(inv.getArgument(1))));
        when(bookingRepository.findByCustomerIdAndIdempotencyKey(any(), any())).thenAnswer(inv ->
                bookings.values().stream()
                        .filter(b -> b.getCustomerId().equals(inv.getArgument(0))
                                && b.getIdempotencyKey().equals(inv.getArgument(1))).findFirst());
        when(bookingRepository.findExpiredIds(any(), any(Instant.class), any())).thenAnswer(inv -> {
            Instant now = inv.getArgument(1);
            return bookings.values().stream()
                    .filter(b -> b.getStatus() == BookingStatus.PENDING_PAYMENT && b.getExpiresAt().isBefore(now))
                    .map(Booking::getBookingId).toList();
        });

        service = new BookingServiceImpl(bookingRepository, showSeatRepository, eventClient,
                mock(SeatInitializer.class), eventPublisher, new BookingProperties(), clock, new NoOpTransactionManager());
    }

    // ---------------------------------------------------------------- create

    @Test
    void createBooking_reservesSeats_andSnapshotsAmountFromShowPrice() {
        BookingResponse response = service.createBooking(ALICE, "key-1", new BookingRequest(SHOW_ID, List.of(2L, 1L)));

        assertThat(response.status()).isEqualTo(BookingStatus.PENDING_PAYMENT);
        assertThat(response.amount()).isEqualByComparingTo("500.00");   // 2 x 250.00, derived server-side
        assertThat(response.currency()).isEqualTo("INR");
        assertThat(response.customerId()).isEqualTo("alice");            // identity from principal
        assertThat(response.expiresAt()).isEqualTo(clock.instant().plus(Duration.ofMinutes(10)));
        assertThat(response.seats()).extracting(BookingSeatResponse::seatId).containsExactly(1L, 2L);
        assertThat(seats.get(1L).getStatus()).isEqualTo(SeatStatus.RESERVED);
        assertThat(seats.get(2L).getHeldByBookingId()).isEqualTo(response.bookingId());
        assertThat(seats.get(3L).getStatus()).isEqualTo(SeatStatus.AVAILABLE);
    }

    @Test
    void createBooking_rejectsSeatThatIsAlreadyBooked() {
        seats.get(2L).reserve(99L, clock.instant(), clock.instant().plusSeconds(60));
        seats.get(2L).markBooked();

        assertThatThrownBy(() -> service.createBooking(ALICE, "key-1", new BookingRequest(SHOW_ID, List.of(1L, 2L))))
                .isInstanceOf(SeatNotAvailableException.class);
        assertThat(bookings).isEmpty();
        assertThat(seats.get(1L).getStatus()).isEqualTo(SeatStatus.AVAILABLE);   // nothing half-reserved
    }

    @Test
    void createBooking_rejectsSeatReservedByAnotherActiveBooking() {
        service.createBooking(ALICE, "key-1", new BookingRequest(SHOW_ID, List.of(1L)));

        assertThatThrownBy(() -> service.createBooking(BOB, "key-2", new BookingRequest(SHOW_ID, List.of(1L))))
                .isInstanceOf(SeatNotAvailableException.class);
        assertThat(bookings).hasSize(1);
    }

    @Test
    void createBooking_canTakeSeatWhoseReservationExpired() {
        BookingResponse first = service.createBooking(ALICE, "key-1", new BookingRequest(SHOW_ID, List.of(1L)));
        clock.advance(Duration.ofMinutes(11));

        BookingResponse second = service.createBooking(BOB, "key-2", new BookingRequest(SHOW_ID, List.of(1L)));

        assertThat(second.bookingId()).isNotEqualTo(first.bookingId());
        assertThat(seats.get(1L).getHeldByBookingId()).isEqualTo(second.bookingId());
    }

    @Test
    void createBooking_sameIdempotencyKey_returnsOriginalBooking() {
        BookingResponse first = service.createBooking(ALICE, "key-1", new BookingRequest(SHOW_ID, List.of(1L, 2L)));
        BookingResponse retry = service.createBooking(ALICE, "key-1", new BookingRequest(SHOW_ID, List.of(2L, 1L)));

        assertThat(retry.bookingId()).isEqualTo(first.bookingId());
        assertThat(bookings).hasSize(1);
        verify(bookingRepository, times(1)).saveAndFlush(any(Booking.class));
    }

    @Test
    void createBooking_sameKeyDifferentRequest_isRejected() {
        service.createBooking(ALICE, "key-1", new BookingRequest(SHOW_ID, List.of(1L)));

        assertThatThrownBy(() -> service.createBooking(ALICE, "key-1", new BookingRequest(SHOW_ID, List.of(3L))))
                .isInstanceOf(IdempotencyConflictException.class);
    }

    @Test
    void createBooking_requiresIdempotencyKey() {
        assertThatThrownBy(() -> service.createBooking(ALICE, " ", new BookingRequest(SHOW_ID, List.of(1L))))
                .isInstanceOf(InvalidRequestException.class);
    }

    @Test
    void createBooking_rejectsShowThatIsNotOpen() {
        when(eventClient.getShow(SHOW_ID)).thenReturn(show("SCHEDULED", "PUBLISHED", "250.00"));
        assertThatThrownBy(() -> service.createBooking(ALICE, "k", new BookingRequest(SHOW_ID, List.of(1L))))
                .isInstanceOf(ShowNotBookableException.class);
    }

    @Test
    void createBooking_rejectsUnpublishedEvent() {
        when(eventClient.getShow(SHOW_ID)).thenReturn(show("OPEN", "DRAFT", "250.00"));
        assertThatThrownBy(() -> service.createBooking(ALICE, "k", new BookingRequest(SHOW_ID, List.of(1L))))
                .isInstanceOf(ShowNotBookableException.class);
    }

    @Test
    void createBooking_unknownShow_isNotFound() {
        when(eventClient.getShow(SHOW_ID)).thenReturn(null);
        assertThatThrownBy(() -> service.createBooking(ALICE, "k", new BookingRequest(SHOW_ID, List.of(1L))))
                .isInstanceOf(ShowNotFoundException.class);
    }

    @Test
    void createBooking_rejectsSeatOutsideTheShowLayout() {
        assertThatThrownBy(() -> service.createBooking(ALICE, "k", new BookingRequest(SHOW_ID, List.of(1L, 999L))))
                .isInstanceOf(InvalidSeatSelectionException.class);
    }

    @Test
    void createBooking_rejectsDuplicateSeatsAndTooManySeats() {
        assertThatThrownBy(() -> service.createBooking(ALICE, "k", new BookingRequest(SHOW_ID, List.of(1L, 1L))))
                .isInstanceOf(InvalidSeatSelectionException.class);
        List<Long> eleven = new ArrayList<>();
        for (long i = 1; i <= 11; i++) {
            eleven.add(i);
        }
        assertThatThrownBy(() -> service.createBooking(ALICE, "k", new BookingRequest(SHOW_ID, eleven)))
                .isInstanceOf(InvalidSeatSelectionException.class);
    }

    // ------------------------------------------------------- payment result

    @Test
    void paymentSuccess_confirmsBooking_booksSeats_andPublishesEventOnce() {
        BookingResponse b = service.createBooking(ALICE, "k", new BookingRequest(SHOW_ID, List.of(1L, 2L)));

        PaymentResultResponse result = service.applyPaymentResult(b.bookingId(),
                new PaymentResultRequest("pay-1", PaymentResultStatus.SUCCESS, new BigDecimal("500.00")));

        assertThat(result.outcome()).isEqualTo(PaymentResultOutcome.CONFIRMED);
        assertThat(result.bookingStatus()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(seats.get(1L).getStatus()).isEqualTo(SeatStatus.BOOKED);
        assertThat(seats.get(2L).getStatus()).isEqualTo(SeatStatus.BOOKED);

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher, times(1)).publishEvent(captor.capture());
        BookingConfirmedEvent event = (BookingConfirmedEvent) captor.getValue();
        assertThat(event.bookingId()).isEqualTo(b.bookingId());
        assertThat(event.seatIds()).containsExactly(1L, 2L);
        assertThat(event.amount()).isEqualByComparingTo("500.00");
        assertThat(event.customerEmail()).isEqualTo("alice@example.com");
        assertThat(event.toJson()).contains("\"eventType\":\"BookingConfirmed\"").contains("\"seatIds\":[1,2]");
    }

    @Test
    void paymentSuccess_repeated_isIdempotent() {
        BookingResponse b = service.createBooking(ALICE, "k", new BookingRequest(SHOW_ID, List.of(1L)));
        PaymentResultRequest request = new PaymentResultRequest("pay-1", PaymentResultStatus.SUCCESS, null);

        service.applyPaymentResult(b.bookingId(), request);
        PaymentResultResponse again = service.applyPaymentResult(b.bookingId(), request);

        assertThat(again.outcome()).isEqualTo(PaymentResultOutcome.ALREADY_PROCESSED);
        assertThat(again.bookingStatus()).isEqualTo(BookingStatus.CONFIRMED);
        verify(eventPublisher, times(1)).publishEvent(any(Object.class));
    }

    @Test
    void paymentSuccess_fromDifferentPayment_isFlaggedAsDuplicate() {
        BookingResponse b = service.createBooking(ALICE, "k", new BookingRequest(SHOW_ID, List.of(1L)));
        service.applyPaymentResult(b.bookingId(), new PaymentResultRequest("pay-1", PaymentResultStatus.SUCCESS, null));

        PaymentResultResponse other = service.applyPaymentResult(b.bookingId(),
                new PaymentResultRequest("pay-2", PaymentResultStatus.SUCCESS, null));

        assertThat(other.outcome()).isEqualTo(PaymentResultOutcome.DUPLICATE_PAYMENT);
    }

    @Test
    void paymentSuccess_afterExpiry_doesNotConfirm_andReleasesSeats() {
        BookingResponse b = service.createBooking(ALICE, "k", new BookingRequest(SHOW_ID, List.of(1L)));
        clock.advance(Duration.ofMinutes(11));

        PaymentResultResponse result = service.applyPaymentResult(b.bookingId(),
                new PaymentResultRequest("pay-1", PaymentResultStatus.SUCCESS, null));

        assertThat(result.outcome()).isEqualTo(PaymentResultOutcome.REJECTED_EXPIRED);
        assertThat(result.bookingStatus()).isEqualTo(BookingStatus.EXPIRED);
        assertThat(seats.get(1L).getStatus()).isEqualTo(SeatStatus.AVAILABLE);
        verify(eventPublisher, never()).publishEvent(any(Object.class));
    }

    @Test
    void paymentFailure_marksPaymentFailed_andReleasesSeats() {
        BookingResponse b = service.createBooking(ALICE, "k", new BookingRequest(SHOW_ID, List.of(1L, 2L)));

        PaymentResultResponse result = service.applyPaymentResult(b.bookingId(),
                new PaymentResultRequest("pay-1", PaymentResultStatus.FAILED, null));

        assertThat(result.outcome()).isEqualTo(PaymentResultOutcome.PAYMENT_FAILED_RECORDED);
        assertThat(result.bookingStatus()).isEqualTo(BookingStatus.PAYMENT_FAILED);
        assertThat(seats.get(1L).getStatus()).isEqualTo(SeatStatus.AVAILABLE);
        assertThat(seats.get(2L).getStatus()).isEqualTo(SeatStatus.AVAILABLE);
        verify(eventPublisher, never()).publishEvent(any(Object.class));
    }

    @Test
    void lateFailureCallback_neverUndoesConfirmedBooking() {
        BookingResponse b = service.createBooking(ALICE, "k", new BookingRequest(SHOW_ID, List.of(1L)));
        service.applyPaymentResult(b.bookingId(), new PaymentResultRequest("pay-1", PaymentResultStatus.SUCCESS, null));

        PaymentResultResponse late = service.applyPaymentResult(b.bookingId(),
                new PaymentResultRequest("pay-1", PaymentResultStatus.FAILED, null));

        assertThat(late.bookingStatus()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(seats.get(1L).getStatus()).isEqualTo(SeatStatus.BOOKED);
    }

    @Test
    void paymentResult_withWrongAmount_isRejected_andChangesNothing() {
        BookingResponse b = service.createBooking(ALICE, "k", new BookingRequest(SHOW_ID, List.of(1L)));

        assertThatThrownBy(() -> service.applyPaymentResult(b.bookingId(),
                new PaymentResultRequest("pay-1", PaymentResultStatus.SUCCESS, new BigDecimal("1.00"))))
                .isInstanceOf(AmountMismatchException.class);
        assertThat(bookings.get(b.bookingId()).getStatus()).isEqualTo(BookingStatus.PENDING_PAYMENT);
    }

    @Test
    void paymentResult_forUnknownBooking_isNotFound() {
        assertThatThrownBy(() -> service.applyPaymentResult(404L,
                new PaymentResultRequest("pay-1", PaymentResultStatus.SUCCESS, null)))
                .isInstanceOf(BookingNotFoundException.class);
    }

    // ---------------------------------------------------------------- cancel

    @Test
    void cancel_pendingBooking_releasesSeats() {
        BookingResponse b = service.createBooking(ALICE, "k", new BookingRequest(SHOW_ID, List.of(1L, 2L)));

        BookingResponse cancelled = service.cancelBooking(ALICE, b.bookingId());

        assertThat(cancelled.status()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(seats.get(1L).getStatus()).isEqualTo(SeatStatus.AVAILABLE);
        assertThat(seats.get(2L).getStatus()).isEqualTo(SeatStatus.AVAILABLE);
    }

    @Test
    void cancel_confirmedBooking_isRejected() {
        BookingResponse b = service.createBooking(ALICE, "k", new BookingRequest(SHOW_ID, List.of(1L)));
        service.applyPaymentResult(b.bookingId(), new PaymentResultRequest("pay-1", PaymentResultStatus.SUCCESS, null));

        assertThatThrownBy(() -> service.cancelBooking(ALICE, b.bookingId()))
                .isInstanceOf(InvalidBookingStateException.class);
        assertThat(seats.get(1L).getStatus()).isEqualTo(SeatStatus.BOOKED);
    }

    @Test
    void cancel_someoneElsesBooking_looksLikeNotFound() {
        BookingResponse b = service.createBooking(ALICE, "k", new BookingRequest(SHOW_ID, List.of(1L)));

        assertThatThrownBy(() -> service.cancelBooking(BOB, b.bookingId()))
                .isInstanceOf(BookingNotFoundException.class);
    }

    @Test
    void getBooking_enforcesOwnership_butAdminCanView() {
        BookingResponse b = service.createBooking(ALICE, "k", new BookingRequest(SHOW_ID, List.of(1L)));
        when(bookingRepository.findById(b.bookingId())).thenReturn(Optional.of(bookings.get(b.bookingId())));

        assertThat(service.getBooking(ALICE, b.bookingId()).bookingId()).isEqualTo(b.bookingId());
        assertThatThrownBy(() -> service.getBooking(BOB, b.bookingId())).isInstanceOf(BookingNotFoundException.class);
        assertThat(service.getBooking(new CurrentUser("ops", null, true), b.bookingId()).bookingId()).isEqualTo(b.bookingId());
    }

    // ---------------------------------------------------------------- expiry

    @Test
    void expiry_marksBookingExpired_andReleasesSeats() {
        BookingResponse b = service.createBooking(ALICE, "k", new BookingRequest(SHOW_ID, List.of(1L, 2L)));

        assertThat(service.expireDueBookings()).isZero();          // not due yet
        clock.advance(Duration.ofMinutes(10).plusSeconds(1));
        assertThat(service.expireDueBookings()).isEqualTo(1);

        assertThat(bookings.get(b.bookingId()).getStatus()).isEqualTo(BookingStatus.EXPIRED);
        assertThat(seats.get(1L).getStatus()).isEqualTo(SeatStatus.AVAILABLE);
        assertThat(seats.get(2L).getStatus()).isEqualTo(SeatStatus.AVAILABLE);
    }

    @Test
    void expiry_doesNotTouchConfirmedBookings() {
        BookingResponse b = service.createBooking(ALICE, "k", new BookingRequest(SHOW_ID, List.of(1L)));
        service.applyPaymentResult(b.bookingId(), new PaymentResultRequest("pay-1", PaymentResultStatus.SUCCESS, null));
        clock.advance(Duration.ofHours(1));

        assertThat(service.expireDueBookings()).isZero();
        assertThat(seats.get(1L).getStatus()).isEqualTo(SeatStatus.BOOKED);
    }

    @Test
    void expiry_doesNotReleaseSeatsAlreadyTakenOverByAnotherBooking() {
        BookingResponse first = service.createBooking(ALICE, "k1", new BookingRequest(SHOW_ID, List.of(1L)));
        clock.advance(Duration.ofMinutes(11));
        BookingResponse second = service.createBooking(BOB, "k2", new BookingRequest(SHOW_ID, List.of(1L)));

        service.expireDueBookings();

        assertThat(bookings.get(first.bookingId()).getStatus()).isEqualTo(BookingStatus.EXPIRED);
        assertThat(seats.get(1L).getStatus()).isEqualTo(SeatStatus.RESERVED);
        assertThat(seats.get(1L).getHeldByBookingId()).isEqualTo(second.bookingId());
    }

    // ------------------------------------------------------------ seat state

    @Test
    void seatStates_listReservedAndBooked_ignoringExpiredReservations() {
        service.createBooking(ALICE, "k1", new BookingRequest(SHOW_ID, List.of(1L)));
        BookingResponse paid = service.createBooking(BOB, "k2", new BookingRequest(SHOW_ID, List.of(2L)));
        service.applyPaymentResult(paid.bookingId(), new PaymentResultRequest("pay-1", PaymentResultStatus.SUCCESS, null));

        assertThat(service.getSeatStates(SHOW_ID)).containsExactly(
                new SeatStateResponse(1L, SeatStatus.RESERVED), new SeatStateResponse(2L, SeatStatus.BOOKED));

        clock.advance(Duration.ofMinutes(11));
        assertThat(service.getSeatStates(SHOW_ID)).containsExactly(new SeatStateResponse(2L, SeatStatus.BOOKED));
    }

    // --------------------------------------------------------------- helpers

    private static ShowResponse show(String status, String eventStatus, String price) {
        return new ShowResponse(SHOW_ID, 5L, eventStatus, 9L, java.time.LocalDateTime.now().plusHours(1),
                java.time.LocalDateTime.now().plusHours(3), status, 1, 10, 1L, java.time.LocalDateTime.now());
    }

    private static class MutableClock extends Clock {
        private Instant now;

        MutableClock(Instant start) {
            this.now = start;
        }

        void advance(Duration d) {
            now = now.plus(d);
        }

        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return now; }
    }

    private static class NoOpTransactionManager implements PlatformTransactionManager {
        @Override public TransactionStatus getTransaction(TransactionDefinition definition) { return new SimpleTransactionStatus(); }
        @Override public void commit(TransactionStatus status) { }
        @Override public void rollback(TransactionStatus status) { }
    }
}
