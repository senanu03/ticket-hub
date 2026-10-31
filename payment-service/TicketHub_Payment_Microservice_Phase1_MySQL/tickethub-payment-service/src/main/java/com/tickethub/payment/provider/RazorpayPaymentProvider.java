package com.tickethub.payment.provider;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.tickethub.payment.config.RazorpayProperties;
import com.tickethub.payment.entity.Payment;
import com.tickethub.payment.exception.ProviderException;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

@Component
public class RazorpayPaymentProvider implements PaymentProviderClient {

    private final RazorpayClient client;
    private final RazorpayProperties properties;

    public RazorpayPaymentProvider(RazorpayClient client, RazorpayProperties properties) {
        this.client = client;
        this.properties = properties;
    }

    @Override
    public ProviderOrder createOrder(Payment payment) {
        try {
            long amountInSubunits = payment.getAmount()
                    .movePointRight(2)
                    .longValueExact();

            JSONObject request = new JSONObject();
            request.put("amount", amountInSubunits);
            request.put("currency", payment.getCurrency());
            request.put("receipt", "TH-" + payment.getBookingId());
            request.put("partial_payment", false);

            JSONObject notes = new JSONObject();
            notes.put("ticketHub_payment_id", payment.getId().toString());
            notes.put("ticketHub_booking_id", payment.getBookingId().toString());
            request.put("notes", notes);

            Order order = client.orders.create(request);
            String orderId = order.get("id");
            return new ProviderOrder(orderId, orderId, payment.getAmount(), payment.getCurrency());
        } catch (RazorpayException | ArithmeticException ex) {
            throw new ProviderException("Unable to create Razorpay order", ex);
        }
    }

    @Override
    public boolean isRazorpay() {
        return true;
    }

    @Override
    public String publicKey() {
        return properties.keyId();
    }
}
