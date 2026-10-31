package com.tickethub.payment.repository;

import com.tickethub.payment.entity.Payment;
import com.tickethub.payment.entity.PaymentStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByBookingId(Long bookingId);

    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select p
        from Payment p
        where p.providerOrderId = :providerOrderId
        """)
    Optional<Payment> findByProviderOrderIdForUpdate(
            @Param("providerOrderId") String providerOrderId);

    Optional<Payment> findByProviderOrderId(String providerOrderId);

    Optional<Payment> findByProviderPaymentId(String providerPaymentId);

    Optional<Payment> findByProviderReference(String providerReference);

    Optional<Payment> findByIdAndCustomerId(Long id, String customerId);

    long countByStatus(PaymentStatus status);

    java.util.List<Payment> findByStatusInAndBookingNotifiedFalse(java.util.List<PaymentStatus> statuses);
}