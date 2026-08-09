package com.ecommerce.product.kafka;

import com.ecommerce.product.event.OrderCancelledEvent;
import com.ecommerce.product.event.OrderCreatedEvent;
import com.ecommerce.product.event.PaymentProcessedEvent;
import com.ecommerce.product.event.StockReservationFailedEvent;
import com.ecommerce.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderKafkaConsumer {

    private final ProductService productService;
    private final StockKafkaProducer stockKafkaProducer;

    @KafkaListener(topics = "order-events", groupId = "product-group")
    public void consumeOrderCreatedEvent(OrderCreatedEvent event) {
        log.info("Received OrderCreatedEvent: orderId={}, itemsCount={}",
                event.getOrderId(), event.getItems() != null ? event.getItems().size() : 0);

        if (event.getItems() == null || event.getItems().isEmpty()) return;

        for (OrderCreatedEvent.OrderItemEvent item : event.getItems()) {
            try {
                log.info("Reserving stock for productId='{}', quantity={}", item.getProductId(), item.getQuantity());
                productService.reserveStock(item.getProductId(), item.getQuantity());
            } catch (ResponseStatusException e) {
                log.error("Insufficient stock for productId='{}' on orderId={}. Publishing StockReservationFailedEvent.",
                        item.getProductId(), event.getOrderId());
                stockKafkaProducer.sendStockReservationFailed(
                        StockReservationFailedEvent.builder()
                                .orderId(event.getOrderId())
                                .productId(item.getProductId())
                                .requestedQuantity(item.getQuantity())
                                .reason(e.getReason())
                                .build()
                );
            } catch (Exception e) {
                log.error("Unexpected error reserving stock for productId='{}' on orderId={}: {}",
                        item.getProductId(), event.getOrderId(), e.getMessage());
            }
        }
    }

    @KafkaListener(topics = "order-cancelled-events", groupId = "product-group")
    public void consumeOrderCancelledEvent(OrderCancelledEvent event) {
        log.info("Received OrderCancelledEvent: orderId={}", event.getOrderId());

        if (event.getItems() == null || event.getItems().isEmpty()) return;

        for (OrderCancelledEvent.OrderItemEvent item : event.getItems()) {
            try {
                log.info("Restoring stock for productId='{}', quantity={}", item.getProductId(), item.getQuantity());
                productService.updateStock(item.getProductId(), item.getQuantity());
            } catch (Exception e) {
                log.error("Failed to restore stock for productId='{}' on orderId={}: {}",
                        item.getProductId(), event.getOrderId(), e.getMessage());
            }
        }
    }

    @KafkaListener(topics = "payment-events", groupId = "product-group")
    public void consumePaymentProcessedEvent(PaymentProcessedEvent event) {
        log.info("Received PaymentProcessedEvent: orderId={}, status={}", event.getOrderId(), event.getStatus());

        if ("FAILED".equalsIgnoreCase(event.getStatus())) {
            log.warn("Payment failed for orderId={}. Stock compensation handled via Saga.", event.getOrderId());
        }
    }
}
