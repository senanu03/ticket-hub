package com.tp.snapseat.booking_service.exception;

import com.tp.snapseat.booking_service.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;
import java.util.List;

/**
 * Central error mapping to the single standard error format (ST-02 / ST-50).
 * Framework exceptions (400 / 404 / 405 / 415 ...) are handled by the base class and rendered in the same format.
 * Unexpected failures return a generic message - never stack traces or internals.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BookingException.class)
    public ResponseEntity<Object> handleBooking(BookingException ex, WebRequest request) {
        if (ex.getStatus().is5xxServerError()) {
            log.warn("Booking error {}: {}", ex.getCode(), ex.getMessage());
        } else {
            log.info("Booking error {}: {}", ex.getCode(), ex.getMessage());
        }
        return build(ex.getStatus(), ex.getCode(), ex.getMessage(), request, null);
    }

    @ExceptionHandler({ObjectOptimisticLockingFailureException.class, PessimisticLockingFailureException.class})
    public ResponseEntity<Object> handleConcurrency(Exception ex, WebRequest request) {
        log.info("Concurrent modification: {}", ex.getClass().getSimpleName());
        return build(HttpStatus.CONFLICT, "CONCURRENT_MODIFICATION",
                "The resource was modified by another request, please retry", request, null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleIntegrity(DataIntegrityViolationException ex, WebRequest request) {
        log.warn("Data integrity violation", ex);
        return build(HttpStatus.CONFLICT, "CONFLICT",
                "The request conflicts with the current state of the resource", request, null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Object> handleTypeMismatch(MethodArgumentTypeMismatchException ex, WebRequest request) {
        return build(HttpStatus.BAD_REQUEST, "INVALID_REQUEST",
                "Invalid value for parameter '" + ex.getName() + "'", request, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnexpected(Exception ex, WebRequest request) {
        log.error("Unexpected error", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "An unexpected error occurred", request, null);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatusCode status,
                                                                  WebRequest request) {
        List<ErrorResponse.FieldViolation> details = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ErrorResponse.FieldViolation(fe.getField(), fe.getDefaultMessage()))
                .toList();
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Request validation failed", request, details);
    }

    /** All remaining framework exceptions (bad JSON, missing header, 404, 405, 415 ...) land here. */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
                                                             HttpStatusCode statusCode, WebRequest request) {
        HttpStatus status = HttpStatus.resolve(statusCode.value());
        String message = (ex instanceof org.springframework.web.ErrorResponse er && er.getBody().getDetail() != null)
                ? er.getBody().getDetail()
                : (status != null ? status.getReasonPhrase() : "Request failed");
        String code = statusCode.is4xxClientError() ? "INVALID_REQUEST" : "INTERNAL_ERROR";
        if (statusCode.value() == 404) {
            code = "NOT_FOUND";
        }
        return build(statusCode, code, message, request, null);
    }

    private ResponseEntity<Object> build(HttpStatusCode status, String code, String message, WebRequest request,
                                         List<ErrorResponse.FieldViolation> details) {
        HttpStatus resolved = HttpStatus.resolve(status.value());
        ErrorResponse body = new ErrorResponse(
                Instant.now(),
                status.value(),
                resolved != null ? resolved.getReasonPhrase() : "Error",
                code,
                message,
                path(request),
                MDC.get("correlationId"),
                details);
        return ResponseEntity.status(status).body(body);
    }

    private String path(WebRequest request) {
        return request instanceof ServletWebRequest swr ? swr.getRequest().getRequestURI() : null;
    }
}
