package com.tp.snapseat.booking_service.service;

import com.tp.snapseat.booking_service.dto.*;
import com.tp.snapseat.booking_service.security.CurrentUser;
import com.tp.snapseat.booking_service.dto.BookingPaymentDetails;
import java.util.List;

public interface BookingService {

    /** Creates a PENDING_PAYMENT booking and reserves the seats. Idempotent per (customer, idempotencyKey). */
    BookingResponse createBooking(CurrentUser customer, String idempotencyKey, BookingRequest request);

    BookingResponse getBooking(CurrentUser caller, Long bookingId);

    PagedResponse<BookingResponse> listMyBookings(CurrentUser customer, int page, int size);

    /** Cancels an unpaid booking owned by the caller and releases its seats. */
    BookingResponse cancelBooking(CurrentUser customer, Long bookingId);

    /** Applies a verified payment result from the Payment Service (confirm or fail). Idempotent. */
    PaymentResultResponse applyPaymentResult(Long bookingId, PaymentResultRequest request);

    /** Seats of a show that are RESERVED or BOOKED (all others are AVAILABLE). Used by Event/Show for availability. */
    List<SeatStateResponse> getSeatStates(Long showId);

    /** Expires PENDING_PAYMENT bookings past their expiry and releases their seats. Returns how many were expired. */
    int expireDueBookings();

    BookingPaymentDetails getPaymentDetails(Long bookingId);
}
