package com.tickethub.payment.provider;

import com.tickethub.payment.entity.Payment;
import com.tickethub.payment.entity.PaymentMethod;

import java.math.BigDecimal;

public interface PaymentProviderClient {

    ProviderOrder createOrder(Payment payment);

    record ProviderOrder(
            String providerOrderId,
            String providerReference,
            BigDecimal amount,
            String currency
    ) {}

    boolean isRazorpay();

    default String publicKey() {
        return null;
    }
}
