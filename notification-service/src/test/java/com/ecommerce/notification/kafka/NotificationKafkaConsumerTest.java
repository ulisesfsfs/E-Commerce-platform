package com.ecommerce.notification.kafka;

import com.ecommerce.notification.event.PaymentProcessedEvent;
import com.ecommerce.notification.event.StockReservationFailedEvent;
import com.ecommerce.notification.service.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationKafkaConsumerTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private PaymentNotificationKafkaConsumer paymentConsumer;

    @InjectMocks
    private StockNotificationKafkaConsumer stockConsumer;

    @Test
    void consumePaymentProcessed_Success_TriggersSuccessEmail() {
        PaymentProcessedEvent event = PaymentProcessedEvent.builder()
                .paymentId(1L)
                .orderId(101L)
                .userId("john.doe@example.com")
                .amount(new BigDecimal("150.00"))
                .status("SUCCESS")
                .paymentMethod("CREDIT_CARD")
                .transactionReference("tx_123")
                .build();

        paymentConsumer.consumePaymentProcessed(event);

        verify(emailService).sendPaymentSuccessNotification(eq("john.doe@example.com"), any());
    }

    @Test
    void consumePaymentProcessed_Failed_TriggersFailureEmail() {
        PaymentProcessedEvent event = PaymentProcessedEvent.builder()
                .paymentId(1L)
                .orderId(101L)
                .userId("john.doe@example.com")
                .amount(new BigDecimal("150.00"))
                .status("FAILED")
                .paymentMethod("CREDIT_CARD")
                .transactionReference("tx_123")
                .build();

        paymentConsumer.consumePaymentProcessed(event);

        verify(emailService).sendPaymentFailureNotification(eq("john.doe@example.com"), any());
    }

    @Test
    void consumeStockReservationFailed_TriggersEmail() {
        StockReservationFailedEvent event = StockReservationFailedEvent.builder()
                .orderId(101L)
                .productId("p1")
                .requestedQuantity(3)
                .reason("Insufficient stock")
                .build();

        stockConsumer.consumeStockReservationFailed(event);

        verify(emailService).sendStockFailureNotification(any(), any());
    }
}
