package com.tickethub.payment.controller;

import com.tickethub.payment.dto.PaymentInitiationRequest;
import com.tickethub.payment.dto.PaymentResponse;
import com.tickethub.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "Payments", description = "TicketHub Payment Service APIs")
public class PaymentController {

    private final PaymentService service;

    public PaymentController(PaymentService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Initiate a payment for a PENDING_PAYMENT booking")
    public ResponseEntity<PaymentResponse> initiate(
            @Valid @RequestBody PaymentInitiationRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            Authentication authentication) {

        PaymentResponse response = service.initiate(
                request,
                authentication.getName(),
                idempotencyKey
        );

        return ResponseEntity.created(
                URI.create("/api/v1/payments/" + response.paymentId())
        ).body(response);
    }

    @GetMapping("/{paymentId}")
    @Operation(summary = "Get the authenticated customer's payment")
    public PaymentResponse get(
            @PathVariable Long paymentId,
            Authentication authentication) {
        return service.get(paymentId, authentication.getName());
    }

    @PostMapping("/webhook")
    @Operation(summary = "Receive and verify Razorpay webhook")
    public ResponseEntity<Void> webhook(
            @RequestHeader(name = "X-Razorpay-Signature", required = false) String signature,
            @RequestBody String rawBody) {

        if (signature == null || signature.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        service.processWebhook(rawBody, signature);
        return ResponseEntity.ok().build();
    }
}
