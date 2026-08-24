package com.ecommerce.payment.service;

import com.ecommerce.payment.domain.Payment;
import com.ecommerce.payment.domain.PaymentStatus;
import com.ecommerce.payment.dto.PaymentRequest;
import com.ecommerce.payment.dto.PaymentResponse;
import com.ecommerce.payment.event.PaymentProcessedEvent;
import com.ecommerce.payment.kafka.PaymentKafkaProducer;
import com.ecommerce.payment.provider.PaymentProvider;
import com.ecommerce.payment.repository.PaymentRepository;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import io.micrometer.core.instrument.Timer;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentProvider paymentProvider;
    private final PaymentKafkaProducer kafkaProducer;
    private final MeterRegistry meterRegistry;

    @Override
    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            // 1. Verify Idempotency by Key (if sent)
            if (request.getIdempotencyKey() != null && !request.getIdempotencyKey().isBlank()) {
                Optional<Payment> existing = paymentRepository.findByIdempotencyKey(request.getIdempotencyKey());
                if (existing.isPresent()) {
                    log.info("Idempotent payment request detected for key '{}'. Returning saved payment.",
                            request.getIdempotencyKey());
                    return toResponse(existing.get());
                }
            }

            // 2. Verify if the order already has a successful payment
            Optional<Payment> existingOrderPayment = paymentRepository.findByOrderId(request.getOrderId());
            if (existingOrderPayment.isPresent() && existingOrderPayment.get().getStatus() == PaymentStatus.SUCCESS) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Order with id '" + request.getOrderId() + "' has already been successfully paid");
            }

            // 3. Process payment in the gateway (Mock Strategy)
            PaymentProvider.PaymentResult result = paymentProvider.processPayment(request);
            PaymentStatus status = result.success() ? PaymentStatus.SUCCESS : PaymentStatus.FAILED;

            // 4. Save payment record in PostgreSQL (payment_db)
            Payment payment = Payment.builder()
                    .orderId(request.getOrderId())
                    .userId(request.getUserId())
                    .amount(request.getAmount())
                    .paymentMethod(request.getPaymentMethod())
                    .status(status)
                    .transactionReference(result.transactionReference())
                    .idempotencyKey(request.getIdempotencyKey())
                    .build();

            Payment savedPayment = paymentRepository.save(payment);

            // 5. Publish Asynchronous Event to Kafka (decoupled from order-service)
            PaymentProcessedEvent event = PaymentProcessedEvent.builder()
                    .paymentId(savedPayment.getId())
                    .orderId(savedPayment.getOrderId())
                    .userId(savedPayment.getUserId())
                    .amount(savedPayment.getAmount())
                    .status(status.name())
                    .paymentMethod(savedPayment.getPaymentMethod().name())
                    .transactionReference(savedPayment.getTransactionReference())
                    .timestamp(LocalDateTime.now())
                    .build();

            kafkaProducer.sendPaymentEvent(event);

            meterRegistry.counter("ecommerce.payments.processed",
                    "method", String.valueOf(payment.getPaymentMethod()),
                    "status", String.valueOf(payment.getStatus())).increment();

            return toResponse(savedPayment);
        } finally {
            sample.stop(meterRegistry.timer("ecommerce.payment.processing.duration"));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Payment with id '" + id + "' not found"));
        return toResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrderId(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Payment for order id '" + orderId + "' not found"));
        return toResponse(payment);
    }

    // ---- Helpers ----

    private PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrderId())
                .userId(payment.getUserId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus())
                .transactionReference(payment.getTransactionReference())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
