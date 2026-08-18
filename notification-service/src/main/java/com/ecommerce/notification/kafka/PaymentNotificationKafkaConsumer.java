package com.ecommerce.notification.kafka;

import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.retry.annotation.Backoff;
import org.springframework.kafka.retrytopic.DltStrategy;

import com.ecommerce.notification.event.PaymentProcessedEvent;
import com.ecommerce.notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentNotificationKafkaConsumer {

    private final EmailService emailService;

    @RetryableTopic(attempts = "3", backoff = @Backoff(delay = 2000, multiplier = 2.0), dltStrategy = DltStrategy.FAIL_ON_ERROR)
    @KafkaListener(topics = "payment-events", groupId = "notification-group")
    public void consumePaymentProcessed(PaymentProcessedEvent event) {
        log.info("NotificationService received PaymentProcessedEvent for orderId={}, status={}",
                event.getOrderId(), event.getStatus());

        String recipientEmail = (event.getUserId() != null && event.getUserId().contains("@"))
                ? event.getUserId()
                : "user_" + event.getOrderId() + "@example.com";

        if ("SUCCESS".equalsIgnoreCase(event.getStatus())) {
            emailService.sendPaymentSuccessNotification(recipientEmail, event);
        } else {
            emailService.sendPaymentFailureNotification(recipientEmail, event);
        }
    }

    @DltHandler
    public void handleDltPaymentProcessed(PaymentProcessedEvent event) {
        log.error("Failed to process payment_events after retries for orderId={}", event.getOrderId());
    }
}
