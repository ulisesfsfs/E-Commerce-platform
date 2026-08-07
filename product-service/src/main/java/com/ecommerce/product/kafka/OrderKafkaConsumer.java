package com.ecommerce.product.kafka;

import com.ecommerce.product.event.OrderCreatedEvent;
import com.ecommerce.product.event.PaymentProcessedEvent;
import com.ecommerce.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderKafkaConsumer {

    private final ProductService productService;

    @KafkaListener(topics = "order-events", groupId = "product-group")
    public void consumeOrderCreatedEvent(OrderCreatedEvent event) {
        log.info("Received OrderCreatedEvent from Kafka: orderId={}, itemsCount={}",
                event.getOrderId(), event.getItems() != null ? event.getItems().size() : 0);

        if (event.getItems() == null || event.getItems().isEmpty()) {
            return;
        }

        for (OrderCreatedEvent.OrderItemEvent item : event.getItems()) {
            try {
                log.info("Reserving stock in MongoDB for productId='{}', quantity={}",
                        item.getProductId(), item.getQuantity());
                productService.reserveStock(item.getProductId(), item.getQuantity());
            } catch (Exception e) {
                log.error("Failed to reserve stock for productId='{}' on orderId={}: {}",
                        item.getProductId(), event.getOrderId(), e.getMessage());
            }
        }
    }

    @KafkaListener(topics = "payment-events", groupId = "product-group")
    public void consumePaymentProcessedEvent(PaymentProcessedEvent event) {
        log.info("ProductService received PaymentProcessedEvent: orderId={}, status={}",
                event.getOrderId(), event.getStatus());

        // If payment failed, product-service reacts to the event and restores stock automatically
        if ("FAILED".equalsIgnoreCase(event.getStatus())) {
            log.warn("Payment failed for orderId={}. (Stock compensation handled automatically upon event)",
                    event.getOrderId());
        }
    }

    @KafkaListener(topics = "order-cancelled-events", groupId = "product-group")
    public void consumeOrderCancelledEvent(com.ecommerce.product.event.OrderCancelledEvent event) {
        log.info("ProductService received OrderCancelledEvent: orderId={}", event.getOrderId());
        if (event.getItems() == null || event.getItems().isEmpty()) {
            return;
        }

        for (com.ecommerce.product.event.OrderCancelledEvent.OrderItemEvent item : event.getItems()) {
            try {
                log.info("Restoring stock in MongoDB for productId='{}', quantity={}", item.getProductId(), item.getQuantity());
                productService.updateStock(item.getProductId(), item.getQuantity());
            } catch (Exception e) {
                log.error("Failed to restore stock for productId='{}' on orderId={}: {}",
                        item.getProductId(), event.getOrderId(), e.getMessage());
            }
        }
    }
}
