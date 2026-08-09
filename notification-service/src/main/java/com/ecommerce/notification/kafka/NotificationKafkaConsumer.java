package com.ecommerce.notification.kafka;

import com.ecommerce.notification.event.PaymentProcessedEvent;
import com.ecommerce.notification.event.StockReservationFailedEvent;
import com.ecommerce.notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationKafkaConsumer {

    private final EmailService emailService;

    @KafkaListener(topics = "payment-events", groupId = "notification-group")
    public void consumePaymentProcessed(PaymentProcessedEvent event) {
        log.info("NotificationService received PaymentProcessedEvent for orderId={}, status={}",
                event.getOrderId(), event.getStatus());

        String recipientEmail = (event.getUserId() != null && event.getUserId().contains("@"))
                ? event.getUserId() : "user_" + event.getOrderId() + "@example.com";

        if ("SUCCESS".equalsIgnoreCase(event.getStatus())) {
            emailService.sendPaymentSuccessNotification(recipientEmail, event);
        } else {
            emailService.sendPaymentFailureNotification(recipientEmail, event);
        }
    }

    @KafkaListener(topics = "stock-events", groupId = "notification-group")
    public void consumeStockReservationFailed(StockReservationFailedEvent event) {
        log.info("NotificationService received StockReservationFailedEvent for orderId={}", event.getOrderId());

        String recipientEmail = "customer_order_" + event.getOrderId() + "@example.com";
        emailService.sendStockFailureNotification(recipientEmail, event);
    }
}
