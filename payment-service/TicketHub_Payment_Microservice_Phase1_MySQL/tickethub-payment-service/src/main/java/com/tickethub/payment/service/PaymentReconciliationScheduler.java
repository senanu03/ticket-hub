package com.tickethub.payment.service;

import com.tickethub.payment.entity.Payment;
import com.tickethub.payment.entity.PaymentStatus;
import com.tickethub.payment.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Periodically reconciles completed payments (SUCCESS/FAILED) where the
 * Booking Service notification failed or was temporarily unavailable.
 */
@Component
public class PaymentReconciliationScheduler {

    private static final Logger log = LoggerFactory.getLogger(PaymentReconciliationScheduler.class);

    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;

    public PaymentReconciliationScheduler(
            PaymentRepository paymentRepository,
            PaymentService paymentService) {
        this.paymentRepository = paymentRepository;
        this.paymentService = paymentService;
    }

    @Scheduled(fixedDelayString = "${tickethub.payment.reconciliation.interval-ms:30000}")
    public void reconcilePendingBookingUpdates() {
        List<Payment> unnotified = paymentRepository.findByStatusInAndBookingNotifiedFalse(
                List.of(PaymentStatus.SUCCESS, PaymentStatus.FAILED)
        );

        if (unnotified.isEmpty()) {
            return;
        }

        log.info("Running payment reconciliation for {} unnotified payments", unnotified.size());
        for (Payment payment : unnotified) {
            try {
                paymentService.reconcilePaymentWithBooking(payment.getId());
            } catch (Exception ex) {
                log.error("Reconciliation failed for paymentId={}: {}", payment.getId(), ex.getMessage());
            }
        }
    }
}
