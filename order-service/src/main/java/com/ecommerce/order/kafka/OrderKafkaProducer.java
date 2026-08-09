package com.ecommerce.order.kafka;

import com.ecommerce.order.event.OrderCancelledEvent;
import com.ecommerce.order.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderKafkaProducer {

    public static final String TOPIC_ORDER_EVENTS = "order-events";
    public static final String TOPIC_ORDER_CANCELLED_EVENTS = "order-cancelled-events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendOrderCreatedEvent(OrderCreatedEvent event) {
        log.info("Publishing OrderCreatedEvent to Kafka topic '{}': orderId={}, userId={}",
                TOPIC_ORDER_EVENTS, event.getOrderId(), event.getUserId());
        kafkaTemplate.send(TOPIC_ORDER_EVENTS, String.valueOf(event.getOrderId()), event);
    }

    public void sendOrderCancelledEvent(OrderCancelledEvent event) {
        log.info("Publishing OrderCancelledEvent to Kafka topic '{}': orderId={}",
                TOPIC_ORDER_CANCELLED_EVENTS, event.getOrderId());
        kafkaTemplate.send(TOPIC_ORDER_CANCELLED_EVENTS, String.valueOf(event.getOrderId()), event);
    }
}
