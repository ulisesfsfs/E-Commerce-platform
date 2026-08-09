package com.ecommerce.order.kafka;

import com.ecommerce.order.domain.OrderStatus;
import com.ecommerce.order.event.StockReservationFailedEvent;
import com.ecommerce.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockKafkaConsumer {

    private final OrderRepository orderRepository;

    /**
     * Listens for StockReservationFailedEvent.
     * When product-service cannot reserve stock (insufficient stock or inactive product),
     * this consumer automatically cancels the order to close the Saga compensation loop.
     */
    @Transactional
    @KafkaListener(topics = "stock-events", groupId = "order-group")
    public void consumeStockReservationFailed(StockReservationFailedEvent event) {
        log.warn("OrderService received StockReservationFailedEvent: orderId={}, productId={}, reason={}",
                event.getOrderId(), event.getProductId(), event.getReason());

        orderRepository.findById(event.getOrderId()).ifPresentOrElse(order -> {
            if (order.getStatus() == OrderStatus.PENDING) {
                order.setStatus(OrderStatus.CANCELLED);
                orderRepository.save(order);
                log.warn("Order #{} automatically CANCELLED due to insufficient stock for productId='{}'",
                        event.getOrderId(), event.getProductId());
            } else {
                log.info("Order #{} already in status '{}', skipping cancellation.",
                        event.getOrderId(), order.getStatus());
            }
        }, () -> log.error("StockReservationFailedEvent received for unknown orderId={}", event.getOrderId()));
    }
}
