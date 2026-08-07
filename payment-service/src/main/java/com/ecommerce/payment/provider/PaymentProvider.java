package com.ecommerce.payment.provider;

import com.ecommerce.payment.dto.PaymentRequest;

public interface PaymentProvider {

    PaymentResult processPayment(PaymentRequest request);

    record PaymentResult(boolean success, String transactionReference, String errorMessage) {}
}
