package com.tp.snapseat.notification_service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BookingResponseDTO {

    private String eventId;
    private String eventType;
    private int eventVersion;
    private Long bookingId;
    private String customerId;
    private String customerEmail;
    private Long showId;
    private List<Long> seatIds;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String paymentId;
    private String occurredAt;

    public BookingResponseDTO() {}

    public BookingResponseDTO(String eventId, String eventType, int eventVersion, Long bookingId,
                              String customerId, String customerEmail, Long showId, List<Long> seatIds,
                              BigDecimal amount, String currency, String status, String paymentId,
                              String occurredAt) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.eventVersion = eventVersion;
        this.bookingId = bookingId;
        this.customerId = customerId;
        this.customerEmail = customerEmail;
        this.showId = showId;
        this.seatIds = seatIds;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.paymentId = paymentId;
        this.occurredAt = occurredAt;
    }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public int getEventVersion() { return eventVersion; }
    public void setEventVersion(int eventVersion) { this.eventVersion = eventVersion; }
    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    public Long getShowId() { return showId; }
    public void setShowId(Long showId) { this.showId = showId; }
    public List<Long> getSeatIds() { return seatIds; }
    public void setSeatIds(List<Long> seatIds) { this.seatIds = seatIds; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }
    public String getOccurredAt() { return occurredAt; }
    public void setOccurredAt(String occurredAt) { this.occurredAt = occurredAt; }
}