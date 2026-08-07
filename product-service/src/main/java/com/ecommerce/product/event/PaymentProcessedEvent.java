package com.ecommerce.product.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentProcessedEvent {

    private Long paymentId;
    private Long orderId;
    private String userId;
    private BigDecimal amount;
    private String status; // "SUCCESS" or "FAILED"
    private String paymentMethod;
    private String transactionReference;
}
