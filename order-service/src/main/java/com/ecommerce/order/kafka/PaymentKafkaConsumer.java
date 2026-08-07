package com.ecommerce.order.kafka;

import com.ecommerce.order.domain.OrderStatus;
import com.ecommerce.order.event.PaymentProcessedEvent;
import com.ecommerce.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentKafkaConsumer {

    private final OrderService orderService;

    @KafkaListener(topics = "payment-events", groupId = "order-group")
    public void consumePaymentEvent(PaymentProcessedEvent event) {
        log.info("Received PaymentProcessedEvent from Kafka: orderId={}, status={}",
                event.getOrderId(), event.getStatus());

        try {
            if ("SUCCESS".equalsIgnoreCase(event.getStatus())) {
                orderService.updateOrderStatus(event.getOrderId(), OrderStatus.CONFIRMED);
                log.info("Order id '{}' successfully updated to CONFIRMED following payment success.", event.getOrderId());
            } else if ("FAILED".equalsIgnoreCase(event.getStatus())) {
                orderService.updateOrderStatus(event.getOrderId(), OrderStatus.CANCELLED);
                log.warn("Order id '{}' updated to CANCELLED and stock restored following payment failure.", event.getOrderId());
            } else {
                log.warn("Received unknown payment status '{}' for orderId={}", event.getStatus(), event.getOrderId());
            }
        } catch (Exception e) {
            log.error("Failed to process payment event for orderId={}", event.getOrderId(), e);
        }
    }
}
