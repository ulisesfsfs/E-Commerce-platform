package com.ecommerce.payment.service;

import com.ecommerce.payment.domain.Payment;
import com.ecommerce.payment.domain.PaymentMethod;
import com.ecommerce.payment.domain.PaymentStatus;
import com.ecommerce.payment.dto.PaymentRequest;
import com.ecommerce.payment.dto.PaymentResponse;
import com.ecommerce.payment.kafka.PaymentKafkaProducer;
import com.ecommerce.payment.provider.PaymentProvider;
import com.ecommerce.payment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentProvider paymentProvider;

    @Mock
    private PaymentKafkaProducer kafkaProducer;

    private MeterRegistry meterRegistry = new SimpleMeterRegistry();
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentServiceImpl(paymentRepository, paymentProvider, kafkaProducer, meterRegistry);
    }

    @Test
    void processPayment_Success() {
        PaymentRequest request = PaymentRequest.builder()
                .orderId(10L)
                .userId("user123")
                .amount(new BigDecimal("150.00"))
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .build();

        PaymentProvider.PaymentResult result = new PaymentProvider.PaymentResult(true, "tx_123456", null);
        when(paymentRepository.findByOrderId(10L)).thenReturn(Optional.empty());
        when(paymentProvider.processPayment(request)).thenReturn(result);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });

        PaymentResponse response = paymentService.processPayment(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(PaymentStatus.SUCCESS, response.getStatus());
        assertEquals("tx_123456", response.getTransactionReference());

        verify(kafkaProducer).sendPaymentEvent(any());
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void processPayment_Failure() {
        PaymentRequest request = PaymentRequest.builder()
                .orderId(10L)
                .userId("user123")
                .amount(new BigDecimal("150.00"))
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .simulateFailure(true)
                .build();

        PaymentProvider.PaymentResult result = new PaymentProvider.PaymentResult(false, null, "Insufficient funds");
        when(paymentRepository.findByOrderId(10L)).thenReturn(Optional.empty());
        when(paymentProvider.processPayment(request)).thenReturn(result);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });

        PaymentResponse response = paymentService.processPayment(request);

        assertNotNull(response);
        assertEquals(PaymentStatus.FAILED, response.getStatus());

        verify(kafkaProducer).sendPaymentEvent(any());
    }

    @Test
    void processPayment_IdempotentKeyExists_ReturnsSavedPayment() {
        Payment existingPayment = Payment.builder()
                .id(1L)
                .orderId(10L)
                .userId("user123")
                .amount(new BigDecimal("150.00"))
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .status(PaymentStatus.SUCCESS)
                .idempotencyKey("key-xyz")
                .transactionReference("tx_existing")
                .build();

        PaymentRequest request = PaymentRequest.builder()
                .orderId(10L)
                .userId("user123")
                .amount(new BigDecimal("150.00"))
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .idempotencyKey("key-xyz")
                .build();

        when(paymentRepository.findByIdempotencyKey("key-xyz")).thenReturn(Optional.of(existingPayment));

        PaymentResponse response = paymentService.processPayment(request);

        assertEquals(1L, response.getId());
        assertEquals("tx_existing", response.getTransactionReference());
        verify(paymentProvider, never()).processPayment(any());
        verify(kafkaProducer, never()).sendPaymentEvent(any());
    }

    @Test
    void processPayment_AlreadyPaidOrder_ThrowsBadRequest() {
        Payment existingSuccess = Payment.builder()
                .id(1L)
                .orderId(10L)
                .status(PaymentStatus.SUCCESS)
                .build();

        PaymentRequest request = PaymentRequest.builder()
                .orderId(10L)
                .userId("user123")
                .amount(new BigDecimal("150.00"))
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .build();

        when(paymentRepository.findByOrderId(10L)).thenReturn(Optional.of(existingSuccess));

        assertThrows(ResponseStatusException.class, () -> paymentService.processPayment(request));
        verify(paymentProvider, never()).processPayment(any());
    }
}
