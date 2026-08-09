package com.ecommerce.product.kafka;

import com.ecommerce.product.event.StockReservationFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockKafkaProducer {

    public static final String TOPIC_STOCK_EVENTS = "stock-events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendStockReservationFailed(StockReservationFailedEvent event) {
        log.warn("Publishing StockReservationFailedEvent to Kafka topic '{}': orderId={}, productId={}, reason={}",
                TOPIC_STOCK_EVENTS, event.getOrderId(), event.getProductId(), event.getReason());

        kafkaTemplate.send(TOPIC_STOCK_EVENTS, String.valueOf(event.getOrderId()), event);
    }
}
