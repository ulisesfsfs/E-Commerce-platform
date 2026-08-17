package com.ecommerce.order.client;

import com.ecommerce.order.client.dto.CartClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;

@Component
public class CartClientFallback implements CartClient {

    private static final Logger log = LoggerFactory.getLogger(CartClientFallback.class);

    @Override
    public CartClientResponse getCart(String userId) {
        log.warn("Fallback triggered for CartClient.getCart for userId: {}. Cart service is currently unavailable.", userId);
        return CartClientResponse.builder()
                .userId(userId)
                .items(Collections.emptyList())
                .totalPrice(BigDecimal.ZERO)
                .totalItems(0)
                .build();
    }

    @Override
    public void clearCart(String userId) {
        log.warn("Fallback triggered for CartClient.clearCart for userId: {}. Cart service is currently unavailable.", userId);
    }
}
