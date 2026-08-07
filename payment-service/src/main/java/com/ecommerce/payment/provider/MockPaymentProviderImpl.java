package com.ecommerce.payment.provider;

import com.ecommerce.payment.dto.PaymentRequest;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MockPaymentProviderImpl implements PaymentProvider {

    @Override
    public PaymentResult processPayment(PaymentRequest request) {
        // If client explicitly requests simulated failure for resilience testing
        if (request.isSimulateFailure()) {
            return new PaymentResult(false, null, "Simulated payment gateway decline: Insufficient funds");
        }

        // Generate unique transaction ID simulating payment gateway (Stripe/MercadoPago)
        String txRef = "tx_" + UUID.randomUUID().toString().substring(0, 18);
        return new PaymentResult(true, txRef, null);
    }
}
