package com.ecommerce.payment.kafka;

import com.ecommerce.payment.event.PaymentProcessedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentKafkaProducer {

    public static final String TOPIC_PAYMENT_EVENTS = "payment-events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendPaymentEvent(PaymentProcessedEvent event) {
        log.info("Publishing PaymentProcessedEvent to Kafka topic '{}': orderId={}, status={}",
                TOPIC_PAYMENT_EVENTS, event.getOrderId(), event.getStatus());

        kafkaTemplate.send(TOPIC_PAYMENT_EVENTS, String.valueOf(event.getOrderId()), event);
    }
}
