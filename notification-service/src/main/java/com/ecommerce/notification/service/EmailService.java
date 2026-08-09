package com.ecommerce.notification.service;

import com.ecommerce.notification.event.PaymentProcessedEvent;
import com.ecommerce.notification.event.StockReservationFailedEvent;

public interface EmailService {

    void sendPaymentSuccessNotification(String recipientEmail, PaymentProcessedEvent event);

    void sendPaymentFailureNotification(String recipientEmail, PaymentProcessedEvent event);

    void sendStockFailureNotification(String recipientEmail, StockReservationFailedEvent event);
}
