package com.tp.snapseat.booking_service.service;

import com.tp.snapseat.booking_service.client.EventServiceClient;
import com.tp.snapseat.booking_service.client.SeatLayoutResponse;
import com.tp.snapseat.booking_service.client.ShowResponse;
import com.tp.snapseat.booking_service.config.BookingProperties;
import com.tp.snapseat.booking_service.dto.*;
import com.tp.snapseat.booking_service.entity.Booking;
import com.tp.snapseat.booking_service.entity.BookingSeat;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Service
public class BookingServiceImpl implements BookingService {

    private static final Logger log =
            LoggerFactory.getLogger(BookingServiceImpl.class);

    private static final int MAX_IDEMPOTENCY_KEY_LENGTH = 100;

    private final BookingRepository bookingRepository;
    private final ShowSeatRepository showSeatRepository;
    private final EventServiceClient eventServiceClient;
    private final SeatInitializer seatInitializer;
    private final ApplicationEventPublisher eventPublisher;
    private final BookingProperties properties;
    private final Clock clock;
    private final TransactionTemplate tx;

    public BookingServiceImpl(
            BookingRepository bookingRepository,
            ShowSeatRepository showSeatRepository,
            EventServiceClient eventServiceClient,
            SeatInitializer seatInitializer,
            ApplicationEventPublisher eventPublisher,
            BookingProperties properties,
            Clock clock,
            PlatformTransactionManager transactionManager) {

        this.bookingRepository = bookingRepository;
        this.showSeatRepository = showSeatRepository;
        this.eventServiceClient = eventServiceClient;
        this.seatInitializer = seatInitializer;
        this.eventPublisher = eventPublisher;
        this.properties = properties;
        this.clock = clock;
        this.tx = new TransactionTemplate(transactionManager);
    }

    // ------------------------------------------------------------------ create

    /**
     * Deliberately NOT one big transaction:
     * remote Event calls and seat-row initialisation happen before
     * the short local transaction that locks seats and writes the booking.
     */
    @Override
    public BookingResponse createBooking(
            CurrentUser customer,
            String idempotencyKey,
            BookingRequest request) {

        String key = normalizeKey(idempotencyKey);

        Long showId = request.showId();

        List<Long> seatIds =
                normalizeSeats(request.seatIds());

        // SC-13:
        // A retry with the same key returns the original booking.
        Optional<BookingResponse> replay =
                findReplay(
                        customer.id(),
                        key,
                        showId,
                        seatIds);

        if (replay.isPresent()) {

            log.info(
                    "Idempotent replay of booking request: bookingId={}",
                    replay.get().bookingId());

            return replay.get();
        }

        ShowResponse show =
                validateShow(showId);

        validateSeatsBelongToShow(
                showId,
                seatIds);

        seatInitializer.ensureRows(
                showId,
                seatIds);

        try {

            BookingResponse created =
                    tx.execute(status ->
                            reserve(
                                    customer,
                                    key,
                                    show,
                                    seatIds));

            log.info(
                    "Booking created: bookingId={} showId={} seats={} amount={} {}",
                    created.bookingId(),
                    showId,
                    seatIds.size(),
                    created.amount(),
                    created.currency());

            return created;

        } catch (DataIntegrityViolationException ex) {

            Optional<BookingResponse> winner =
                    findReplay(
                            customer.id(),
                            key,
                            showId,
                            seatIds);

            if (winner.isPresent()) {
                return winner.get();
            }

            throw new SeatNotAvailableException(
                    "The selected seats could not be reserved, please retry",
                    ex);

        } catch (SeatNotAvailableException ex) {

            Optional<BookingResponse> winner =
                    findReplay(
                            customer.id(),
                            key,
                            showId,
                            seatIds);

            if (winner.isPresent()) {
                return winner.get();
            }

            throw ex;

        } catch (PessimisticLockingFailureException ex) {

            throw new SeatNotAvailableException(
                    "The selected seats are being reserved by another request, please retry",
                    ex);
        }
    }



