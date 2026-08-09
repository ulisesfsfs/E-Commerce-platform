package com.ecommerce.notification.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockReservationFailedEvent {

    private Long orderId;
    private String productId;
    private Integer requestedQuantity;
    private Integer availableStock;
    private String reason;
}
