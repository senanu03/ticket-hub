package com.tickethub.payment.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;


@Entity
@Table(name = "payments",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_payment_booking", columnNames = "booking_id"),
           @UniqueConstraint(name = "uk_payment_idempotency", columnNames = "idempotency_key"),
           @UniqueConstraint(name = "uk_payment_provider_ref", columnNames = "provider_reference")
       },
       indexes = {
           @Index(name = "idx_payment_customer", columnList = "customer_id"),
           @Index(name = "idx_payment_status", columnList = "status")
       })
public class Payment {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "booking_id", nullable = false, updatable = false)
    private Long bookingId;

    // Snapshot of the authenticated principal; never accepted from the request body.
    @Column(name = "customer_id", nullable = false, updatable = false, length = 100)
    private String customerId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentProvider provider;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 30)
    private PaymentMethod method;

    // Razorpay order id initially; Razorpay payment id after capture.
    @Column(name = "provider_reference", length = 100)
    private String providerReference;

    @Column(name = "provider_order_id", length = 100)
    private String providerOrderId;

    @Column(name = "provider_payment_id", length = 100)
    private String providerPaymentId;

    @Column(name = "idempotency_key", nullable = false, length = 100, updatable = false)
    private String idempotencyKey;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    private long version;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public Long getBookingId() { return bookingId; }
    public String getCustomerId() { return customerId; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public PaymentStatus getStatus() { return status; }
    public PaymentProvider getProvider() { return provider; }
    public PaymentMethod getMethod() { return method; }
    public String getProviderReference() { return providerReference; }
    public String getProviderOrderId() { return providerOrderId; }
    public String getProviderPaymentId() { return providerPaymentId; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public long getVersion() { return version; }

    @Column(name = "booking_notified", nullable = false)
    private boolean bookingNotified = false;

    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public void setCurrency(String currency) { this.currency = currency; }
    public void setStatus(PaymentStatus status) { this.status = status; }
    public void setProvider(PaymentProvider provider) { this.provider = provider; }
    public void setMethod(PaymentMethod method) { this.method = method; }
    public void setProviderReference(String providerReference) { this.providerReference = providerReference; }
    public void setProviderOrderId(String providerOrderId) { this.providerOrderId = providerOrderId; }
    public void setProviderPaymentId(String providerPaymentId) { this.providerPaymentId = providerPaymentId; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public boolean isBookingNotified() { return bookingNotified; }
    public void setBookingNotified(boolean bookingNotified) { this.bookingNotified = bookingNotified; }
}