    private BookingResponse reserve(
            CurrentUser customer,
            String key,
            ShowResponse show,
            List<Long> seatIds) {

        Instant now = clock.instant();

        List<ShowSeat> seats =
                showSeatRepository.lockSeats(
                        show.id(),
                        seatIds);

        if (seats.size() != seatIds.size()) {
            throw new SeatNotAvailableException(
                    "The selected seats could not be reserved, please retry");
        }

        for (ShowSeat seat : seats) {
            if (!seat.isAvailableAt(now)) {
                throw new SeatNotAvailableException(
                        "Seat " + seat.getSeatId() + " is not available");
            }
        }

        List<SeatLayoutResponse> layout =
                eventServiceClient.getSeatLayout(show.id());

        if (layout == null || layout.isEmpty()) {
            throw new ShowNotFoundException(
                    "Seat layout for show " + show.id() + " not found");
        }

        var priceBySeatId =
                layout.stream()
                        .collect(Collectors.toMap(
                                SeatLayoutResponse::seatId,
                                SeatLayoutResponse::price));

        for (Long seatId : seatIds) {
            if (!priceBySeatId.containsKey(seatId)
                    || priceBySeatId.get(seatId) == null) {

                throw new InvalidSeatSelectionException(
                        "Price is not configured for seat "
                                + seatId
                                + " of show "
                                + show.id());
            }
        }

        java.math.BigDecimal totalAmount =
                seatIds.stream()
                        .map(priceBySeatId::get)
                        .reduce(
                                java.math.BigDecimal.ZERO,
                                java.math.BigDecimal::add);

        Instant expiresAt =
                now.plus(properties.getReservation().getExpiry());

        Booking booking =
                Booking.create(
                        customer.id(),
                        customer.email(),
                        key,
                        show.id(),
                        show.eventId(),
                        totalAmount,
                        "INR",
                        now,
                        expiresAt);

        for (ShowSeat seat : seats) {
            booking.addSeat(
                    seat.getSeatId(),
                    seat.getShowSeatId(),
                    priceBySeatId.get(seat.getSeatId()));
        }

        // Save first so the database generates bookingId.
        Booking saved =
                bookingRepository.saveAndFlush(booking);

        // Now the generated bookingId is available.
        for (ShowSeat seat : seats) {
            seat.reserve(
                    saved.getBookingId(),
                    now,
                    expiresAt);
        }

        showSeatRepository.saveAll(seats);

        return toResponse(saved);
    }




    // ---------------------------------------------------------------- replay

    private Optional<BookingResponse> findReplay(
            String customerId,
            String key,
            Long showId,
            List<Long> seatIds) {

        Optional<BookingResponse> result =
                tx.execute(status ->
                        bookingRepository
                                .findByCustomerIdAndIdempotencyKey(
                                        customerId,
                                        key)
                                .map(existing -> {

                                    boolean sameRequest =
                                            existing.getShowId()
                                                    .equals(showId)
                                                    && new HashSet<>(
                                                    existing.seatIds())
                                                    .equals(
                                                            new HashSet<>(
                                                                    seatIds));

                                    if (!sameRequest) {

                                        throw new IdempotencyConflictException(
                                                "Idempotency-Key was already used "
                                                        + "for a different booking request");
                                    }

                                    return toResponse(existing);
                                }));

        return result == null
                ? Optional.empty()
                : result;
    }

    // ------------------------------------------------------------------ show

    private ShowResponse validateShow(Long showId) {

        ShowResponse show =
                eventServiceClient.getShow(showId);

        if (show == null) {

            throw new ShowNotFoundException(
                    "Show " + showId + " not found");
        }

        // Only OPEN shows can be booked.
        if (!"OPEN".equals(show.status())) {

            throw new ShowNotBookableException(
                    "Show " + showId
                            + " is not open for booking (status "
                            + show.status() + ")");
        }

        if (show.eventStatus() != null && !"PUBLISHED".equalsIgnoreCase(show.eventStatus())) {
            throw new ShowNotBookableException(
                    "Show " + showId
                            + " belongs to an event that is not published (event status "
                            + show.eventStatus() + ")");
        }

        return show;
    }

    // -------------------------------------------------------------- seat check

    private void validateSeatsBelongToShow(
            Long showId,
            List<Long> seatIds) {

        List<SeatLayoutResponse> layout =
                eventServiceClient.getSeatLayout(showId);

        if (layout == null || layout.isEmpty()) {

            throw new ShowNotFoundException(
                    "Seat layout for show "
                            + showId
                            + " not found");
        }

        Set<Long> valid =
                layout.stream()
                        .map(SeatLayoutResponse::seatId)
                        .collect(Collectors.toSet());

        List<Long> unknown =
                seatIds.stream()
                        .filter(id -> !valid.contains(id))
                        .toList();

        if (!unknown.isEmpty()) {

            throw new InvalidSeatSelectionException(
                    "Seats " + unknown
                            + " do not belong to show "
                            + showId);
        }
    }

