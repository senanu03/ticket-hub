package com.tp.snapseat.booking_service.controller;

import com.tp.snapseat.booking_service.dto.BookingPaymentDetails;
import com.tp.snapseat.booking_service.dto.PaymentResultRequest;
import com.tp.snapseat.booking_service.dto.PaymentResultResponse;
import com.tp.snapseat.booking_service.dto.SeatStateResponse;
import com.tp.snapseat.booking_service.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Service-to-service API. Not routed through the public gateway. */
@RestController
@RequestMapping("/internal")
@Tag(name = "Internal (service-to-service)")
public class InternalBookingController {

    private final BookingService bookingService;

    public InternalBookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @Operation(summary = "Payment Service reports a verified payment result",
            description = "SUCCESS confirms the booking and books its seats (unless it expired); FAILED releases the seats. "
                    + "Idempotent: repeating the same result changes nothing. Requires the SERVICE role.")
    @PostMapping("/bookings/{bookingId}/payment-result")
    public PaymentResultResponse paymentResult(@PathVariable Long bookingId,
                                               @Valid @RequestBody PaymentResultRequest request) {
        return bookingService.applyPaymentResult(bookingId, request);
    }

    @Operation(summary = "Reservation state of a show's seats",
            description = "Lists seats that are RESERVED or BOOKED; all other seats of the show are AVAILABLE. "
                    + "Event/Show combines this with its seat layout.")
    @GetMapping("/shows/{showId}/seat-states")
    public List<SeatStateResponse> seatStates(@PathVariable Long showId) {
        return bookingService.getSeatStates(showId);
    }

    @GetMapping("/bookings/{bookingId}/payment-details")
    public BookingPaymentDetails paymentDetails(@PathVariable Long bookingId) {
        return bookingService.getPaymentDetails(bookingId);
    }
}
