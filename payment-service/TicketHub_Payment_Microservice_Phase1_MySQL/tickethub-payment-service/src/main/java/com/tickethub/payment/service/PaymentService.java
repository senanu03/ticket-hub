package com.tickethub.payment.service;

import com.tickethub.payment.client.BookingServiceClient;
import com.tickethub.payment.dto.BookingPaymentDetails;
import com.tickethub.payment.dto.PaymentInitiationRequest;
import com.tickethub.payment.dto.PaymentResponse;
import com.tickethub.payment.dto.PaymentResultRequest;
import com.tickethub.payment.entity.Payment;
import com.tickethub.payment.entity.PaymentProvider;
import com.tickethub.payment.entity.PaymentStatus;
import com.tickethub.payment.event.PaymentEventPublisher;
import com.tickethub.payment.exception.PaymentConflictException;
import com.tickethub.payment.exception.PaymentNotFoundException;
import com.tickethub.payment.provider.PaymentProviderClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class PaymentService {

    private static final Logger log =
            LoggerFactory.getLogger(PaymentService.class);

    private final com.tickethub.payment.repository.PaymentRepository repository;
    private final BookingServiceClient bookingClient;
    private final PaymentProviderClient provider;
    private final PaymentEventPublisher eventPublisher;
    private final com.tickethub.payment.config.RazorpayProperties razorpayProperties;

    public PaymentService(
            com.tickethub.payment.repository.PaymentRepository repository,
            BookingServiceClient bookingClient,
            PaymentProviderClient provider,
            PaymentEventPublisher eventPublisher,
            com.tickethub.payment.config.RazorpayProperties razorpayProperties) {

        this.repository = repository;
        this.bookingClient = bookingClient;
        this.provider = provider;
        this.eventPublisher = eventPublisher;
        this.razorpayProperties = razorpayProperties;
    }

    @Transactional
    public PaymentResponse initiate(
            PaymentInitiationRequest request,
            String customerId,
            String idempotencyKey) {

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new PaymentConflictException(
                    "Idempotency-Key header is required");
        }

        var existingByKey =
                repository.findByIdempotencyKey(idempotencyKey);

        if (existingByKey.isPresent()) {

            Payment existing = existingByKey.get();

            if (!existing.getCustomerId().equals(customerId)) {
                throw new PaymentConflictException(
                        "Idempotency key belongs to another customer");
            }

            return toResponse(existing);
        }

        var existingByBooking =
                repository.findByBookingId(request.bookingId());

        if (existingByBooking.isPresent()) {

            Payment existing = existingByBooking.get();

            if (!existing.getCustomerId().equals(customerId)) {
                throw new PaymentConflictException(
                        "Booking is owned by another customer");
            }

            if (existing.getStatus() == PaymentStatus.SUCCESS) {
                return toResponse(existing);
            }

            throw new PaymentConflictException(
                    "A payment already exists for this booking");
        }

        BookingPaymentDetails booking =
                bookingClient.getPaymentDetails(request.bookingId());

        if (!customerId.equals(booking.customerId())) {
            throw new PaymentConflictException(
                    "Booking is owned by another customer");
        }

        if (!"PENDING_PAYMENT".equals(booking.status())) {
            throw new PaymentConflictException(
                    "Booking is not in PENDING_PAYMENT state");
        }

        if (booking.expiryAt() != null
                && booking.expiryAt().isBefore(Instant.now())) {

            throw new PaymentConflictException(
                    "Booking reservation has expired");
        }

        Payment payment = new Payment();

        payment.setBookingId(booking.bookingId());
        payment.setCustomerId(customerId);
        payment.setAmount(booking.amount());
        payment.setCurrency(booking.currency());
        payment.setMethod(request.method());

        payment.setProvider(
                provider.isRazorpay()
                        ? PaymentProvider.RAZORPAY
                        : PaymentProvider.SIMULATED
        );

        payment.setStatus(PaymentStatus.INITIATED);
        payment.setIdempotencyKey(idempotencyKey);

        repository.saveAndFlush(payment);

        PaymentProviderClient.ProviderOrder order =
                provider.createOrder(payment);

        payment.setProviderOrderId(order.providerOrderId());
        payment.setProviderReference(order.providerReference());
        payment.setStatus(PaymentStatus.PENDING);

        repository.save(payment);

        log.info(
                "Payment initiated paymentId={} bookingId={} customerId={} provider={} status={}",
                payment.getId(),
                payment.getBookingId(),
                payment.getCustomerId(),
                payment.getProvider(),
                payment.getStatus()
        );

        return toResponse(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse get(
            Long paymentId,
            String customerId) {

        Payment payment =
                repository.findByIdAndCustomerId(
                                paymentId,
                                customerId)
                        .orElseThrow(() ->
                                new PaymentNotFoundException(
                                        "Payment not found"
                                ));

        return toResponse(payment);
    }

    @Transactional
    public void processWebhook(
            String rawBody,
            String signature) {

        try {

            boolean valid =
                    com.razorpay.Utils.verifyWebhookSignature(
                            rawBody,
                            signature,
                            razorpayProperties.webhookSecret()
                    );

            if (!valid) {
                throw new PaymentConflictException(
                        "Invalid Razorpay webhook signature");
            }

        } catch (com.razorpay.RazorpayException ex) {

            throw new PaymentConflictException(
                    "Invalid Razorpay webhook signature");
        }

        org.json.JSONObject root =
                new org.json.JSONObject(rawBody);

        String event =
                root.optString("event");

        org.json.JSONObject payload =
                root.optJSONObject("payload");

        if (payload == null) {
            throw new PaymentConflictException(
                    "Webhook payload is missing");
        }

        org.json.JSONObject paymentNode =
                payload.optJSONObject("payment");

        if (paymentNode == null) {
            return;
        }

        org.json.JSONObject entity =
                paymentNode.optJSONObject("entity");

        if (entity == null) {
            return;
        }

        String providerPaymentId =
                entity.optString("id", null);

        String providerOrderId =
                entity.optString("order_id", null);

        if (providerOrderId == null
                || providerOrderId.isBlank()) {

            throw new PaymentConflictException(
                    "Razorpay order ID is missing");
        }

        Payment payment =
                repository.findByProviderOrderIdForUpdate(
                                providerOrderId)
                        .orElseThrow(() ->
                                new PaymentNotFoundException(
                                        "No TicketHub payment found for Razorpay order"
                                ));

        /*
         * IMPORTANT:
         *
         * If Payment is already SUCCESS, do NOT simply return.
         *
         * The previous webhook may have successfully updated Payment
         * but failed to update Booking.
         *
         * A later Razorpay webhook for the same payment can therefore
         * be used to retry the Booking update.
         */
        if (payment.getStatus() == PaymentStatus.SUCCESS
                && ("payment.captured".equals(event)
                || "order.paid".equals(event))) {

            sendPaymentResultToBooking(payment);

            return;
        }

        if (payment.getStatus() == PaymentStatus.FAILED
                && "payment.failed".equals(event)) {

            sendPaymentResultToBooking(payment);

            return;
        }

        switch (event) {

            case "payment.captured", "order.paid" ->
                    markSuccess(
                            payment,
                            providerPaymentId,
                            providerOrderId
                    );

            case "payment.failed" ->
                    markFailed(
                            payment,
                            providerPaymentId,
                            providerOrderId
                    );

            default ->
                    log.debug(
                            "Ignoring unsupported Razorpay webhook event={}",
                            event
                    );
        }
    }

    private void markSuccess(
            Payment payment,
            String providerPaymentId,
            String providerOrderId) {

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            sendPaymentResultToBooking(payment);
            return;
        }

        if (payment.getStatus() == PaymentStatus.FAILED) {
            log.warn(
                    "Ignoring late SUCCESS after FAILED paymentId={}",
                    payment.getId()
            );
            return;
        }

        payment.setProviderPaymentId(providerPaymentId);
        payment.setProviderOrderId(providerOrderId);
        payment.setProviderReference(providerPaymentId);
        payment.setStatus(PaymentStatus.SUCCESS);

        repository.save(payment);

        sendPaymentResultToBooking(payment);

        eventPublisher.publish(payment);
    }

    private void markFailed(
            Payment payment,
            String providerPaymentId,
            String providerOrderId) {

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            log.warn(
                    "Ignoring late FAILED after SUCCESS paymentId={}",
                    payment.getId()
            );
            return;
        }

        if (payment.getStatus() == PaymentStatus.FAILED) {
            sendPaymentResultToBooking(payment);
            return;
        }

        payment.setProviderPaymentId(providerPaymentId);
        payment.setProviderOrderId(providerOrderId);
        payment.setProviderReference(providerPaymentId);
        payment.setStatus(PaymentStatus.FAILED);

        repository.save(payment);

        sendPaymentResultToBooking(payment);

        eventPublisher.publish(payment);
    }

    @Transactional
    public void reconcilePaymentWithBooking(Long paymentId) {
        Payment payment = repository.findById(paymentId).orElse(null);
        if (payment == null || payment.isBookingNotified()) {
            return;
        }
        sendPaymentResultToBooking(payment);
    }

    private void sendPaymentResultToBooking(
            Payment payment) {

        /*
         * Booking Service's paymentId is the TicketHub Payment ID.
         *
         * Example:
         * Payment entity ID = 10
         * Booking receives paymentId = "10"
         */
        String paymentId =
                String.valueOf(payment.getId());

        try {
            bookingClient.sendPaymentResult(
                    payment.getBookingId(),
                    new PaymentResultRequest(
                            paymentId,
                            payment.getStatus(),
                            payment.getAmount(),
                            payment.getProviderReference(),
                            payment.getProviderPaymentId(),
                            payment.getStatus() == PaymentStatus.FAILED
                                    ? "Razorpay payment failed"
                                    : null
                    )
            );

            payment.setBookingNotified(true);
            repository.save(payment);

            log.info(
                    "Booking payment result updated paymentId={} bookingId={} status={}",
                    paymentId,
                    payment.getBookingId(),
                    payment.getStatus()
            );
        } catch (Exception ex) {
            log.warn(
                    "Booking Service unavailable for payment result notification paymentId={} bookingId={}. Reconciliation job will retry: {}",
                    paymentId,
                    payment.getBookingId(),
                    ex.getMessage()
            );
        }
    }

    private PaymentResponse toResponse(
            Payment p) {

        return new PaymentResponse(
                p.getId(),
                p.getBookingId(),
                p.getAmount(),
                p.getCurrency(),
                p.getStatus(),
                p.getProvider(),
                p.getMethod(),
                p.getProviderReference(),
                p.getProviderOrderId(),
                p.getProviderPaymentId(),
                provider.isRazorpay()
                        ? razorpayProperties.keyId()
                        : null,
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }
}