    // --------------------------------------------------------------- idempotency

    private String normalizeKey(String key) {

        if (key == null || key.isBlank()) {

            throw new InvalidRequestException(
                    "Idempotency-Key header is required");
        }

        String trimmed =
                key.trim();

        if (trimmed.length()
                > MAX_IDEMPOTENCY_KEY_LENGTH) {

            throw new InvalidRequestException(
                    "Idempotency-Key must be at most "
                            + MAX_IDEMPOTENCY_KEY_LENGTH
                            + " characters");
        }

        return trimmed;
    }

    // ------------------------------------------------------------------- seats

    private List<Long> normalizeSeats(
            List<Long> seatIds) {

        if (seatIds == null || seatIds.isEmpty()) {

            throw new InvalidRequestException(
                    "At least one seat is required");
        }

        Set<Long> unique =
                new TreeSet<>(seatIds);

        if (unique.size()
                != seatIds.size()) {

            throw new InvalidSeatSelectionException(
                    "Duplicate seats in the request");
        }

        int max =
                properties.getReservation()
                        .getMaxSeatsPerBooking();

        if (unique.size() > max) {

            throw new InvalidSeatSelectionException(
                    "A booking can contain at most "
                            + max
                            + " seats");
        }

        // Sorted ascending:
        // consistent database lock order.
        return new ArrayList<>(unique);
    }

