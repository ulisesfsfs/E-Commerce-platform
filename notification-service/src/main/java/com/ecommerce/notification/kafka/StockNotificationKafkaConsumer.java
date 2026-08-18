package com.ecommerce.notification.kafka;

import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.retry.annotation.Backoff;
import org.springframework.kafka.retrytopic.DltStrategy;

import com.ecommerce.notification.event.StockReservationFailedEvent;
import com.ecommerce.notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockNotificationKafkaConsumer {

    private final EmailService emailService;

    @RetryableTopic(attempts = "3", backoff = @Backoff(delay = 2000, multiplier = 2.0), dltStrategy = DltStrategy.FAIL_ON_ERROR)
    @KafkaListener(topics = "stock-events", groupId = "notification-group")
    public void consumeStockReservationFailed(StockReservationFailedEvent event) {
        log.info("NotificationService received StockReservationFailedEvent for orderId={}", event.getOrderId());

        String recipientEmail = "customer_order_" + event.getOrderId() + "@example.com";
        emailService.sendStockFailureNotification(recipientEmail, event);
    }

    @DltHandler
    public void handleDltStockReservationFailed(StockReservationFailedEvent event) {
        log.error("Failed to process stock-events after retries for orderId={}", event.getOrderId());
    }
}
