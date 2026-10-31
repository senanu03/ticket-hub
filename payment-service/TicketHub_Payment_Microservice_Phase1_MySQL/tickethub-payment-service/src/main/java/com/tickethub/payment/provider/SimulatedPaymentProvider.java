package com.tickethub.payment.provider;

import com.tickethub.payment.entity.Payment;
import com.tickethub.payment.exception.ProviderException;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("test-simulated")
public class SimulatedPaymentProvider implements PaymentProviderClient {

    @Override
    public ProviderOrder createOrder(Payment payment) {
        return new ProviderOrder(
                "sim_order_" + payment.getId(),
                "sim_order_" + payment.getId(),
                payment.getAmount(),
                payment.getCurrency()
        );
    }

    @Override
    public boolean isRazorpay() {
        return false;
    }
}