    // -------------------------------------------------------------------- read

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getBooking(
            CurrentUser caller,
            Long bookingId) {

        Optional<Booking> booking =
                caller.admin()
                        ? bookingRepository.findById(bookingId)
                        : bookingRepository
                        .findByBookingIdAndCustomerId(
                                bookingId,
                                caller.id());

        return toResponse(
                booking.orElseThrow(() ->
                        new BookingNotFoundException(
                                "Booking "
                                        + bookingId
                                        + " not found")));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<BookingResponse> listMyBookings(
            CurrentUser customer,
            int page,
            int size) {

        int safePage =
                Math.max(page, 0);

        int safeSize =
                Math.min(
                        Math.max(size, 1),
                        100);

        Page<Booking> result =
                bookingRepository.findByCustomerId(
                        customer.id(),
                        PageRequest.of(
                                safePage,
                                safeSize,
                                Sort.by(
                                        Sort.Direction.DESC,
                                        "createdAt")));

        return new PagedResponse<>(
                result.getContent()
                        .stream()
                        .map(BookingServiceImpl::toResponse)
                        .toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeatStateResponse> getSeatStates(
            Long showId) {

        Instant now =
                clock.instant();

        return showSeatRepository
                .findByShowIdAndStatusIn(
                        showId,
                        List.of(
                                SeatStatus.RESERVED,
                                SeatStatus.BOOKED))
                .stream()
                .filter(s ->
                        s.getStatus()
                                == SeatStatus.BOOKED
                                || s.isActivelyReservedAt(now))
                .map(s ->
                        new SeatStateResponse(
                                s.getSeatId(),
                                s.getStatus()))
                .sorted(
                        Comparator.comparing(
                                SeatStateResponse::seatId))
                .toList();
    }

    // ------------------------------------------------------------------ cancel

    @Override
    @Transactional
    public BookingResponse cancelBooking(
            CurrentUser customer,
            Long bookingId) {

        Booking booking =
                bookingRepository
                        .findByIdForUpdate(bookingId)
                        .filter(b ->
                                b.getCustomerId()
                                        .equals(customer.id()))
                        .orElseThrow(() ->
                                new BookingNotFoundException(
                                        "Booking "
                                                + bookingId
                                                + " not found"));

        try (MDC.MDCCloseable ignored =
                     MDC.putCloseable(
                             "bookingId",
                             String.valueOf(bookingId))) {

            switch (booking.getStatus()) {

                case PENDING_PAYMENT -> {

                    booking.changeStatus(
                            BookingStatus.CANCELLED,
                            "Cancelled by customer",
                            clock.instant());

                    releaseSeats(booking);

                    log.info(
                            "Booking cancelled by customer: bookingId={}",
                            bookingId);
                }

                case CANCELLED ->

                        log.info(
                                "Cancel requested for already cancelled booking: bookingId={}",
                                bookingId);

                case CONFIRMED ->

                        throw new InvalidBookingStateException(
                                "A confirmed booking cannot be cancelled "
                                        + "(refunds are not supported yet)");

                default ->

                        throw new InvalidBookingStateException(
                                "Booking "
                                        + bookingId
                                        + " is "
                                        + booking.getStatus()
                                        + " and cannot be cancelled");
            }

            return toResponse(booking);
        }
    }

    // ---------------------------------------------------------- payment result

    @Override
    @Transactional
    public PaymentResultResponse applyPaymentResult(
            Long bookingId,
            PaymentResultRequest request) {

        Booking booking =
                bookingRepository
                        .findByIdForUpdate(bookingId)
                        .orElseThrow(() ->
                                new BookingNotFoundException(
                                        "Booking "
                                                + bookingId
                                                + " not found"));

        Instant now =
                clock.instant();

        try (MDC.MDCCloseable b =
                     MDC.putCloseable(
                             "bookingId",
                             String.valueOf(bookingId));
             MDC.MDCCloseable p =
                     MDC.putCloseable(
                             "paymentId",
                             request.paymentId())) {

            if (request.amount() != null
                    && request.amount()
                    .compareTo(
                            booking.getAmount()) != 0) {

                throw new AmountMismatchException(
                        "Payment amount does not match the booking amount");
            }

            return request.status()
                    == PaymentResultStatus.SUCCESS
                    ? applySuccess(
                    booking,
                    request,
                    now)
                    : applyFailure(
                    booking,
                    request,
                    now);
        }
    }

    private PaymentResultResponse applySuccess(
            Booking booking,
            PaymentResultRequest request,
            Instant now) {

        switch (booking.getStatus()) {

            case CONFIRMED -> {

                if (request.paymentId()
                        .equals(
                                booking.getPaymentId())) {

                    return result(
                            booking,
                            PaymentResultOutcome.ALREADY_PROCESSED);
                }

                log.warn(
                        "Booking already confirmed by another payment: "
                                + "bookingId={} existing={} new={}",
                        booking.getBookingId(),
                        booking.getPaymentId(),
                        request.paymentId());

                return result(
                        booking,
                        PaymentResultOutcome.DUPLICATE_PAYMENT);
            }

            case PENDING_PAYMENT -> {

                if (booking.isExpiredAt(now)) {

                    booking.setPaymentId(
                            request.paymentId());

                    booking.changeStatus(
                            BookingStatus.EXPIRED,
                            "Payment "
                                    + request.paymentId()
                                    + " succeeded after the reservation expired",
                            now);

                    releaseSeats(booking);

                    log.warn(
                            "Payment succeeded after expiry, booking NOT confirmed: "
                                    + "bookingId={} paymentId={}",
                            booking.getBookingId(),
                            request.paymentId());

                    return result(
                            booking,
                            PaymentResultOutcome.REJECTED_EXPIRED);
                }

                confirm(
                        booking,
                        request.paymentId(),
                        now);

                return result(
                        booking,
                        PaymentResultOutcome.CONFIRMED);
            }

            case EXPIRED -> {

                log.warn(
                        "Payment succeeded for an expired booking: "
                                + "bookingId={} paymentId={}",
                        booking.getBookingId(),
                        request.paymentId());

                return result(
                        booking,
                        PaymentResultOutcome.REJECTED_EXPIRED);
            }

            default -> {

                log.warn(
                        "Payment succeeded for a {} booking: "
                                + "bookingId={} paymentId={}",
                        booking.getStatus(),
                        booking.getBookingId(),
                        request.paymentId());

                return result(
                        booking,
                        PaymentResultOutcome.REJECTED_NOT_PENDING);
            }
        }
    }

    private PaymentResultResponse applyFailure(
            Booking booking,
            PaymentResultRequest request,
            Instant now) {

        if (booking.getStatus()
                == BookingStatus.PENDING_PAYMENT) {

            booking.setPaymentId(
                    request.paymentId());

            booking.changeStatus(
                    BookingStatus.PAYMENT_FAILED,
                    "Payment "
                            + request.paymentId()
                            + " failed",
                    now);

            releaseSeats(booking);

            log.info(
                    "Payment failed, seats released: "
                            + "bookingId={} paymentId={}",
                    booking.getBookingId(),
                    request.paymentId());

            return result(
                    booking,
                    PaymentResultOutcome.PAYMENT_FAILED_RECORDED);
        }

        return result(
                booking,
                PaymentResultOutcome.ALREADY_PROCESSED);
    }

    private void confirm(
            Booking booking,
            String paymentId,
            Instant now) {

        List<ShowSeat> held =
                showSeatRepository.lockHeldBy(
                        booking.getBookingId(),
                        SeatStatus.RESERVED);

        if (held.size()
                != booking.getSeats().size()) {

            throw new InvalidBookingStateException(
                    "Seats of booking "
                            + booking.getBookingId()
                            + " are no longer held by it; cannot confirm");
        }

        held.forEach(ShowSeat::markBooked);

        showSeatRepository.saveAll(held);

        booking.setPaymentId(paymentId);

        booking.changeStatus(
                BookingStatus.CONFIRMED,
                "Payment " + paymentId + " succeeded",
                now);

        // Application event is published inside the transaction.
        // The listener should handle it after commit if configured with
        // @TransactionalEventListener(phase = AFTER_COMMIT).
        eventPublisher.publishEvent(
                BookingConfirmedEvent.from(
                        booking,
                        now));

        log.info(
                "Booking confirmed: bookingId={} paymentId={}",
                paymentId,
                paymentId);
    }

    private PaymentResultResponse result(
            Booking booking,
            PaymentResultOutcome outcome) {

        return new PaymentResultResponse(
                booking.getBookingId(),
                booking.getStatus(),
                outcome);
    }

    // ------------------------------------------------------------------ expiry

    @Override
    public int expireDueBookings() {

        Instant now =
                clock.instant();

        List<Long> dueIds =
                tx.execute(status ->
                        bookingRepository.findExpiredIds(
                                BookingStatus.PENDING_PAYMENT,
                                now,
                                PageRequest.of(
                                        0,
                                        properties
                                                .getReservation()
                                                .getExpiryBatchSize())));

        if (dueIds == null || dueIds.isEmpty()) {
            return 0;
        }

        int expired = 0;

        for (Long id : dueIds) {

            try {

                Boolean done =
                        tx.execute(status ->
                                expireOne(id));

                if (Boolean.TRUE.equals(done)) {
                    expired++;
                }

            } catch (RuntimeException ex) {

                log.error(
                        "Failed to expire booking: bookingId={}",
                        id,
                        ex);
            }
        }

        return expired;
    }

    private boolean expireOne(Long bookingId) {

        Booking booking =
                bookingRepository
                        .findByIdForUpdate(bookingId)
                        .orElse(null);

        Instant now =
                clock.instant();

        // Re-check under the lock:
        // a payment result may have just confirmed / failed it.
        if (booking == null
                || booking.getStatus()
                != BookingStatus.PENDING_PAYMENT
                || !booking.isExpiredAt(now)) {

            return false;
        }

        try (MDC.MDCCloseable ignored =
                     MDC.putCloseable(
                             "bookingId",
                             String.valueOf(bookingId))) {

            booking.changeStatus(
                    BookingStatus.EXPIRED,
                    "Reservation expired before payment completed",
                    now);

            releaseSeats(booking);

            log.info(
                    "Booking expired, seats released: bookingId={}",
                    bookingId);
        }

        return true;
    }

    // ----------------------------------------------------------------- helpers

    /**
     * Releases only seats still held by this booking.
     * An expired hold may have been taken over by another booking.
     */
    private void releaseSeats(Booking booking) {

        List<ShowSeat> held =
                showSeatRepository.lockHeldBy(
                        booking.getBookingId(),
                        SeatStatus.RESERVED);

        held.forEach(ShowSeat::release);

        showSeatRepository.saveAll(held);
    }

    static BookingResponse toResponse(Booking b) {

        List<BookingSeatResponse> seats =
                b.getSeats()
                        .stream()
                        .sorted(
                                Comparator.comparing(
                                        BookingSeat::getSeatId))
                        .map(s ->
                                new BookingSeatResponse(
                                        s.getSeatId(),
                                        s.getPrice()))
                        .toList();

        return new BookingResponse(
                b.getBookingId(),
                b.getCustomerId(),
                b.getShowId(),
                seats,
                b.getAmount(),
                b.getCurrency(),
                b.getStatus(),
                b.getPaymentId(),
                b.getCreatedAt(),
                b.getExpiresAt());
    }


    @Override
    @Transactional(readOnly = true)
    public BookingPaymentDetails getPaymentDetails(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(String.valueOf(bookingId)));

        return new BookingPaymentDetails(
                booking.getBookingId(),
                booking.getCustomerId(),
                booking.getAmount(),
                booking.getCurrency(),
                booking.getStatus().name(),
                booking.getExpiresAt()
        );
    }
}