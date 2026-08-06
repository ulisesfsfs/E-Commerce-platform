package com.ecommerce.order.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartClientResponse {

    private String userId;
    private List<CartItemClientResponse> items;
    private BigDecimal totalPrice;
    private Integer totalItems;
}
