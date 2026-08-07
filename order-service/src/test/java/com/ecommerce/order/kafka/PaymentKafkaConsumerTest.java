package com.ecommerce.order.kafka;

import com.ecommerce.order.domain.OrderStatus;
import com.ecommerce.order.event.PaymentProcessedEvent;
import com.ecommerce.order.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentKafkaConsumerTest {

    @Mock
    private OrderService orderService;

    private PaymentKafkaConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new PaymentKafkaConsumer(orderService);
    }

    @Test
    void consumePaymentEvent_Success_UpdatesOrderToConfirmed() {
        PaymentProcessedEvent event = PaymentProcessedEvent.builder()
                .paymentId(1L)
                .orderId(100L)
                .status("SUCCESS")
                .amount(new BigDecimal("99.99"))
                .build();

        consumer.consumePaymentEvent(event);

        verify(orderService).updateOrderStatus(100L, OrderStatus.CONFIRMED);
    }

    @Test
    void consumePaymentEvent_Failed_UpdatesOrderToCancelled() {
        PaymentProcessedEvent event = PaymentProcessedEvent.builder()
                .paymentId(1L)
                .orderId(100L)
                .status("FAILED")
                .amount(new BigDecimal("99.99"))
                .build();

        consumer.consumePaymentEvent(event);

        verify(orderService).updateOrderStatus(100L, OrderStatus.CANCELLED);
    }
}
