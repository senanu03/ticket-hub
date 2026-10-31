package com.tickethub.payment.exception;

import com.tickethub.payment.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ErrorResponse.FieldErrorItem> errors = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(this::field)
                .toList();

        return response(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                "Request validation failed", request, errors);
    }

    @ExceptionHandler(PaymentNotFoundException.class)
    ResponseEntity<ErrorResponse> notFound(PaymentNotFoundException ex, HttpServletRequest request) {
        return response(HttpStatus.NOT_FOUND, "PAYMENT_NOT_FOUND", ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(PaymentConflictException.class)
    ResponseEntity<ErrorResponse> conflict(PaymentConflictException ex, HttpServletRequest request) {
        return response(HttpStatus.CONFLICT, "PAYMENT_CONFLICT", ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(BookingServiceException.class)
    ResponseEntity<ErrorResponse> booking(BookingServiceException ex, HttpServletRequest request) {
        return response(HttpStatus.SERVICE_UNAVAILABLE, "BOOKING_SERVICE_UNAVAILABLE",
                "Booking Service is temporarily unavailable", request, List.of());
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> generic(Exception ex, HttpServletRequest request) {
        ex.printStackTrace();
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "PAYMENT_SERVICE_ERROR",
                "Unexpected payment service error", request, List.of());
    }

    private ErrorResponse.FieldErrorItem field(FieldError e) {
        return new ErrorResponse.FieldErrorItem(e.getField(), e.getDefaultMessage());
    }

    private ResponseEntity<ErrorResponse> response(
            HttpStatus status,
            String code,
            String message,
            HttpServletRequest request,
            List<ErrorResponse.FieldErrorItem> errors) {

        String correlationId = request.getHeader("X-Correlation-Id");
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = "n/a";
        }

        return ResponseEntity.status(status).body(new ErrorResponse(
                Instant.now(), status.value(), code, message,
                request.getRequestURI(), correlationId, errors
        ));
    }
}
